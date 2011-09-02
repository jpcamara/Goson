package org.jschema.typeloader.rpc;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;

import java.util.Map;

public class JSchemaCustomizedRPCType extends JSchemaRPCTypeBase {

  public static final String TYPE_SUFFIX = ".CustomInstance";

  public JSchemaCustomizedRPCType(String name, ITypeLoader typeloader, Object object, Map<String, String> typeDefs,
                            Map<String, Map<String, Object>> defaultValues, String schema) {
    super(name, typeloader, object, typeDefs, defaultValues,  schema);
  }

  @Override
  protected JSchemaCustomizedRPCTypeInfo initTypeInfo(Object object) {
    return new JSchemaCustomizedRPCTypeInfo(JSchemaCustomizedRPCType.this);
  }

  @Override
  public Object getDefaultValue(String method, String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
