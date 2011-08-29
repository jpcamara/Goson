package org.jschema.model;

import gw.lang.reflect.IType;

import java.util.*;

public class JsonList extends JsonCollection implements List<Object> {

  List<Object> _backingList;

  public JsonList() {
    this(null);
  }

  public JsonList(IType explicitType) {
    this(explicitType, new ArrayList<Object>());
  }

  JsonList(IType explicitType, List<Object> backingList) {
    super(explicitType, backingList);
    _backingList = backingList;
  }

  @Override
  public boolean addAll(int i, Collection<? extends Object> objects) {
    boolean b = _backingList.addAll(i, objects);
    for (Object object : objects) {
      setThisAsParentFor(object);
    }
    return b;
  }

  @Override
  public Object get(int i) {
    return _backingList.get(i);
  }

  @Override
  public Object set(int i, Object o) {
    Object evicted = _backingList.set(i, o);
    setThisAsParentFor(o);
    nullParentIfNotSame(o, evicted);
    return evicted;
  }

  @Override
  public void add(int i, Object o) {
    _backingList.add(i, o);
    setThisAsParentFor(o);
  }

  @Override
  public Object remove(int i) {
    Object remove = _backingList.remove(i);
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
  public ListIterator<Object> listIterator() {
    return (ListIterator<Object>) new JsonListIterator(_backingList.listIterator()).withValueConverter(getValueConverter()).withRealOwner(getRealOwner());
  }

  @Override
  public ListIterator<Object> listIterator(int i) {
    return (ListIterator<Object>) new JsonListIterator(_backingList.listIterator()).withValueConverter(getValueConverter()).withRealOwner(getRealOwner());
  }

  @Override
  public List<Object> subList(int i, int i1) {
    return (List<Object>) new JsonList(getIntrinsicType(), _backingList.subList(i, i1)).withValueConverter(getValueConverter()).withRealOwner(getRealOwner());
  }
}
