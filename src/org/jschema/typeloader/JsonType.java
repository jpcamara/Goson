package org.jschema.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.java.IJavaType;
import gw.util.concurrent.LazyVar;
import org.jschema.parser.JSONParser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JsonType extends JSchemaTypeBase implements IJsonType {

  private Map<String, String> _typeDefs;

  public JsonType(String name, ITypeLoader typeloader, final Object object, Map<String, String> typeDefs) {
    super(name, typeloader, object);
    _typeDefs = typeDefs;
  }

  public Map<String, String> getTypeDefs() {
    return _typeDefs;
  }

  protected JsonTypeInfo initTypeInfo(Object object) {
    return new JsonTypeInfo(JsonType.this, object);
  }
}
