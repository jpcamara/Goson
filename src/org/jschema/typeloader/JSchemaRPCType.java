package org.jschema.typeloader;

import gw.lang.reflect.ITypeLoader;

public class JSchemaRPCType extends JSchemaTypeBase {

  public JSchemaRPCType(String name, ITypeLoader typeloader, Object object) {
    super(name, typeloader, object);
  }

  @Override
  protected JsonTypeInfo initTypeInfo(Object object) {
    return null;
  }
}
