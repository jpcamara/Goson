package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;
import org.jschema.typeloader.JSchemaTypeBase;

import java.util.List;
import java.util.Map;

abstract class JSchemaRPCTypeBase extends JSchemaTypeBase {

  private Map _object;
  private Map<String, String> _typeDefs;

  public JSchemaRPCTypeBase(String name, ITypeLoader typeloader, Object object, Map<String, String> typeDefs) {
    super(name, typeloader, object);
    _object = (Map) object;
    _typeDefs = typeDefs;
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
}
