package org.jschema.typeloader;

import gw.util.GosuStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonName {
	private List<String> originalNames;
	private List<String> names;

  public JsonName(JsonName append, String name) {
    this.originalNames = new ArrayList<String>(append.getOriginalNames());
    this.names = new ArrayList<String>(append.getNames());
    originalNames.add(name);
    names.add(namify(name));
  }

	public JsonName(String... name) {
    if (name.length == 0) {
      throw new IllegalArgumentException("Must have at least one name.");
    }
    this.originalNames = new ArrayList<String>();
    this.names = new ArrayList<String>();
    for (String originalName : name) {
      originalNames.add(originalName);
      names.add(namify(originalName));
    }
	}
	
	public String getName() {
		return names.get(names.size() - 1);
	}
	
	public String getJsonName() {
		return originalNames.get(originalNames.size() - 1);
	}

  public List<String> getNames() {
    return names;
  }

  public List<String> getOriginalNames() {
    return originalNames;
  }
	
	private String namify(String name) {
		StringBuilder typeName = new StringBuilder();
		String normalizedName = name.replaceAll("\\s", "_");
		for (String piece : normalizedName.split("_")) {
			typeName.append(GosuStringUtil.capitalize(piece));
		}
		
		return typeName.toString();
	}
	
	public String join(String joiner) {
	  return join(names, joiner);
	}
	
	public static String join(List<String> list, String joiner) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        builder.append(joiner);
      }
      builder.append(list.get(i));
    }
    return builder.toString();
  }
  
  @Override
  public String toString() {
    return join(".");
  }
}
