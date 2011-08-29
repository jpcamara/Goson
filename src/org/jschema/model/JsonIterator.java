package org.jschema.model;

import java.util.Iterator;

public class JsonIterator extends JsonObject implements Iterator<Object> {
  private Iterator<Object> _backingIterator;
  private Object _current;

  public JsonIterator(Iterator<Object> iterator) {
    super(null);
    _backingIterator = iterator;
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
  public void remove() {
    setNullAsParentFor(_current);
    _backingIterator.remove();
  }
}
