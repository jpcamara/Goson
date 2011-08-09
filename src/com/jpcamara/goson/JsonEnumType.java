package com.jpcamara.goson;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.IEnumType;
import gw.lang.reflect.IEnumValue;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.java.IJavaType;
import gw.util.concurrent.LazyVar;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuObject;

import org.json.JSONArray;
import org.json.JSONException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JsonEnumType extends JsonType implements IEnumType {
  private List<IEnumValue> values = new ArrayList<IEnumValue>();

  public JsonEnumType(JsonName name, String path, ITypeLoader typeloader,
    final JsonParser object) {
    super(name, path, typeloader, object);
    Object obj = object.get("enum");
    if (obj == null || !(obj instanceof JSONArray)) {
      throw new RuntimeException("An enum must be an array of values.");
    }
    JSONArray arr = (JSONArray)obj;
    try {
      for (int i = 0; i < arr.length(); i++) {
        values.add(new JsonEnumValue((String)arr.get(i)));
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public List<IEnumValue> getEnumValues() {
    return values;
  }

  @Override
  public IEnumValue getEnumValue( String strName ) {
    for (IEnumValue value : values) {
      if (value.getDisplayName().equals(strName)) {
        return value;
      }
    }
    return null;
  }
  
  public static String enumify(String original) {
    return original.replaceAll("\\s", "_").toUpperCase();
  }

/*Not used for enum
  @Override
  public IType getSupertype() {
    return TypeSystem.get(Enum.class);
  }

  @Override
  public boolean isEnum() {
    return true;
  }*/
  
  public class JsonEnumValue implements IEnumValue, IGosuObject {
    public String code;
    public String displayName;
    public String originalValue;

    public JsonEnumValue(String value) {
      originalValue = value;
      code = JsonEnumType.enumify(value);
      displayName = code;
    }
    
    public String getJsonCode() {
      return originalValue;
    }

    //What is this for?
    @Override
    public Object getValue() {
      throw new NotImplementedException();
    }

    @Override
    public String getCode() {
      return code;
    }

    @Override
    public int getOrdinal() {
      throw new NotImplementedException();
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }
    
    @Override
  	public IType getIntrinsicType() {
      return JsonEnumType.this;
  	}
  }
}