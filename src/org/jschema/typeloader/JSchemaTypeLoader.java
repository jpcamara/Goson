package org.jschema.typeloader;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import gw.util.concurrent.LockingLazyVar;
import org.jschema.parser.JsonParseException;
import org.jschema.typeloader.rpc.JSchemaCustomizedRPCType;
import org.jschema.typeloader.rpc.JSchemaRPCType;
import org.jschema.util.JSchemaUtils;

import java.io.FileNotFoundException;
import java.util.*;

public class JSchemaTypeLoader extends TypeLoaderBase {

  private Map<String, IJSchemaType> _rawTypes = new HashMap<String, IJSchemaType>();
  private Map<IFile, List<IType>> _filesToTypes = new HashMap<IFile, List<IType>>();

  private static final String JSC_RPC_EXT = "jsc-rpc";
  private static final String JSC_EXT = "jsc";
  private static final String JSON_EXT = "json";
  private boolean _initing;

  public JSchemaTypeLoader(IModule env) {
    super(env);
  }

  @Override
  public IType getType(String fullyQualifiedName) {
    maybeInitTypes();
    if (fullyQualifiedName == null || _rawTypes.get(fullyQualifiedName) == null) {
      return null;
    }
    IType iType = _rawTypes.get(fullyQualifiedName);
    return TypeSystem.getOrCreateTypeReference(iType);
  }

  //TODO cgross - this should be lazy
  private void maybeInitTypes() {
    if (!_initing) {
      if (_rawTypes.isEmpty()) {
        _initing = true;
        try {
          for (JsonFile jshFile : _jscFiles.get()) {
            try {
              jshFile.parseContent();
              addRootType(_rawTypes, new Stack<Map<String, String>>(), jshFile, jshFile.file, _filesToTypes);
            } catch (Exception e) {
              throw GosuExceptionUtil.forceThrow(e);
            }
          }
          for (JsonFile jshRpcFile : _jscRpcFiles.get()) {
            try {
              jshRpcFile.parseContent();
              addRpcTypes(_rawTypes, jshRpcFile, jshRpcFile.file, _filesToTypes);
            } catch (Exception e) {
              throw GosuExceptionUtil.forceThrow(e);
            }
          }
          for (JsonFile jsonFile : _jsonFiles.get()) {
            try {
              jsonFile.parseContent();
              convertToJSchemaAndAddRootType(_rawTypes, jsonFile, jsonFile.file, _filesToTypes);
            } catch (Exception e) {
              throw GosuExceptionUtil.forceThrow(e);
            }
          }
          initInnerClasses(_rawTypes);
        } finally {
          _initing = false;
        }
      }
    }
  }

  @Override
  public List<IType> refreshedFile(IFile file) {
    List<IType> types = _filesToTypes.get(file);
    if (types == null) {
      return Collections.emptyList();
    } else {
      _rawTypes.clear();
      _filesToTypes.clear();
      if (file.getExtension().equals(JSC_EXT)) {
        _jscFiles.clear();
      }
      if (file.getExtension().equals(JSC_RPC_EXT)) {
        _jscRpcFiles.clear();
      }
      if (file.getExtension().equals(JSON_EXT)) {
        _jsonFiles.clear();
      }
      return types;
    }
  }

  private void convertToJSchemaAndAddRootType(Map<String, IJSchemaType> rawTypes, JsonFile jsonFile, IFile file, Map<IFile, List<IType>> fileMapping) {
    jsonFile.content = JSchemaUtils.convertJsonToJSchema(jsonFile.content);
    addRootType(rawTypes, new Stack<Map<String, String>>(), jsonFile, file, fileMapping);
    return;
  }

  private void initInnerClasses(Map<String, IJSchemaType> rawTypes) {
    for (String name : rawTypes.keySet()) {
      IType iType = rawTypes.get(name);
      IType outerType = rawTypes.get(iType.getNamespace());
      if (outerType instanceof IJSchemaType) {
        ((IJSchemaType) outerType).addInnerClass(TypeSystem.getTypeReference(iType));
      }
    }
  }

  private void addRootType(Map<String, IJSchemaType> rawTypes, Stack<Map<String, String>> typeDefs, JsonFile jshFile, IFile file, Map<IFile, List<IType>> fileMapping) {
    if (jshFile.content instanceof List) {
      int depth = 0;
      while (jshFile.content instanceof List && ((List) jshFile.content).size() > 0) {
        depth++;
        jshFile.content = ((List) jshFile.content).get(0);
      }
      addTypes(rawTypes, typeDefs, jshFile.rootTypeName + ".Element", jshFile.content, file, fileMapping);
      rawTypes.put(jshFile.rootTypeName, new JSchemaListWrapperType(jshFile.rootTypeName, this, depth, jshFile.content));
    } else {
      addTypes(rawTypes, typeDefs, jshFile.rootTypeName, jshFile.content, file, fileMapping);
    }
  }
  
