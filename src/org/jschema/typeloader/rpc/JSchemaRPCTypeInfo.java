package org.jschema.typeloader.rpc;

import gw.lang.parser.ISymbol;
import gw.lang.reflect.*;
import org.jschema.rpc.CustomRPCInstance;
import org.jschema.rpc.HttpMethod;
import org.jschema.rpc.RPCConfig;
import org.jschema.rpc.RPCCallHandler;

import java.util.*;

public class JSchemaRPCTypeInfo extends JSchemaRPCTypeInfoBase
{
  public JSchemaRPCTypeInfo(JSchemaRPCType owner) {
    super(owner);
  }

  @Override
  protected boolean areRPCMethodsStatic() {
    return true;
  }

  @Override
  protected boolean includeNulls(Object ctx) {
    return false;
  }

  @Override
  protected List<IMethodInfo> buildMethods() {
    List<IMethodInfo> methods = super.buildMethods();
    methods.add(new MethodInfoBuilder()
      .withName("with")
      .withStatic()
      .withParameters(
        new ParameterInfoBuilder()
          .withName("handler")
          .withType(TypeSystem.get(RPCCallHandler.class))
          .withDefValue(ISymbol.NULL_DEFAULT_VALUE),
        new ParameterInfoBuilder()
          .withName("url")
          .withType(TypeSystem.get(String.class))
          .withDefValue(ISymbol.NULL_DEFAULT_VALUE),
        new ParameterInfoBuilder()
          .withName("method")
          .withType(TypeSystem.get(HttpMethod.class))
          .withDefValue(ISymbol.NULL_DEFAULT_VALUE),
        new ParameterInfoBuilder()
          .withName("includeNulls")
          .withType(TypeSystem.get(Boolean.class))
          .withDefValue(ISymbol.NULL_DEFAULT_VALUE)
      )
      .withReturnType(TypeSystem.getByFullName(getOwnersType().getName() + JSchemaCustomizedRPCType.TYPE_SUFFIX))
      .withCallHandler(new IMethodCallHandler() {
        @Override
        public Object handleCall(Object ctx, Object... args) {
          return new CustomRPCInstance(getOwnersType(), (RPCCallHandler) args[0], (String) args[1], (HttpMethod) args[2], (Boolean) args[3]);
        }
      })
      .build(this));
    return methods;
  }

  protected String getRootTypeName() {
    return getOwnersType().getName();
  }

  @Override
  protected String handleRPCMethodInvocation(Object ctx, String method, Map<String, String> argsMap) {
    RPCConfig config = new RPCConfig();
    return config.getCallHandler().handleCall(config.getMethod().name(),
      getOwnersType().getDefaultURL() + "/" + method, argsMap);
  }
}
