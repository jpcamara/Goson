package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;
import org.jschema.typeloader.JSchemaTypeBase;

import java.util.List;
import java.util.Map;

public class JSchemaRPCType extends JSchemaRPCTypeBase {
  public JSchemaRPCType(String name, ITypeLoader typeloader, Object object) {
    super(name, typeloader, object);
  }

  @Override
  protected JSchemaRPCTypeInfo initTypeInfo(Object object) {
    return new JSchemaRPCTypeInfo(JSchemaRPCType.this);
  }
}
