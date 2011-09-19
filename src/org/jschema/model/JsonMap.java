package org.jschema.model;

import gw.lang.reflect.IType;

import java.math.BigDecimal;
import java.util.*;

public class JsonMap<T> extends JsonObject implements Map<String, T> {

  private Map<String, T> _backingMap;

  public JsonMap() {
    this(null);
  }

  public JsonMap(IType explicitType) {
    super(explicitType);
    _backingMap = new LinkedHashMap<String, T>();
  }

  @Override
  public int size() {
    return _backingMap.size();
  }

  @Override
  public boolean isEmpty() {
    return _backingMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object o) {
    return _backingMap.containsKey(o);
  }

  @Override
  public boolean containsValue(Object o) {
    return _backingMap.containsValue(o);
  }

  @Override
  public T get(Object o) {
    return _backingMap.get(o);
  }

  /* Typed getters */

  public JsonMap getMap(String name) {
    return (JsonMap) get(name);
  }

  public JsonList getList(String name) {
    return (JsonList) get(name);
  }

  public String getString(String name) {
    return (String) get(name);
  }

  public Number getNumber(String name) {
    return (Number) get(name);
  }

  public BigDecimal getDecimal(String name) {
    return (BigDecimal) get(name);
  }

  public Long getInt(String name) {
    return (Long) get(name);
  }

  public Boolean getBoolean(String name) {
    return (Boolean) get(name);
  }

  @Override
  public T put(String key, T value) {
    T evicted = _backingMap.put(key, value);
    setThisAsParentFor(value);
    nullParentIfNotSame(value, evicted);
    return evicted;
  }

  @Override
  public T remove(Object o) {
    T remove = _backingMap.remove(o);
    setNullAsParentFor(remove);
    return remove;
  }

  @Override
  public void putAll(Map<? extends String, ? extends T> map) {
    Set<? extends Entry<? extends String, ? extends T>> entries = map.entrySet();
    for (Entry<? extends String, ? extends T> entry : entries) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    for (Object value : _backingMap.values()) {
      setNullAsParentFor(value);
    }
    _backingMap.clear();
  }

  @Override
  public Set<String> keySet() {
    return (Set) new JsonSet(null, _backingMap.keySet()).withValueConverter(new ValueConverter() {
      @Override
      public Object convert(Object value) {
        return JsonMap.this.get(value);
      }
    }).withRealOwner(getRealOwner());
  }

  @Override
  public Collection<T> values() {
    return (Collection<T>) new JsonCollection(null, _backingMap.values()).withRealOwner(getRealOwner());
  }

  @Override
  public Set<Entry<String, T>> entrySet() {
    return (Set) new JsonSet(null, _backingMap.entrySet()).withValueConverter(new ValueConverter() {
      @Override
      public Object convert(Object value) {
        return ((Map.Entry) value).getValue();
      }
    }).withRealOwner(getRealOwner());
  }

  @Override
  public int hashCode() {
    return _backingMap.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JsonMap jsonMap = (JsonMap) o;
    if (_backingMap != null ? !_backingMap.equals(jsonMap._backingMap) : jsonMap._backingMap != null) return false;
    return true;
  }

  @Override
  public String toString() {
    return _backingMap.toString();
  }
}
