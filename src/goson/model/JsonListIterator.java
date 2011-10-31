package org.jschema.model;

import java.util.ListIterator;

public class JsonListIterator<T> extends JsonObject implements ListIterator<T> {

  private ListIterator<T> _backingIterator;
  private T _current;

  public JsonListIterator(ListIterator<T> backingIterator) {
    super(null);
    _backingIterator = backingIterator;
  }

  @Override
  public boolean hasNext() {
    return _backingIterator.hasNext();
  }

  @Override
  public T next() {
    _current = _backingIterator.next();
    return _current;
  }

  @Override
  public boolean hasPrevious() {
    return _backingIterator.hasPrevious();
  }

  @Override
  public T previous() {
    _current = _backingIterator.previous();
    return _current;
  }

  @Override
  public int nextIndex() {
    return _backingIterator.nextIndex();
  }

  @Override
  public int previousIndex() {
    return _backingIterator.previousIndex();
  }

  @Override
  public void remove() {
    setNullAsParentFor(_current);
    _backingIterator.remove();
  }

  @Override
  public void set(T o) {
    setNullAsParentFor(_current);
    setThisAsParentFor(o);
    _backingIterator.set(o);
  }

  @Override
  public void add(T o) {
    setThisAsParentFor(o);
    _backingIterator.add(o);
  }
}
