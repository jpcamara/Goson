package org.jschema.model;

import java.util.Iterator;

public class JsonIterator<T> extends JsonObject implements Iterator<T> {
  private Iterator<T> _backingIterator;
  private T _current;

  public JsonIterator(Iterator<T> iterator) {
    super(null);
    _backingIterator = iterator;
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
  public void remove() {
    setNullAsParentFor(_current);
    _backingIterator.remove();
  }
}
