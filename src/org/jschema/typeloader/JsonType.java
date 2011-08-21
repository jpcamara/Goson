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

public class JsonType extends TypeBase {
  private static final long serialVersionUID = -8034222055932240161L;

  private ITypeLoader loader;
  private JsonName name;
  private String path;
  private LazyVar<JsonTypeInfo> typeInfo;
  private Logger logger = Logger.getLogger(getClass().getName());

  public JsonType(JsonName name, String path, ITypeLoader typeloader, final Object object) {
    this.name = name;
    this.path = path;
    this.loader = typeloader;
    this.typeInfo = new LazyVar<JsonTypeInfo>() {
      @Override
      protected JsonTypeInfo init() {
        return new JsonTypeInfo(JsonType.this, object);
      }
    };
  }

  @Override
  public String getName() {
    return path + "." + name.join(".");
  }

  @Override
  public String getNamespace() {
    return path;
  }

  @Override
  public String getRelativeName() {
    return name.getName();
  }

  public String getJsonRelativeName() {
    return name.getJsonName();
  }
  
  public JsonName getNameInfo() {
    return name;
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return typeInfo.get();
  }

  @Override
  public ITypeLoader getTypeLoader() {
    return loader;
  }

  @Override
  public List<? extends IType> getInterfaces() {
    return Collections.emptyList();
  }

  @Override
  public IType getSupertype() {
    return IJavaType.OBJECT;
  }

  @Override
  public boolean isParameterizedType() {
    return false;
  }

  public String toString() {
    return name.join(".");
  }
}