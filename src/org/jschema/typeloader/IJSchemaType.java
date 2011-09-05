package org.jschema.typeloader;

import gw.internal.ext.org.mortbay.jetty.HttpFields;
import gw.lang.parser.IHasInnerClass;
import gw.lang.reflect.IType;

import java.util.Map;

public interface IJSchemaType extends IType, IHasInnerClass {

  public void addInnerClass(IType innerClass);

  Map<String, String> getTypeDefs();

  IType getSelfType();

  IType resolveInnerType(String fqn, Object value);

  IType getTypeForJsonSlot(String key);

  String getJsonSlotForPropertyName(String propName);
}
