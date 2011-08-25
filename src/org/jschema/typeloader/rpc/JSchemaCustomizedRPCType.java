package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;

import java.util.Map;

public class JSchemaCustomizedRPCType extends JSchemaRPCTypeBase {

  public static final String TYPE_SUFFIX = ".CustomInstance";

  public JSchemaCustomizedRPCType(String name, ITypeLoader typeloader, Object object, Map<String, String> typeDefs) {
    super(name, typeloader, object, typeDefs);
  }

  @Override
  protected JSchemaCustomizedRPCTypeInfo initTypeInfo(Object object) {
    return new JSchemaCustomizedRPCTypeInfo(JSchemaCustomizedRPCType.this);
  }
}
