package com.jpcamara.gosu.json;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Json {
	private JSONObject json;
	
	public Json() {
		this.json = new JSONObject();
	}
	
	public Json(String json) {
		try {
			this.json = new JSONObject(json);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public Json(Object json) {
		if ((json instanceof JSONObject) == false) {
			throw new JSONParserException("Must be a JSONObject");
		}
		this.json = (JSONObject)json;
	}
	
	public String serialize() {
		return null;
	}
	
	public Set<String> getAllTypeNames() {
		Set<String> types = new HashSet<String>();
		for (String key : keys()) {
			if (get(key) instanceof JSONObject) {
				types.add(key);
				types.addAll(getJson(key).getAllTypeNames());
			} else if (get(key) instanceof JSONArray) {
				JSONArray arr = (JSONArray)get(key);
				try {
					if (arr.get(0) instanceof JSONObject) {
						types.add(key);
						types.addAll(new Json((JSONObject)arr.get(0)).getAllTypeNames());
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
			return json.get(key);
		} catch (JSONException e) {
			throw new JSONParserException(e);		
		}
	}
	
	public void put(String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public Json getJson(String key) {
		try {
			return new Json(json.getJSONObject(key));
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
	
	public static class JSONParserException extends RuntimeException {
		private static final long serialVersionUID = 5407463188711170624L;

		public JSONParserException(JSONException e) {
			super(e);
		}
		
		public JSONParserException(String message) {
			super(message);
		}
	}
}
