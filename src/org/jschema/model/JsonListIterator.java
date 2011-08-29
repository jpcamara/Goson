package org.jschema.model;

import java.util.ListIterator;

public class JsonListIterator extends JsonObject implements ListIterator<Object> {

  private ListIterator<Object> _backingIterator;
  private Object _current;

  public JsonListIterator(ListIterator<Object> backingIterator) {
    super(null);
    _backingIterator = backingIterator;
  }

  @Override
  public boolean hasNext() {
    return _backingIterator.hasNext();
  }

  @Override
  public Object next() {
    _current = _backingIterator.next();
    return _current;
  }

  @Override
  public boolean hasPrevious() {
    return _backingIterator.hasPrevious();
  }

  @Override
  public Object previous() {
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
  public void set(Object o) {
    setNullAsParentFor(_current);
    setThisAsParentFor(o);
    _backingIterator.set(o);
  }

  @Override
  public void add(Object o) {
    setThisAsParentFor(o);
    _backingIterator.add(o);
  }
}
