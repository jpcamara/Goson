package com.jpcamara.goson;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class JsonKeys implements Iterable<String> {
  private JSONObject json;
  
  public JsonKeys(JSONObject json) {
    this.json = json;
  }
  
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<String> iterator() {
		return (Iterator<String>)json.keys();
	}
}