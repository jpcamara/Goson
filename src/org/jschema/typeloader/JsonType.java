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
import java.util.logging.Logger;

public class JsonType extends JSchemaTypeBase implements IJsonType {
  public JsonType(String name, ITypeLoader typeloader, final Object object) {
    super(name, typeloader, object);
  }

  protected JsonTypeInfo initTypeInfo(Object object) {
    return new JsonTypeInfo(JsonType.this, object);
  }
}
