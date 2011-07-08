package com.jpcamara.gosu.json;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
	private JSONObject json;
	
	public JsonParser() {
		this.json = new JSONObject();
	}
	
  public JsonParser(String json) {
    try {
      this.json = new JSONObject(json);
    } catch (JSONException e) {
      throw new JSONParserException(e);
    }
  }
	
	public JsonParser(Object json) {
		if ((json instanceof JSONObject) == false) {
			throw new JSONParserException("Must be a JSONObject");
		}
		this.json = (JSONObject)json;
	}
		
	public Set<String> getAllTypeNames() {
		Set<String> types = new HashSet<String>();
		for (String key : keys()) {
			if (get(key) instanceof JSONObject) {
				types.add(key);
				types.addAll(getJsonParser(key).getAllTypeNames());
			} else if (get(key) instanceof JSONArray) {
				JSONArray arr = (JSONArray)get(key);
				try {
					if (arr.get(0) instanceof JSONObject) {
						types.add(key);
						types.addAll(new JsonParser((JSONObject)arr.get(0)).getAllTypeNames());
					}
				} catch (JSONException e) {
					throw new JSONParserException(e);
				}
			}
		}
		return types;
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
	
	public JsonParser getJsonParser(String key) {
		try {
			return new JsonParser(json.getJSONObject(key));
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
	
	public static boolean isJSONArray(Object o) {
		return o instanceof JSONArray;
	}
	
	public static boolean isJSONObject(Object o) { 
		return o instanceof JSONObject;
	}
	
	public static boolean isJSONNull(Object o) {
		return JSONObject.NULL.getClass().isInstance(o);
	}
	
	public Iterable<String> keys() {
		return new IterableJson();
	}
	
	private class IterableJson implements Iterable<String> {
		@SuppressWarnings("unchecked")
		@Override
		public Iterator<String> iterator() {
			return (Iterator<String>)json.keys();
		}
	}
}
