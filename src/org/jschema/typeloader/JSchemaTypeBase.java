package org.jschema.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LazyVar;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public abstract class JSchemaTypeBase extends TypeBase {
  private static final long serialVersionUID = -8034222055932240161L;

  private String relativeName;
  private String packageName;
  private String fullName;
  private ITypeLoader loader;
  private LazyVar<ITypeInfo> typeInfo;
  private Logger logger = Logger.getLogger(getClass().getName());

  public JSchemaTypeBase(String name, ITypeLoader typeloader, final Object object) {
    this.relativeName = GosuClassUtil.getShortClassName(name);
    this.packageName = GosuClassUtil.getPackage(name);
    this.fullName = name;
    this.loader = typeloader;
    this.typeInfo = new LazyVar<ITypeInfo>() {
      @Override
      protected ITypeInfo init() {
        return initTypeInfo(object);
      }
    };
  }

  protected abstract ITypeInfo initTypeInfo(Object object);

  @Override
  public String getName() {
    return fullName;
  }

  @Override
  public String getNamespace() {
    return packageName;
  }

  @Override
  public String getRelativeName() {
    return relativeName;
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
    return getName();
  }
}
