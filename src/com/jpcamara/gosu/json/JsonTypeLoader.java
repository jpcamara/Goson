	package com.jpcamara.gosu.json;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.module.IFile;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
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
	private IModule module;
	
	public JsonTypeLoader(IModule module) {
		this.module = module;
	}

	@Override
	public IType getType(String fullyQualifiedName) {
		if (types.isEmpty()) {
			List<Pair<String, IFile>> files = 
				module.getResourceAccess().findAllFilesByExtension("json");
			
			for (Pair<String, IFile> pair : files) {
				Json o = null;
				String fileName = pair.getSecond().getName().replaceAll("\\.json", "");
				int lastIndex = fileName.lastIndexOf(".");
				
				String name = fileName.substring(lastIndex + 1);
				String path = "json." + fileName.substring(0, lastIndex);

				try {
					String jsonString = "";
					Scanner s = new Scanner(pair.getSecond().toJavaFile());
					while (s.hasNextLine()) {
						jsonString += s.nextLine();
					}
					s.close();
					o = new Json(jsonString);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}

				try {
					searchAndAddTypes(name, path, o);
					addType(name, path, o);
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
		types.put(path + "." + name, new JsonType(name, path, this, o));
	}

	private void searchAndAddTypes(String name, String path, Json object)
			throws JSONException {
		for (String key : object.keys()) {
			Object obj = object.get(key);
			if (Json.isJSONObject(obj)) {
				searchAndAddTypes(key, path, object.getJson(key));
				addType(namify(key), path, object.getJson(key));
			} else if (Json.isJSONArray(obj)) {
				Object arrEntry = object.getWithIndex(key, 0);
				if (Json.isJSONObject(arrEntry)) {
					Json typeInArray = new Json(arrEntry);
					searchAndAddTypes(key, path, typeInArray);
					addType(namify(key), path, typeInArray);
				}
			}
		}
	}

	private String namify(String name) {
		return name;
	}

	@Override
	public Set<String> getAllTypeNames() {
		Set<String> typeNames = new HashSet<String>();

		List<Pair<String, IFile>> files = 
			module.getResourceAccess().findAllFilesByExtension("json");
		
		for (Pair<String, IFile> pair : files) {
			Set<String> types = new HashSet<String>();
			File jsonFile = pair.getSecond().toJavaFile();
			
			try {
				Scanner s = new Scanner(jsonFile).useDelimiter("\\Z");
				String content = s.next();
				Json j = new Json(content);
				s.close();
				types.addAll(j.getAllTypeNames());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			String fileName = jsonFile.getName().replaceAll("\\.json", "");
			String[] parts = fileName.split("\\.");
			String[] namespace = new String[parts.length - 1];
			java.lang.System
					.arraycopy(parts, 0, namespace, 0, parts.length - 1);

			String ns = "json";
			for (String piece : namespace) {
				ns += "." + piece;
			}

			typeNames.add(ns + "." + parts[parts.length - 1]);
			for (String type : types) {
				typeNames.add(ns + "." + type);
			}
		}
		return typeNames;
	}

	@Override
	public Set<String> getAllNamespaces() {
		Set<String> allNamespaces = new HashSet<String>();
		allNamespaces.add("json");

		List<Pair<String, IFile>> files = 
			module.getResourceAccess().findAllFilesByExtension("json");
		
		for (Pair<String, IFile> pair : files) {
			String fileName = pair.getSecond().toJavaFile().getName().replaceAll("\\.java", "");
			String[] parts = fileName.split("\\.");
			String[] namespace = new String[parts.length - 1];
			java.lang.System
					.arraycopy(parts, 0, namespace, 0, parts.length - 1);

			String ns = "json";
			for (String piece : namespace) {
				ns += "." + piece;
				allNamespaces.add(ns);
			}
		}

		return allNamespaces;
	}

	@Override
	public List<String> getHandledPrefixes() {
		return Arrays.asList(getAllNamespaces().toArray(new String[] {}));
	}
}
