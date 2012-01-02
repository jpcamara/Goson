package org.jschema.typeloader;

import gw.lang.reflect.*;
import gw.lang.reflect.gs.IGosuObject;
import org.jschema.util.JSchemaUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class JSchemaEnumType extends JSchemaType implements IJSchemaEnumType {

  private List<IEnumValue> values = new ArrayList<IEnumValue>();

  public JSchemaEnumType(String name, ITypeLoader typeloader, final Object object) {
    super(name, typeloader, object, new HashMap<String, String>());
    Object obj = ((Map)object).get(JSchemaUtils.JSCHEMA_ENUM_KEY);
    if (obj == null || !(obj instanceof List)) {
      throw new RuntimeException("An enum must be an array of values.");
    }
    for (Object o : (List) obj) {
      values.add(new JsonEnumValue((String)o));
    }
  }

  @Override
  public boolean isEnum() {
    return true;
  }

  @Override
  public List<IEnumValue> getEnumValues() {
    return values;
  }

  @Override
  public IEnumValue getEnumValue( String strName ) {
    for (IEnumValue value : values) {
      if (value.getCode().equals(strName)) {
        return value;
      }
    }
    return null;
  }

  @Override
  public List<String> getEnumConstants() {
    ArrayList<String> strings = new ArrayList<String>();
    for (IEnumValue iEnumValue : getEnumValues()) {
      strings.add(iEnumValue.getCode());
    }
    return strings;
  }

  public static String enumify(String original) {
    return original.replaceAll("\\s", "_").toUpperCase();
  }

  @Override
  public boolean isAssignableFrom(IType type) {
    if (super.isAssignableFrom(type)) {
      return true;
    } else {
      return isEquivalentEnum(type);
    }
  }

  private boolean isEquivalentEnum(IType type) {
    if (type instanceof IJSchemaEnumType) {
      IJSchemaEnumType otherEnum = (IJSchemaEnumType) type;
      List<IEnumValue> otherValues = otherEnum.getEnumValues();
      for (IEnumValue otherValue : otherValues) {
        if (!hasValue(otherValue.getValue())) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private boolean hasValue(Object value) {
    for (IEnumValue iEnumValue : values) {
      if (value.equals(iEnumValue.getValue())) {
        return true;
      }
    }
    return false;
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

    @Override
    public String toString() {
      return getValue() + "";
    }
  }
}
