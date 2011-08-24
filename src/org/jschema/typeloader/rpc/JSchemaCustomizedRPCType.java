package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;

public class JSchemaCustomizedRPCType extends JSchemaRPCTypeBase {

  public static final String TYPE_SUFFIX = ".CustomInstance";

  public JSchemaCustomizedRPCType(String name, ITypeLoader typeloader, Object object) {
    super(name, typeloader, object);
  }

  @Override
  protected JSchemaCustomizedRPCTypeInfo initTypeInfo(Object object) {
    return new JSchemaCustomizedRPCTypeInfo(JSchemaCustomizedRPCType.this);
  }
}
