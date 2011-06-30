package com.jpcamara.gosu.json;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeLoaderBase;
import gw.fs.IFile;
import gw.lang.reflect.module.IModule;
import gw.lang.reflect.module.IResourceAccess;
import gw.util.Pair;
import gw.lang.reflect.TypeSystem;
import gw.util.concurrent.LazyVar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONException;

public class JsonTypeLoader extends TypeLoaderBase {
	private Map<String, IType> types = new HashMap<String, IType>();
	private static final String EXT = "json";
	
	public JsonTypeLoader(IModule env, IResourceAccess access) {
		super(env);
	}
	
	public JsonTypeLoader() {}

	@Override
	public IType getType(String fullyQualifiedName) {
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
		if (fullyQualifiedName == null || types.get(fullyQualifiedName) == null) {
			return null;
		}
		return types.get(fullyQualifiedName);
	}

	private void addType(String name, String path, Json o) {
		JsonName typeName = new JsonName(name);
		JsonType type = new JsonType(typeName, path, this, o);
		types.put(path + "." + typeName.getName(), TypeSystem.getOrCreateTypeReference(type));
	}

	private void searchAndAddTypes(String name, String path, Json object)
			throws JSONException {
		for (String key : object.keys()) {
			Object obj = object.get(key);
			if (Json.isJSONObject(obj)) {
				searchAndAddTypes(key, path, object.getJson(key));
				addType(key, path, object.getJson(key));
			} else if (Json.isJSONArray(obj)) {
				Object arrEntry = object.getWithIndex(key, 0);
				if (Json.isJSONObject(arrEntry)) {
					Json typeInArray = new Json(arrEntry);
					searchAndAddTypes(key, path, typeInArray);
					addType(key, path, typeInArray);
				}
			}
		}
	}

	@Override
	public Set<String> getAllTypeNames() {
		Set<String> typeNames = new HashSet<String>();

		List<JsonFile> files = jsonFiles.get();
		for (JsonFile file : files) {
			Set<String> types = new HashSet<String>();
			types.addAll(file.content.getAllTypeNames());
			
			String[] namespace = file.path.split("\\.");
			
			java.lang.StringBuilder ns = new java.lang.StringBuilder("json");
			for (String piece : namespace) {
				ns.append("." + piece);
			}

			typeNames.add(ns.toString() + "." + new JsonName(file.name).getName());
			for (String type : types) {
				typeNames.add(ns.toString() + "." + new JsonName(type).getName());
			}
		}
		
		return typeNames;
	}

	@Override
	public Set<String> getAllNamespaces() {
		Set<String> allNamespaces = new HashSet<String>();
		allNamespaces.add(EXT);
		
		List<JsonFile> files = jsonFiles.get();
		for (JsonFile file : files) {
			String[] namespace = file.path.split("\\.");
			String ns = EXT;
			for (String piece : namespace) {
				ns += "." + piece;
				allNamespaces.add(ns);
			}
		}
		
		return allNamespaces;
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
				
				String fileName = pair.getSecond().getName().replaceAll("\\.json", "");
				int lastIndex = fileName.lastIndexOf(".");
				
				current.name = fileName.substring(lastIndex + 1);
				current.path = "json." + fileName.substring(0, lastIndex);

				Scanner s = null;
				try {
					java.lang.StringBuilder jsonString = new java.lang.StringBuilder();
					s = new Scanner(pair.getSecond().toJavaFile());
					while (s.hasNextLine()) {
						jsonString.append(s.nextLine());
					}
					current.content = new Json(jsonString.toString());
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
		private Json content;
		private String path;
		private String name;
	}
}
