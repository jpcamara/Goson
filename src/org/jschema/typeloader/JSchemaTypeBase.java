package org.jschema.typeloader;

import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LockingLazyVar;
import org.jschema.model.JsonList;
import org.jschema.model.JsonMap;
import org.jschema.parser.JsonParseError;
import org.jschema.util.JSchemaUtils;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public abstract class JSchemaTypeBase extends TypeBase implements IJSchemaType, IProvidesCustomErrorInfo {
  private static final long serialVersionUID = -8034222055932240161L;

  private static final Map<String, IJavaType> TYPES = new HashMap<String, IJavaType>();
  static {
    TYPES.put("number", JavaTypes.BIG_DECIMAL());
    TYPES.put("int", JavaTypes.LONG());
    TYPES.put("string", JavaTypes.STRING());
    TYPES.put("date", JavaTypes.DATE());
    TYPES.put("uri", (IJavaType) TypeSystem.get(URI.class));
    TYPES.put("boolean", JavaTypes.BOOLEAN());
    TYPES.put("enum", JavaTypes.ENUM());
    TYPES.put("map_of", JavaTypes.MAP());
    TYPES.put("object", JavaTypes.OBJECT());
  }

  private String relativeName;
  private String packageName;
  private String fullName;
  private ITypeLoader loader;
  private LockingLazyVar<ITypeInfo> typeInfo;
  private Logger logger = Logger.getLogger(getClass().getName());
  private Map<String, IType> _innerClasses;
  private List<CustomErrorInfo> _errors;

  public JSchemaTypeBase(String name, ITypeLoader typeloader, final Object object) {
    this.relativeName = GosuClassUtil.getShortClassName(name);
    this.packageName = GosuClassUtil.getPackage(name);
    this.fullName = name;
    this.loader = typeloader;
    this.typeInfo = new LockingLazyVar<ITypeInfo>() {
      @Override
      protected ITypeInfo init() {
        return initTypeInfo(object);
      }
    };
    _innerClasses = new HashMap<String, IType>();
    _errors = new ArrayList<CustomErrorInfo>();
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
  public List<? extends IType> getInnerClasses() {
    return new ArrayList<IType>(_innerClasses.values());
  }

  @Override
  public List<? extends IType> getLoadedInnerClasses() {
    return getInnerClasses();
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
        return TypeSystem.get(JsonMap.class).getParameterizedType(resolveInnerType(fqn, map.get("map_of")));
      } else {
        return TypeSystem.getByFullName(fqn);
      }
    } else if (value instanceof List && ((List) value).size() > 0) {
      return TypeSystem.get(JsonList.class).getParameterizedType(resolveInnerType(fqn, ((List) value).get(0)));
    }
    //TODO cgross - this should be a verification error
    return JavaTypes.OBJECT();
  }

  @Override
  public IType getTypeForJsonSlot(String key) {
    return null;
  }

  public String getJsonSlotForPropertyName(String propName) {
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
    return JavaTypes.OBJECT();
  }

  @Override
  public boolean isParameterizedType() {
    return false;
  }

  public String toString() {
    return getName();
  }

  @Override
  public List<CustomErrorInfo> getCustomErrors() {
    return _errors;
  }

  public void addErrors(List<JsonParseError> errors) {
    if (errors != null) {
      for (JsonParseError error : errors) {
        _errors.add(new CustomErrorInfo(ErrorLevel.ERROR, error.getMessage(), error.getStart(), error.getEnd()));
      }
    }
  }
}
