package org.jschema.typeloader;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import gw.util.concurrent.LazyVar;
import org.jschema.parser.JSONParser;
import org.jschema.util.JSONUtils;

import java.io.*;
import java.util.*;

public class JsonTypeLoader extends TypeLoaderBase {

  private Map<String, IType> types = new HashMap<String, IType>();

  private static final String JSC_RPC_EXT = "jsc-rpc";
  private static final String JSC_EXT = "jsc";

  public JsonTypeLoader(IModule env) {
    super(env);
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
          addTypes(types, jshFile.rootTypeName, jshFile.content);
        } catch (Exception e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
      for (JsonFile jshRpcFile : jscRpcFiles.get()) {
        try {
          addRpcTypes(types, jshRpcFile.rootTypeName, jshRpcFile.content);
        } catch (Exception e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
      System.out.println(types.keySet());
    }
  }

  private void addTypes(Map<String, IType> types, String name, Object o) {
    if (o instanceof List && !((List)o).isEmpty()) {
      o = ((List)o).get(0);
    }
    if (o instanceof Map) {
      Map<Object, Object> jsonMap = (Map<Object, Object>)o;
      if (jsonMap.get("enum") != null) {
        types.put(name, new JsonEnumType(name, this, o));
      } else if (jsonMap.get("map_of") != null) {
        addTypes(types, name, jsonMap.get("map_of"));
      } else {
        for (Object key : jsonMap.keySet()) {
          addTypes(types,
            name + "." + JSONUtils.convertJSONStringToGosuIdentifier(key.toString()),
            jsonMap.get(key));
        }
        types.put(name, new JsonType(name, this, o));
      }
    }
  }

  private void addRpcTypes(Map<String, IType> types, String name, Object o) {
    types.put(name, new JSchemaRPCType(name, this, o));
    if (o instanceof Map) {
      Object functions = ((Map) o).get("functions");
      if (functions instanceof List) {
        for (Object function : (List) functions) {
          if (function instanceof Map) {
            Map functionMap = (Map) function;
            Object str = functionMap.get("name");
            String functionTypeName =  name + JSONUtils.convertJSONStringToGosuIdentifier(str.toString());

            // add parameter names
            Object args = functionMap.get("args");
            if (args instanceof Map) {
              Object argName = ((Map) args).get("name");
              addTypes(types,
                functionTypeName + "." + JSONUtils.convertJSONStringToGosuIdentifier(argName.toString()),
                ((Map) args).get("type"));
            }

            // add the return type
            addTypes(types, functionTypeName, ((Map) function).get("returns"));
          }
        }
      }
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

  private List<JsonFile> findFilesOfType(String extension) {
    List<JsonFile> init = new java.util.ArrayList<JsonFile>();

    List<Pair<String, IFile>> files = getModule().getResourceAccess().findAllFilesByExtension(extension);
    for (Pair<String, IFile> pair : files) {
      JsonFile current = new JsonFile();

      String relativeNameAsFile = pair.getFirst();
      int trimmedLength = relativeNameAsFile.length() - extension.length() - 1;
      String typeName = relativeNameAsFile.replace('/', '.').replace('\\', '.').substring(0, trimmedLength);
      if (typeName.indexOf('.') == -1) {
        //TODO ignore?
        throw new RuntimeException("Cannot have Simple JSON Schema definitions in the default package");
      }
      current.rootTypeName = typeName;

      Scanner s = null;
      try {
        StringBuilder jsonString = new StringBuilder();
        s = new Scanner(pair.getSecond().toJavaFile());
        while (s.hasNextLine()) {
          jsonString.append(s.nextLine());
        }
        current.content = JSONParser.parseJSON(jsonString.toString());
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      } finally {
        if (s != null) { s.close(); }
      }
      init.add(current);
    }
    return init;
  }

  private static class JsonFile {
    private Object content;
    private String rootTypeName;
  }
}