  private void addTypes(Map<String, IJSchemaType> rawTypes, Stack<Map<String, String>> typeDefs, String name, Object o, IFile file, Map<IFile, List<IType>> fileMapping) {
    // Handles this "customers" : [{ "name" : "string", "id" : "int"}]
    // i.e. an type def in an array field def
    while (o instanceof List && !((List)o).isEmpty()) {
      o = ((List)o).get(0);
    }
    if (o instanceof Map) {
      Map<Object, Object> jsonMap = (Map<Object, Object>)o;
      if (jsonMap.get(JSchemaUtils.JSCHEMA_ENUM_KEY) != null) {
        putType(rawTypes, name, new JSchemaEnumType(name, this, o), file, fileMapping);
      } else if (jsonMap.get("map_of") != null) {
        addTypes(rawTypes, typeDefs, name, jsonMap.get("map_of"), file, fileMapping);
      } else {
        try {
          typeDefs.push(new HashMap<String, String>());
          processTypeDefs(rawTypes, typeDefs, name, jsonMap, file, fileMapping);
          for (Object key : jsonMap.keySet()) {
            if (!JSchemaUtils.JSCHEMA_TYPEDEFS_KEY.equals(key)) {
              // RECURSION. This will call for every field in the definition. We rely on the if(o instanceof Map) thing up
              // there to cause those calls to be ignored.
              addTypes(rawTypes, typeDefs, name + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(key.toString()),
                jsonMap.get(key), file, fileMapping);
            }
          }
          putType(rawTypes, name, new JSchemaType(name, this, o, copyTypeDefs(typeDefs)), file, fileMapping);
        } finally {
          typeDefs.pop();
        }
      }
    }
  }

  private void putType(Map<String, IJSchemaType> rawTypes, String name, IJSchemaType type, IFile file, Map<IFile, List<IType>> fileMapping) {
    rawTypes.put(name, type);
    List<IType> iTypes = fileMapping.get(file);
    if (iTypes == null) {
      iTypes = new ArrayList<IType>();
      fileMapping.put(file, iTypes);
    }
    iTypes.add(TypeSystem.getOrCreateTypeReference(type));
  }

  private Map<String, String> copyTypeDefs(Stack<Map<String, String>> typeDefs) {
    HashMap<String, String> allTypeDefs = new HashMap<String, String>();
    for (Map<String, String> typeDef : typeDefs) {
      allTypeDefs.putAll(typeDef);
    }
    return allTypeDefs;
  }

  private void processTypeDefs(Map<String, IJSchemaType> types, Stack<Map<String, String>> typeDefs, String name, Map o, IFile file, Map<IFile, List<IType>> fileMapping) {
    Object currentTypeDefs = o.get(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY);
    if (currentTypeDefs instanceof Map) {
      Set set = ((Map) currentTypeDefs).keySet();
      List<IJSchemaType> previousTypeDefs = new ArrayList<IJSchemaType>();
      for (Object typeDefTypeName : set) {
        String rawName = typeDefTypeName.toString();
        String relativeName = JSchemaUtils.convertJSONStringToGosuIdentifier(rawName);
        String fullyQualifiedName = name + "." + relativeName;
        typeDefs.peek().put(rawName, fullyQualifiedName);
        addTypes(types, typeDefs, fullyQualifiedName, ((Map) currentTypeDefs).get(typeDefTypeName), file, fileMapping);
        for (IJSchemaType previousTypeDef : previousTypeDefs) {
          previousTypeDef.getTypeDefs().put(rawName, fullyQualifiedName);
        }
        previousTypeDefs.add(types.get(fullyQualifiedName));
      }
    }
  }

