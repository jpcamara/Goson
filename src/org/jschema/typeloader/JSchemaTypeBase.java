package org.jschema.typeloader;

import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LazyVar;
import org.jschema.util.JSchemaUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class JSchemaTypeBase extends TypeBase implements IJSchemaType {
  private static final long serialVersionUID = -8034222055932240161L;

  private static final Map<String, IJavaType> TYPES = new HashMap<String, IJavaType>();
  static {
    TYPES.put("bigdecimal", IJavaType.BIGDECIMAL);
    TYPES.put("biginteger", IJavaType.BIGINTEGER);
    TYPES.put("decimal", IJavaType.DOUBLE);
    TYPES.put("integer", IJavaType.INTEGER);
    TYPES.put("string", IJavaType.STRING);
    TYPES.put("date", IJavaType.DATE);
    TYPES.put("boolean", IJavaType.BOOLEAN);
    TYPES.put("enum", IJavaType.ENUM);
    TYPES.put("map_of", IJavaType.MAP);
    TYPES.put("object", IJavaType.OBJECT);
  }

  private String relativeName;
  private String packageName;
  private String fullName;
  private ITypeLoader loader;
  private LazyVar<ITypeInfo> typeInfo;
  private Logger logger = Logger.getLogger(getClass().getName());
  private Map<String, IType> _innerClasses;

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
    _innerClasses = new HashMap<String, IType>();
  }

  protected abstract ITypeInfo initTypeInfo(Object object);

  @Override
  public IType getInnerClass(CharSequence strTypeName) {
    return _innerClasses.get(strTypeName.toString());
  }

  public void addInnerClass(IType innerClass) {
    _innerClasses.put(innerClass.getRelativeName(), innerClass);
  }

  @Override
  public IType resolveInnerType(String fqn, Object value) {
    if (value instanceof String) {
      if ("self".equals(value)) {
        return getSelfType();
      }
      IJavaType javaType = TYPES.get(value);
      if (javaType != null) {
        return javaType;
      }
      String typeDefType = getTypeDefs().get(value);
      if (typeDefType != null) {
        return TypeSystem.getByFullName(typeDefType);
      }
    } else if (value instanceof Map) {
      Map map = (Map) value;
      if (map.size() == 1 && map.containsKey(JSchemaUtils.JSCHEMA_MAP_KEY)) {
        return IJavaType.MAP.getParameterizedType(IJavaType.STRING, resolveInnerType(fqn, map.get("map_of")));
      } else {
        return TypeSystem.getByFullName(fqn);
      }
    } else if (value instanceof List) {
      return IJavaType.LIST.getParameterizedType(resolveInnerType(fqn, ((List) value).get(0)));
    }
    //TODO cgross - this should be a verification error
    return IJavaType.OBJECT;
  }

  @Override
  public IType getTypeForJsonSlot(String key) {
    return null;
  }

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
