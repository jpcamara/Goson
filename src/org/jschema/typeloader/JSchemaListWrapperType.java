package org.jschema.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeSystem;
import org.jschema.model.JsonList;

import java.util.Collections;
import java.util.Map;

public class JSchemaListWrapperType extends JSchemaTypeBase implements IJsonType {

  private int _depth;

  public JSchemaListWrapperType(String name, ITypeLoader typeloader, final int depth, Object o) {
    super(name, typeloader, o);
    _depth = depth;
  }

  public Map<String, String> getTypeDefs() {
    return Collections.emptyMap();
  }

  protected JSchemaTypeInfo initTypeInfo(Object object) {
    return new JSchemaTypeInfo(JSchemaListWrapperType.this, object);
  }

  @Override
  public IType getTypeForJsonSlot(String key) {
    return getTypeInfo().getTypeForJsonSlot(key);
  }

  @Override
  public String getJsonSlotForPropertyName(String propName) {
    return getTypeInfo().getJsonSlotForPropertyName(propName);
  }

  @Override
  public JSchemaTypeInfo getTypeInfo() {
    return (JSchemaTypeInfo) super.getTypeInfo();
  }

  public IType getSelfType() {
    IType byFullNameIfValid = TypeSystem.getByFullNameIfValid(getNamespace());
    if (byFullNameIfValid instanceof JSchemaListWrapperType) {
      return ((JSchemaListWrapperType) byFullNameIfValid).getSelfType();
    } else if(byFullNameIfValid == null) {
      return this;
    } else {
      return null;
    }
  }

  public IType getWrappedType() {
    IType type = TypeSystem.getByFullName(getName() + ".Element");
    int depth = _depth;
    while (depth > 0) {
      type = TypeSystem.get(JsonList.class).getParameterizedType(type);
      depth--;
    }
    return type;
  }
}
