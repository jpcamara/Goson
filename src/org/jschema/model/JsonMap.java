package org.jschema.model;

import gw.lang.reflect.IType;

import java.util.*;

public class JsonMap extends JsonObject implements Map<String, Object> {

  private Map<String, Object> _backingMap;

  public JsonMap() {
    this(null);
  }

  public JsonMap(IType explicitType) {
    super(explicitType);
    _backingMap = new LinkedHashMap<String, Object>();
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
  public Object get(Object o) {
    return _backingMap.get(o);
  }

  @Override
  public Object put(String key, Object value) {
    Object evicted = _backingMap.put(key, value);
    setThisAsParentFor(value);
    nullParentIfNotSame(value, evicted);
    return evicted;
  }

  @Override
  public Object remove(Object o) {
    Object remove = _backingMap.remove(o);
    setNullAsParentFor(remove);
    return remove;
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> map) {
    Set<? extends Entry<? extends String, ? extends Object>> entries = map.entrySet();
    for (Entry<? extends String, ? extends Object> entry : entries) {
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
  public Collection<Object> values() {
    return (Collection<Object>) new JsonCollection(null, _backingMap.values()).withRealOwner(getRealOwner());
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
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
}
