package com.jpcamara.goson;

import gw.fs.IFile;
import gw.fs.ResourcePath;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.lang.reflect.module.IResourceAccess;
import gw.util.Pair;
import gw.util.concurrent.LazyVar;

import java.io.FileNotFoundException;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.json.JSONObject;

public class JsonTypeLoader extends TypeLoaderBase {
  private Map<String, IType> types = new HashMap<String, IType>();
  private static final String EXT = "jsd";

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
      List<JsonFile> files = jsonFiles.get();
      for (JsonFile file : files) {
        try {
          searchAndAddTypes(file.name, file.path, file.content);
          addType(file.name, file.path, file.content);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void addType(String name, String path, JsonParser o) {
    if (o.get("enum") != null) {
      addEnumType(name, path, o);
      return;
    }
    if (o.get("map_of") != null) {
      o = o.copy();
      JsonParser mapStuff = o.getJsonParser("map_of");
      if (mapStuff.get("value") instanceof JSONObject) {
        mapStuff = mapStuff.getJsonParser("value");
        for (String key : mapStuff.keys()) {
          o.put(key, mapStuff.get(key));
        }
      }
      o.put("map_of", null);
      o.put("value", null);
    }
    JsonName typeName = new JsonName(name);
    JsonType type = new JsonType(typeName, path, this, o);
    types.put(path + "." + typeName.getName(), type);
  }

  private void addEnumType(String name, String path, JsonParser o) {
    JsonName typeName = new JsonName(name);
    JsonEnumType type = new JsonEnumType(typeName, path, this, o);
    types.put(path + "." + typeName.getName(), type);
  }

  private void searchAndAddTypes(String name, String path, JsonParser object)
    throws Exception {
    for (String key : object.keys()) {
      Object obj = object.get(key);
      if (JsonParser.isJSONObject(obj)) {
        searchAndAddTypes(key, path, object.getJsonParser(key));
        addType(key, path, object.getJsonParser(key));
      } else if (JsonParser.isJSONArray(obj)) {
        Object arrEntry = object.getWithIndex(key, 0);
        if (JsonParser.isJSONObject(arrEntry)) {
          JsonParser typeInArray = new JsonParser(arrEntry);
          searchAndAddTypes(key, path, typeInArray);
          addType(key, path, typeInArray);
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
    List<String> prefixes = Arrays.asList(getAllNamespaces().toArray(new String[] {}));
    return prefixes;
  }

  /*
  * Default implementation to handle Gosu 0.9 reqs
  */
  @Override
  public boolean handlesNonPrefixLoads() {
    return true;
  }

  private LazyVar<List<JsonFile>> jsonFiles = new LazyVar<List<JsonFile>>() {
    @Override
    protected List<JsonFile> init() {
      List<JsonFile> init = new java.util.ArrayList<JsonFile>();

      List<Pair<String, IFile>> files = 
      JsonTypeLoader.this.getModule().getResourceAccess().findAllFilesByExtension(EXT);
      for (Pair<String, IFile> pair : files) {
        JsonFile current = new JsonFile();

        String fileName = pair.getSecond().getName().replaceAll("\\." + EXT, "");
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
          current.content = new JsonParser(jsonString.toString());
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
    private JsonParser content;
    private String path;
    private String name;
  }
}