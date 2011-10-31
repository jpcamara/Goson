package org.jschema.typeloader;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.lang.reflect.module.IResourceAccess;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import gw.util.concurrent.LazyVar;
import org.jschema.parser.JsonParseException;
import org.jschema.typeloader.rpc.JSchemaCustomizedRPCType;
import org.jschema.typeloader.rpc.JSchemaRPCType;
import org.jschema.util.JSchemaUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class JSchemaTypeLoader extends TypeLoaderBase {

  private Map<String, IType> types = new HashMap<String, IType>();

  private static final String JSC_RPC_EXT = "jsc-rpc";
  private static final String JSC_EXT = "jsc";
  private static final String JSON_EXT = "json";

  public JSchemaTypeLoader(IModule env) {
    super(env);
  }

  public JSchemaTypeLoader(IModule env, IResourceAccess resourceAccess) {
    this(env);
  }

  @Override
  public IType getType(String fullyQualifiedName) {
    maybeInitTypes();
    if (fullyQualifiedName == null || types.get(fullyQualifiedName) == null) {
      return null;
    }
    IType iType = types.get( fullyQualifiedName );
    return TypeSystem.getOrCreateTypeReference( iType );
  }

  private void maybeInitTypes() {
    if (types.isEmpty()) {
      for (JsonFile jshFile : jscFiles.get()) {
        try {
          jshFile.parseContent();
          addRootType(types, new Stack<Map<String, String>>(), jshFile);
        } catch (Exception e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
      for (JsonFile jshRpcFile : jscRpcFiles.get()) {
        try {
          jshRpcFile.parseContent();
          addRpcTypes(types, jshRpcFile);
        } catch (Exception e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
      for (JsonFile jsonFile : jsonFiles.get()) {
        try {
          jsonFile.parseContent();
          convertToJSchemaAndAddRootType(types, jsonFile);
        } catch (Exception e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
      initInnerClasses(types);
    }
  }

  private void convertToJSchemaAndAddRootType(Map<String, IType> types, JsonFile jsonFile) {
    jsonFile.content = JSchemaUtils.convertJsonToJSchema(jsonFile.content);
    addRootType(types, new Stack<Map<String, String>>(), jsonFile);
    return;
  }

  private void initInnerClasses(Map<String, IType> types) {
    for (String name : types.keySet()) {
      IType iType = types.get(name);
      IType outerType = types.get(iType.getNamespace());
      if (outerType instanceof IJSchemaType) {
        ((IJSchemaType) outerType).addInnerClass(iType);
      }
    }
  }

  private void addRootType(Map<String, IType> types, Stack<Map<String, String>> typeDefs, JsonFile jshFile) {
    if (jshFile.content instanceof List) {
      int depth = 0;
      while (jshFile.content instanceof List && ((List) jshFile.content).size() > 0) {
        depth++;
        jshFile.content = ((List) jshFile.content).get(0);
      }
      addTypes(types, typeDefs, jshFile.rootTypeName + ".Element", jshFile.content);
      types.put(jshFile.rootTypeName, new JSchemaListWrapperType(jshFile.rootTypeName, this, depth, jshFile.content) );
    } else {
      addTypes(types, typeDefs, jshFile.rootTypeName, jshFile.content);
    }
  }
  
  private void addTypes(Map<String, IType> types, Stack<Map<String, String>> typeDefs, String name, Object o) {
    // Handles this "customers" : [{ "name" : "string", "id" : "int"}]
    // i.e. an type def in an array field def
    while (o instanceof List && !((List)o).isEmpty()) {
      o = ((List)o).get(0);
    }
    if (o instanceof Map) {
      Map<Object, Object> jsonMap = (Map<Object, Object>)o;
      if (jsonMap.get(JSchemaUtils.JSCHEMA_ENUM_KEY) != null) {
        putType(types, name, new JSchemaEnumType(name, this, o));
      } else if (jsonMap.get("map_of") != null) {
        addTypes(types, typeDefs, name, jsonMap.get("map_of"));
      } else {
        try {
          typeDefs.push(new HashMap<String, String>());
          processTypeDefs(types, typeDefs, name, jsonMap);
          for (Object key : jsonMap.keySet()) {
            if (!JSchemaUtils.JSCHEMA_TYPEDEFS_KEY.equals(key)) {
              // RECURSION. This will call for every field in the definition. We rely on the if(o instanceof Map) thing up
              // there to cause those calls to be ignored.
              addTypes(types, typeDefs, name + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(key.toString()),
                jsonMap.get(key));
            }
          }
          putType(types, name, new JSchemaType(name, this, o, copyTypeDefs(typeDefs)));
        } finally {
          typeDefs.pop();
        }
      }
    }
  }

  private void putType(Map<String, IType> types, String name, IType type) {
    types.put(name, TypeSystem.getOrCreateTypeReference(type));
  }

  private Map<String, String> copyTypeDefs(Stack<Map<String, String>> typeDefs) {
    HashMap<String, String> allTypeDefs = new HashMap<String, String>();
    for (Map<String, String> typeDef : typeDefs) {
      allTypeDefs.putAll(typeDef);
    }
    return allTypeDefs;
  }

  private void processTypeDefs(Map<String, IType> types, Stack<Map<String, String>> typeDefs, String name, Map o) {
    Object currentTypeDefs = o.get(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY);
    if (currentTypeDefs instanceof Map) {
      Set set = ((Map) currentTypeDefs).keySet();
      List<IJSchemaType> previousTypeDefs = new ArrayList<IJSchemaType>();
      for (Object typeDefTypeName : set) {
        String rawName = typeDefTypeName.toString();
        String relativeName = JSchemaUtils.convertJSONStringToGosuIdentifier(rawName);
        String fullyQualifiedName = name + "." + relativeName;
        typeDefs.peek().put(rawName, fullyQualifiedName);
        addTypes(types, typeDefs, fullyQualifiedName, ((Map) currentTypeDefs).get(typeDefTypeName));
        for (IJSchemaType previousTypeDef : previousTypeDefs) {
          previousTypeDef.getTypeDefs().put(rawName, fullyQualifiedName);
        }
        previousTypeDefs.add((IJSchemaType) types.get(fullyQualifiedName));
      }
    }
  }

  private void addRpcTypes(Map<String, IType> types, JsonFile jshRpcFile)
  {
    Stack<Map<String, String>> typeDefs = new Stack<Map<String, String>>();
    typeDefs.push(new HashMap<String, String>());
    Map<String, Map<String, Object>> defaultValues = new HashMap<String, Map<String, Object>>();
    if (jshRpcFile.content instanceof Map) {
      processTypeDefs(types, typeDefs, jshRpcFile.rootTypeName, (Map) jshRpcFile.content);
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
                        ((Map) arg).get(key));
                    }
                  }
                }
              }
            }
            // add the return type
            addTypes(types, typeDefs, functionTypeName, ((Map) function).get("returns"));
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
    return new HashSet<String>( types.keySet() );
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

  private LazyVar<List<JsonFile>> jscFiles = new LazyVar<List<JsonFile>>() {
    @Override
    protected List<JsonFile> init() {
      return findFilesOfType(JSC_EXT);
    }
  };

  private LazyVar<List<JsonFile>> jscRpcFiles = new LazyVar<List<JsonFile>>() {
    @Override
    protected List<JsonFile> init() {
      return findFilesOfType(JSC_RPC_EXT);
    }
  };

  private LazyVar<List<JsonFile>> jsonFiles = new LazyVar<List<JsonFile>>() {
    @Override
    protected List<JsonFile> init() {
      return findFilesOfType(JSON_EXT);
    }
  };

  private List<JsonFile> findFilesOfType(String extension) {
    List<JsonFile> init = new java.util.ArrayList<JsonFile>();

    List<Pair<String, IFile>> files = getModule().getResourceAccess().findAllFilesByExtension(extension);
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
