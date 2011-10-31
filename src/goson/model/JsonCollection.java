package org.jschema.model;

import gw.lang.reflect.IType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class JsonCollection<T> extends JsonObject implements Collection<T> {

  Collection<T> _backingCollection;

  protected JsonCollection() {
    this(null, new ArrayList<T>());
  }

  protected JsonCollection(IType explicitType, Collection<T> backingCollection) {
    super(explicitType);
    _backingCollection = backingCollection;
  }

  @Override
  public int size() {
    return _backingCollection.size();
  }

  @Override
  public boolean isEmpty() {
    return _backingCollection.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return _backingCollection.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return (Iterator<T>) new JsonIterator<T>(_backingCollection.iterator()).withValueConverter(getValueConverter()).withRealOwner(getRealOwner());
  }

  @Override
  public Object[] toArray() {
    return _backingCollection.toArray();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    return _backingCollection.toArray(ts);
  }

  @Override
  public boolean add(T o) {
    boolean add = _backingCollection.add(o);
    setThisAsParentFor(o);
    return add;
  }

  @Override
  public boolean remove(Object o) {
    setNullAsParentFor(o);
    return _backingCollection.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> objects) {
    return _backingCollection.containsAll(objects);
  }

  @Override
  public boolean addAll(Collection<? extends T> objects) {
    for (Object object : objects) {
      setThisAsParentFor(object);
    }
    return _backingCollection.addAll(objects);
  }

  @Override
  public boolean removeAll(Collection<?> objects) {
    for (Object object : objects) {
      setNullAsParentFor(object);
    }
    return _backingCollection.removeAll(objects);
  }

  @Override
  public boolean retainAll(Collection<?> objects) {
    for (Object o : _backingCollection) {
      if (!objects.contains(o)) {
        setNullAsParentFor(o);
      }
    }
    return _backingCollection.retainAll(objects);
  }

  @Override
  public void clear() {
    for (Object o : _backingCollection) {
      setNullAsParentFor(o);
    }
    _backingCollection.clear();
  }

  @Override
  public int hashCode() {
    return _backingCollection.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JsonCollection that = (JsonCollection) o;
    if (!_backingCollection.equals(that._backingCollection)) return false;
    return true;
  }

}