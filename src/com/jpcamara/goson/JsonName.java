package com.jpcamara.goson;

import gw.util.GosuStringUtil;

public class JsonName {
	private String originalName;
	private String name;
	
	public JsonName(String name) {
		this.name = namify(name);
		originalName = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getJsonName() {
		return originalName;
	}
	
	//FIXME Only handles underscores in the name... and poorly
	private String namify(String name) {
		StringBuilder typeName = new StringBuilder();
		for (String piece : name.split("_")) {
			typeName.append(GosuStringUtil.capitalize(piece));
		}
		
		return typeName.toString();
	}
}
