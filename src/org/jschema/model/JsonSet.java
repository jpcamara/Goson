package org.jschema.model;

import gw.lang.reflect.IType;

import java.util.*;

public class JsonSet extends JsonCollection implements Set<Object> {
  JsonSet(IType explicitType, Set backingSet) {
    super(explicitType, backingSet);
  }
}
