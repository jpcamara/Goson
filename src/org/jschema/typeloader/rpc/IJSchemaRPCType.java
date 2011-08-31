package org.jschema.typeloader.rpc;

import gw.lang.reflect.IType;

public interface IJSchemaRPCType extends IType {

  Object getDefaultValue(String method, String name);

  String getSchemaContent();
}
