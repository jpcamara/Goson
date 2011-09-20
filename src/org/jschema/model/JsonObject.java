package org.jschema.model;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuObject;
import org.jschema.util.JSchemaUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonObject implements IGosuObject{

  private IType _explicitType;
  private JsonObject _parent;
  private JsonObject _realOwner;
  private ValueConverter _valueConverter;

  public JsonObject(IType explicitType) {
    _explicitType = explicitType;
    _realOwner = this;
  }

  @Override
  public IType getIntrinsicType() {
    if (_explicitType != null) {
      return _explicitType;
    } else {
      return TypeSystem.get(this.getClass());
    }
  }

  public JsonObject withRealOwner(JsonObject realOwner) {
    _realOwner = realOwner;
    return this;
  }

  public JsonObject withValueConverter(ValueConverter valueConverter) {
    _valueConverter = valueConverter;
    return this;
  }

  public JsonObject getParent() {
    return _parent;
  }

  protected JsonObject getRealOwner() {
    return _realOwner;
  }

  protected ValueConverter getValueConverter() {
    return _valueConverter;
  }

  protected void nullParentIfNotSame(Object value, Object evicted) {
    if (evicted != value) {
      setNullAsParentFor(evicted);
    }
  }

  protected void setNullAsParentFor(Object evicted) {
    evicted = convertValue(evicted);
    if (evicted instanceof JsonObject) {
      JsonObject parent = ((JsonObject) evicted)._parent;
      if (parent == _realOwner) {
        ((JsonObject) evicted)._parent = null;
      }
    }
  }

  private Object convertValue(Object value) {
    if (_valueConverter == null) {
      return value;
    } else {
      return _valueConverter.convert(value);
    }
  }

  protected void setThisAsParentFor(Object jsonObj) {
    if (jsonObj instanceof JsonObject) {
      ((JsonObject) jsonObj)._parent = _realOwner;
    }
  }

  public Iterable getDescendents() {
    return findDescendents(this, new LinkedList(), null);
  }

  public Iterable findDescendents(IType type) {
    return findDescendents(this, new LinkedList(), type);
  }

  private static Iterable findDescendents(Object obj, List ll, IType type) {
    if (type != null) {
      if (type.isAssignableFrom(TypeSystem.getTypeFromObject(obj))) {
        ll.add(obj);
      }
    } else {
      ll.add(obj);
    }
    if (obj instanceof List) {
      for (Object o : ((List) obj)) {
        findDescendents(o, ll, type);
      }
    }
    else if (obj instanceof Map)
    {
      for (Object o : ((Map) obj).values()) {
        findDescendents(o, ll, type);
      }
    }
    return ll;
  }

  public String write() {
    return JSchemaUtils.serializeJson(this);
  }

  public String prettyPrint() {
    return JSchemaUtils.serializeJson(this, 2);
  }

  public String prettyPrint(int indent) {
    return JSchemaUtils.serializeJson(this, indent);
  }

  protected interface ValueConverter {
    Object convert(Object value);
  }
}
