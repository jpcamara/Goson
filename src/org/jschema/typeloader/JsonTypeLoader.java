package org.jschema.typeloader;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;
import gw.util.concurrent.LazyVar;
import org.jschema.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
          addTypes(types, new JsonName(jshFile.name), jshFile.path, jshFile.content);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      System.out.println(types.keySet());
    }
  }

  private void addTypes(Map<String, IType> types, JsonName name, String path, Object o) {
    if (o instanceof List && !((List)o).isEmpty()) {
      o = ((List)o).get(0);
    }
    if (o instanceof Map) {
      Map<Object, Object> type = (Map<Object, Object>)o;
//      System.out.println("Map: " + name + " content: " + type);
      if (type.get("enum") != null) {
        types.put(path + "." + name, new JsonEnumType(name, path, this, o));
      } else if (types.get("map_of") != null) {
//        System.out.println("map_of: " + name);
        addTypes(types, name, path, type.get("map_of"));
      } else {
        for (Object key : type.keySet()) {
//          System.out.println("key: [" + key + "]");
          addTypes(types, name.copyAndAppend((String)key), path, type.get(key));
        }
        types.put(path + "." + name, new JsonType(name, path, this, o));
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
    List<String> prefixes = Arrays.asList(getAllNamespaces().toArray(new String[] {}));
    return prefixes;
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
      List<JsonFile> init = new java.util.ArrayList<JsonFile>();

      List<Pair<String, IFile>> files = getModule().getResourceAccess().findAllFilesByExtension(JSC_EXT);
      for (Pair<String, IFile> pair : files) {
        JsonFile current = new JsonFile();

        String fileName = pair.getSecond().getName().replaceAll("\\." + JSC_EXT, "");
        String path = pair.getFirst().replaceAll(pair.getSecond().getName(), "");
        if (path.isEmpty()) {
          throw new RuntimeException("Cannot have Simple JSON Schema definitions in the default package");
        }

        int lastSeparatorIndex = path.lastIndexOf(File.separator);
        path = path.substring(0, lastSeparatorIndex).replace(File.separator, ".");

        int lastIndex = fileName.lastIndexOf(".");
        current.name = fileName.substring(lastIndex + 1);
        current.path = path;

        Scanner s = null;
        try {
          java.lang.StringBuilder jsonString = new java.lang.StringBuilder();
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
  };

  private static class JsonFile {
    private Object content;
    private String path;
    private String name;
  }
}
