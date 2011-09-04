package org.jschema.model;

import gw.lang.reflect.IType;

import java.util.*;

public class JsonSet<T> extends JsonCollection<T> implements Set<T> {
  JsonSet(IType explicitType, Set<T> backingSet) {
    super(explicitType, backingSet);
  }
}
