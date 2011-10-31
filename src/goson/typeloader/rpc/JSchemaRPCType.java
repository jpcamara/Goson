package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;

import java.util.Map;

public class JSchemaRPCType extends JSchemaRPCTypeBase implements IJSchemaRPCType {

  public JSchemaRPCType(String name, ITypeLoader typeloader, Object object, Map<String, String> typeDefs,
                            Map<String, Map<String, Object>> defaultValues, String schema) {
    super(name, typeloader, object, typeDefs, defaultValues,  schema);
  }

  @Override
  protected JSchemaRPCTypeInfo initTypeInfo(Object object) {
    return new JSchemaRPCTypeInfo(JSchemaRPCType.this);
  }
}
