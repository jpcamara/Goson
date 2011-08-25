package org.jschema.typeloader;

import gw.lang.reflect.ITypeLoader;

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
}
