package org.jschema.typeloader.rpc;

import gw.lang.reflect.ITypeLoader;
import org.jschema.typeloader.JSchemaTypeBase;

import java.util.List;
import java.util.Map;

public class JSchemaRPCType extends JSchemaRPCTypeBase  implements IJSchemaRPCType {
  public JSchemaRPCType(String name, ITypeLoader typeloader, Object object, Map<String, String> typeDefs) {
    super(name, typeloader, object, typeDefs);
  }

  @Override
  protected JSchemaRPCTypeInfo initTypeInfo(Object object) {
    return new JSchemaRPCTypeInfo(JSchemaRPCType.this);
  }
}
