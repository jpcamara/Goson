package org.jschema.typeloader;

import gw.lang.reflect.ITypeLoader;

import java.util.List;
import java.util.Map;

public class JSchemaRPCType extends JSchemaTypeBase {

  private Map _object;

  public JSchemaRPCType(String name, ITypeLoader typeloader, Object object) {
    super(name, typeloader, object);
    _object = (Map) object;
  }

  @Override
  protected JSchemaRPCTypeInfo initTypeInfo(Object object) {
    return new JSchemaRPCTypeInfo(JSchemaRPCType.this);
  }

  public List<Map> getFunctions() {
    return (List<Map>) _object.get("functions");
  }
}
