package org.jschema.typeloader;

import gw.lang.reflect.IEnumType;
import gw.lang.reflect.IEnumValue;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.gs.IGosuObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JSchemaEnumType extends JSchemaType implements IEnumType {

  private List<IEnumValue> values = new ArrayList<IEnumValue>();

  public JSchemaEnumType(String name, ITypeLoader typeloader, final Object object) {
    super(name, typeloader, object, Collections.EMPTY_MAP);
    Object obj = ((Map)object).get("enum");
    if (obj == null || !(obj instanceof List)) {
      throw new RuntimeException("An enum must be an array of values.");
    }
    for (Object o : (List) obj) {
      values.add(new JsonEnumValue((String)o));
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
  
  public class JsonEnumValue implements IEnumValue, IGosuObject {
    public String code;
    public String displayName;
    public String originalValue;

    public JsonEnumValue(String value) {
      originalValue = value;
      code = JSchemaEnumType.enumify(value);
      displayName = code;
    }
    
    @Override
    public Object getValue() {
      return originalValue;
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
      return JSchemaEnumType.this;
  	}
  }
}
