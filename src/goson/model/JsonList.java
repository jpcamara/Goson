package org.jschema.model;

import gw.lang.reflect.IType;

import java.math.BigDecimal;
import java.util.*;

public class JsonList<T> extends JsonCollection<T> implements List<T> {

  List<T> _backingList;

  public JsonList() {
    this(null, new ArrayList<T>());
  }

  public JsonList(List<T> backingList) {
    this(null, backingList);
  }

  public JsonList(IType explicitType) {
    this(explicitType, new ArrayList<T>());
  }

  JsonList(IType explicitType, List<T> backingList) {
    super(explicitType, backingList);
    _backingList = backingList;
  }

  @Override
  public boolean addAll(int i, Collection<? extends T> objects) {
    boolean b = _backingList.addAll(i, objects);
    for (Object object : objects) {
      setThisAsParentFor(object);
    }
    return b;
  }

  @Override
  public T get(int i) {
    return _backingList.get(i);
  }

  public JsonMap getMap(int i) {
    return (JsonMap) get(i);
  }

  public JsonList getList(int i) {
    return (JsonList) get(i);
  }

  public String getString(int i) {
    return (String) get(i);
  }

  public Number getNumber(int i) {
    return (Number) get(i);
  }

  public BigDecimal getDecimal(int i) {
    return (BigDecimal) get(i);
  }

  public Long getInt(int i) {
    return (Long) get(i);
  }

  public Boolean getBoolean(int i) {
    return (Boolean) get(i);
  }

  @Override
  public T set(int i, T o) {
    T evicted = _backingList.set(i, o);
    setThisAsParentFor(o);
    nullParentIfNotSame(o, evicted);
    return evicted;
  }

  @Override
  public void add(int i, T o) {
    _backingList.add(i, o);
    setThisAsParentFor(o);
  }

  @Override
  public T remove(int i) {
    T remove = _backingList.remove(i);
    setNullAsParentFor(remove);
    return remove;
  }

  @Override
  public int indexOf(Object o) {
    return _backingList.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return _backingList.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return (ListIterator<T>) new JsonListIterator<T>(_backingList.listIterator()).withValueConverter(getValueConverter()).withRealOwner(getRealOwner());
  }

  @Override
  public ListIterator<T> listIterator(int i) {
    return (ListIterator<T>) new JsonListIterator<T>(_backingList.listIterator()).withValueConverter(getValueConverter()).withRealOwner(getRealOwner());
  }

  @Override
  public List<T> subList(int i, int i1) {
    return (List<T>) new JsonList<T>(getIntrinsicType(), _backingList.subList(i, i1)).withValueConverter(getValueConverter()).withRealOwner(getRealOwner());
  }

  @Override
  public String toString() {
    return _backingList.toString();
  }
}
