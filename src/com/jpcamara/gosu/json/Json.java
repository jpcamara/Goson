package com.jpcamara.gosu.json;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gw.lang.reflect.IType;
import gw.lang.reflect.gs.IGosuObject;

public class Json implements IGosuObject {
	private JSONObject json;
	private IType type;
	
	public Json(IType type) {
	  this.type = type;
		this.json = new JSONObject();
	}
	
	public Json(Object json, IType type) {
		if ((json instanceof JSONObject) == false) {
			throw new JSONParserException("Must be a JSONObject");
		}
	  this.type = type;
		this.json = (JSONObject)json;
	}
	
	/**
	* Creates Json object, validating structure of the json 
	* string against the JsonTypeInfo provided.
	*/
	public Json(String json, JsonTypeInfo structure) {
		try {
		  this.type = structure.getOwnersType();
/*			this.json = new JSONObject(json);*/
			this.json = createJson(new JSONObject(json), structure);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
  private JSONObject createJson(JSONObject it, JsonTypeInfo structure) 
    throws JSONException {

    Iterator<String> k = (Iterator<String>)it.keys();
		List<String> keys = new ArrayList<String>();
		while (k.hasNext()) {
			keys.add(k.next());
		}
		for (String key : keys) {
			IType currentType = type.getTypeLoader()
					.getType(type.getNamespace() + "." + new JsonName(key).getName());
			
			Object o = it.get(key);
			if (o instanceof JSONArray && o != null) {
				JSONArray arr = (JSONArray)o;
				ArrayList rawList = new ArrayList();
				for (int i = 0; i < arr.length(); i++) {
					if (arr.get(i) instanceof JSONObject) {
						rawList.add(new Json(createJson((JSONObject)arr.get(i), structure), currentType));
					} else {
						rawList.add(arr.get(i));					
					}
				}
/*        System.out.println(it.remove(key).getClass());*/
				it.put(key, (Object)rawList); //cast it so it doesn't get transformed to a JSONArray
/*        System.out.println(it.get(key).getClass());*/
			} else if (o instanceof JSONObject && o != null) {
				it.remove(key);
				it.put(key, new Json(createJson((JSONObject)o, structure), currentType));
			}
		}
		return it;
	}
	
	public Json(Object json) {
		if ((json instanceof JSONObject) == false) {
			throw new JSONParserException("Must be a JSONObject");
		}
		this.json = (JSONObject)json;
	}
	
	public String serialize() {
		JSONObject output = serializeAsJSONObject();
		return output.toString();
	}
	
	private JSONObject serializeAsJSONObject() {
		JSONObject output = new JSONObject();

		try {
			for (String key : keys()) {
				JsonName name = new JsonName(key);
				if (get(key) instanceof List) {
					List list = (List)get(key);
					JSONArray array = new JSONArray();
					output.put(name.getJsonName(), array);
					if (list.size() > 0 && list.get(0) instanceof Json) {
						List<Json> jsons = (List<Json>)list;
						for (Json j : jsons) {
							array.put(j.serializeAsJSONObject());
						}
					} else {
						for (Object o : list) {
/*              System.out.println("list of Objects: " + o.getClass());*/
							array.put(o);
						}
					}
				} else if (get(key) instanceof Json) {
					Json current = (Json)get(name.getJsonName());
					output.put(name.getJsonName(), current.serializeAsJSONObject());
				} else if (get(key) instanceof JSONArray) {
				  JSONArray arr = (JSONArray)get(key);
				  for (int i = 0; i < arr.length(); i++) {
				    Object o = arr.get(i);
				    if (o instanceof Json) {
				      arr.put(i, ((Json)o).serializeAsJSONObject());
				    }
				  }
				} else {
/*          System.out.println("one Object: " + key + " : " + get(key).getClass());
          System.out.println(get(key).toString());*/
					output.put(name.getJsonName(), get(key));
				}
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return output;
	}
	
	public Object get(String key) {
		try {
			if (json.has(key) == false) {
				return null;
			}
			return json.get(key);
		} catch (JSONException e) {
			throw new JSONParserException(e);		
		}
	}
	
	public Object getWithIndex(String key, int index) {
		try {
			if (json.has(key) == false) {
				return null;
			}
			Object o = json.get(key);
			if (JsonParser.isJSONArray(o) == false) {
				throw new JSONParserException(key + " is not an array");
			}
			return ((JSONArray)o).get(index);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public void put(String key, Object value) {
		try {
			json.put(key, value);
/*      System.out.println(json.get(key).getClass());*/
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public Json getJson(String key) {
		try {
			return new Json(json.getJSONObject(key), type);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public String toString() {
		String str = "[";
		for (String key : keys()) {
			str += key + ",";
		}
		str += "]";
		return str;
	}
	
	public Iterable<String> keys() {
		return new IterableJson();
	}
	
	@Override
	public IType getIntrinsicType() {
	  return type;
	}
	
	private class IterableJson implements Iterable<String> {
		@SuppressWarnings("unchecked")
		@Override
		public Iterator<String> iterator() {
			return (Iterator<String>)json.keys();
		}
	}
}