  private void addRpcTypes(Map<String, IJSchemaType> types, JsonFile jshRpcFile, IFile file, Map<IFile, List<IType>> fileMapping)
  {
    Stack<Map<String, String>> typeDefs = new Stack<Map<String, String>>();
    typeDefs.push(new HashMap<String, String>());
    Map<String, Map<String, Object>> defaultValues = new HashMap<String, Map<String, Object>>();
    if (jshRpcFile.content instanceof Map) {
      processTypeDefs(types, typeDefs, jshRpcFile.rootTypeName, (Map) jshRpcFile.content, file, fileMapping);
      Object functions = ((Map) jshRpcFile.content).get(JSchemaUtils.JSCHEMA_FUNCTIONS_KEY);
      if (functions instanceof List) {
        for (Object function : (List) functions) {
          if (function instanceof Map) {
            Map functionMap = (Map) function;
            String str = functionMap.get("name").toString();
            String functionTypeName =  jshRpcFile.rootTypeName + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(str);

            // add parameter names
            Object args = functionMap.get("args");
            if (args instanceof List) {
              for (Object arg : (List) args) {
                if (arg instanceof Map) {
                  Set argSpecKeys = ((Map) arg).keySet();
                  for (Object key : argSpecKeys) {
                    if (key.equals("default")) {
                      Map<String, Object> argsMap = defaultValues.get(str);
                      if (argsMap == null) {
                        argsMap = new HashMap<String, Object>();
                        defaultValues.put(str, argsMap);
                      }
                      argsMap.put(((Map) arg).keySet().iterator().next().toString(), ((Map) arg).get("default"));
                    } else if (key.equals("description")) {
                      //ignore
                    } else {
                      addTypes(types,
                        typeDefs, functionTypeName + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(key.toString()),
                        ((Map) arg).get(key), file, fileMapping);
                    }
                  }
                }
              }
            }
            // add the return type
            addTypes(types, typeDefs, functionTypeName, ((Map) function).get("returns"), file, fileMapping);
          }
        }
      }
      types.put(jshRpcFile.rootTypeName, new JSchemaRPCType(jshRpcFile.rootTypeName, this, jshRpcFile.content, typeDefs.peek(), defaultValues, jshRpcFile.stringContent));
      String customizedTypeName = jshRpcFile.rootTypeName + JSchemaCustomizedRPCType.TYPE_SUFFIX;
      types.put(customizedTypeName, new JSchemaCustomizedRPCType(customizedTypeName, this, jshRpcFile.content, typeDefs.peek(), defaultValues, jshRpcFile.stringContent));
    }
  }

  @Override
  public Set<String> getAllTypeNames() {
    maybeInitTypes();
    return new HashSet<String>( _rawTypes.keySet() );
  }

  @Override
  public List<String> getHandledPrefixes() {
    return Collections.emptyList();
  }

  /*
  * Default implementation to handle Gosu 0.9 reqs
  */
//  @Override TODO restore later
  public boolean handlesNonPrefixLoads() {
    return true;
  }

  private LockingLazyVar<List<JsonFile>> _jscFiles = new LockingLazyVar<List<JsonFile>>() {
    @Override
    protected List<JsonFile> init() {
      return findFilesOfType(JSC_EXT);
    }
  };

  private LockingLazyVar<List<JsonFile>> _jscRpcFiles = new LockingLazyVar<List<JsonFile>>() {
    @Override
    protected List<JsonFile> init() {
      return findFilesOfType(JSC_RPC_EXT);
    }
  };

  private LockingLazyVar<List<JsonFile>> _jsonFiles = new LockingLazyVar<List<JsonFile>>() {
    @Override
    protected List<JsonFile> init() {
      return findFilesOfType(JSON_EXT);
    }
  };

  private List<JsonFile> findFilesOfType(String extension) {
    List<JsonFile> init = new java.util.ArrayList<JsonFile>();

    List<Pair<String, IFile>> files = getModule().getFileRepository().findAllFilesByExtension(extension);
    for (Pair<String, IFile> pair : files) {
      JsonFile current = new JsonFile();
      current.file = pair.getSecond();
      String relativeNameAsFile = pair.getFirst();
      int trimmedLength = relativeNameAsFile.length() - extension.length() - 1;
      String typeName = relativeNameAsFile.replace('/', '.').replace('\\', '.').substring(0, trimmedLength);
      if (typeName.indexOf('.') == -1) {
        //TODO ignore?
        throw new RuntimeException("Cannot have Simple JSON Schema definitions in the default package");
      }
      current.rootTypeName = typeName;
      init.add(current);
    }
    return init;
  }

  private static class JsonFile {
    private Object content;
    private String stringContent;
    private String rootTypeName;
    private IFile file;

    @Override
    public String toString() {
      return file.getPath().getPathString();
    }

    public void parseContent() {
      Scanner s = null;
      try {
        StringBuilder jsonString = new StringBuilder();
        s = new Scanner(file.toJavaFile());
        while (s.hasNextLine()) {
          jsonString.append(s.nextLine());
          jsonString.append("\n");
        }
        stringContent = jsonString.toString();
        content = JSchemaUtils.parseJSchema(stringContent);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      } catch (JsonParseException e) {
        System.out.println("Unable to parse JSON file " + file.toJavaFile().getAbsolutePath());
        System.out.println(e.getMessage());
      } finally {
        if (s != null) { s.close(); }
      }
    }
  }
}
