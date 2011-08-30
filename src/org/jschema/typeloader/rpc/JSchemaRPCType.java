package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;

import java.util.Map;

public class JSchemaRPCType extends JSchemaRPCTypeBase  implements IJSchemaRPCType {

  private Map<String, Map<String, Object>> _defaultValues;

  public JSchemaRPCType(String name, ITypeLoader typeloader, Object object, Map<String, String> typeDefs, Map<String, Map<String, Object>> defaultValues) {
    super(name, typeloader, object, typeDefs);
    _defaultValues = defaultValues;
  }

  @Override
  protected JSchemaRPCTypeInfo initTypeInfo(Object object) {
    return new JSchemaRPCTypeInfo(JSchemaRPCType.this);
  }

  @Override
  public Object getDefaultValue(String method, String parameterName) {
    Map<String, Object> methodDefaults = _defaultValues.get(method);
    if (methodDefaults == null) {
      return null;
    } else {
      return methodDefaults.get(parameterName);
    }
  }
}
