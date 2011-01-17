package com.jpcamara.gosu.json;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.module.IFile;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;
import gw.util.StreamUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Writer;
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
	
	public JsonTypeLoader(IModule env) {
		super(env);
	}
	
	public JsonTypeLoader() {
		super();
	}

	@Override
	public IType getType(String fullyQualifiedName) {
		if (types.isEmpty()) {
			List<Pair<String, IFile>> files = 
				getModule().getResourceAccess().findAllFilesByExtension("json");
			writeIt("getType1: " + fullyQualifiedName);
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
		writeIt("getTyp2e: " + fullyQualifiedName);
		if (fullyQualifiedName == null || types.get(fullyQualifiedName) == null) {
			return null;
		}
		writeIt("getType3: " + fullyQualifiedName);
		return types.get(fullyQualifiedName);
	}

	private void addType(String name, String path, Json o) {
		JsonName typeName = new JsonName(name);
		JsonType type = new JsonType(typeName, path, this, o);
		writeIt("original name: " + typeName.getJsonName());
		types.put(path + "." + typeName.getName(), type);
	}
	
	private static int i = 0;
	public void writeIt(String content) {
		try {
			File newFile = new File("/Users/johnpcamara/Desktop/output" + i + ".txt");
//			newFile.delete();
//			newFile.createNewFile();
			Writer out = StreamUtil.getOutputStreamWriter(new FileOutputStream(newFile));
			out.append(content);
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		writeIt("typeNames");
		Set<String> typeNames = new HashSet<String>();

		List<Pair<String, IFile>> files = 
			getModule().getResourceAccess().findAllFilesByExtension("json");
		
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

			typeNames.add(ns + "." + new JsonName(parts[parts.length - 1]).getName());
			for (String type : types) {
				typeNames.add(ns + "." + new JsonName(type).getName());
			}
			writeIt(typeNames.toString());
		}
		
		return typeNames;
	}

	@Override
	public Set<String> getAllNamespaces() {
		Set<String> allNamespaces = new HashSet<String>();
		allNamespaces.add("json");

		List<Pair<String, IFile>> files = 
			getModule().getResourceAccess().findAllFilesByExtension("json");
		
		for (Pair<String, IFile> pair : files) {
			String fileName = pair.getSecond().toJavaFile().getName().replaceAll("\\.json", "");
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
		List<String> prefixes = Arrays.asList(getAllNamespaces().toArray(new String[] {}));
		return prefixes;
	}
}
