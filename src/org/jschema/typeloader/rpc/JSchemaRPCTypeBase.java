package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;
import org.jschema.typeloader.JSchemaTypeBase;

import java.util.List;
import java.util.Map;

abstract class JSchemaRPCTypeBase extends JSchemaTypeBase {

  private Map _object;

  public JSchemaRPCTypeBase(String name, ITypeLoader typeloader, Object object) {
    super(name, typeloader, object);
    _object = (Map) object;
  }

  public List<Map> getFunctions() {
    return (List<Map>) _object.get("functions");
  }

  public String getDefaultURL() {
    return (String) _object.get("url");
  }
}
