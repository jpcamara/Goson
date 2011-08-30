package org.jschema.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeSystem;

import java.util.Map;

public class JSchemaType extends JSchemaTypeBase implements IJsonType {

  private Map<String, String> _typeDefs;

  public JSchemaType(String name, ITypeLoader typeloader, final Object object, Map<String, String> typeDefs) {
    super(name, typeloader, object);
    _typeDefs = typeDefs;
  }

  public Map<String, String> getTypeDefs() {
    return _typeDefs;
  }

  protected JSchemaTypeInfo initTypeInfo(Object object) {
    return new JSchemaTypeInfo(JSchemaType.this, object);
  }

  public IType getSelfType() {
    IType byFullNameIfValid = TypeSystem.getByFullNameIfValid(getNamespace());
    if (byFullNameIfValid instanceof JSchemaType) {
      return ((JSchemaType) byFullNameIfValid).getSelfType();
    } else if(byFullNameIfValid == null) {
      return this;
    } else {
      return null;
    }
  }
}
