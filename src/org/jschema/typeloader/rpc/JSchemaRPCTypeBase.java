package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;
import org.jschema.typeloader.JSchemaTypeBase;

import java.util.List;
import java.util.Map;

abstract class JSchemaRPCTypeBase extends JSchemaTypeBase implements IJSchemaRPCType{

  private Map _object;
  private Map<String, String> _typeDefs;
  private Map<String, Map<String, Object>> _defaultValues;
  private String _schema;

  public JSchemaRPCTypeBase(String name, ITypeLoader typeloader, Object object, Map<String, String> typeDefs,
                            Map<String, Map<String, Object>> defaultValues, String schema) {
    super(name, typeloader, object);
    _object = (Map) object;
    _typeDefs = typeDefs;
    _schema = schema;
    _defaultValues = defaultValues;
  }

  public List<Map> getFunctions() {
    return (List<Map>) _object.get("functions");
  }

  public String getDefaultURL() {
    return (String) _object.get("url");
  }

  public Map<String, String> getTypeDefs() {
    return _typeDefs;
  }

  @Override
  public String getSchemaContent() {
    return _schema;
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
