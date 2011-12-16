package org.jschema.typeloader.rpc;

import gw.lang.GosuShop;
import gw.lang.reflect.*;
import org.jschema.rpc.*;

import java.util.List;
import java.util.Map;

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
          .withDefValue(GosuShop.getNullExpressionInstance()),
        new ParameterInfoBuilder()
          .withName("url")
          .withType(TypeSystem.get(String.class))
          .withDefValue(GosuShop.getNullExpressionInstance()),
        new ParameterInfoBuilder()
          .withName("method")
          .withType(TypeSystem.get(HttpMethod.class))
          .withDefValue(GosuShop.getNullExpressionInstance()),
        new ParameterInfoBuilder()
          .withName("includeNulls")
          .withType(TypeSystem.get(Boolean.class))
          .withDefValue(GosuShop.getNullExpressionInstance()),
        new ParameterInfoBuilder()
          .withName("logger")
          .withType(TypeSystem.get(RPCLoggerCallback.class))
          .withDefValue(GosuShop.getNullExpressionInstance()),
        new ParameterInfoBuilder()
          .withName("wrapper")
          .withType(TypeSystem.get(RPCInvocationWrapper.class))
          .withDefValue(GosuShop.getNullExpressionInstance())
      )
      .withReturnType(TypeSystem.getByFullName(getOwnersType().getName() + JSchemaCustomizedRPCType.TYPE_SUFFIX))
      .withCallHandler(new IMethodCallHandler() {
        @Override
        public Object handleCall(Object ctx, Object... args) {
          return new CustomRPCInstance(getOwnersType(), (RPCCallHandler) args[0], (String) args[1], (HttpMethod) args[2],
            (Boolean) args[3], (RPCLoggerCallback) args[4], (RPCInvocationWrapper) args[5] );
        }
      })
      .build(this));
    return methods;
  }

  protected String getRootTypeName() {
    return getOwnersType().getName();
  }

  @Override
  protected String handleRPCMethodInvocation(Object ctx, String method, final Map<String, String> argsMap) {
    final RPCConfig config = new RPCConfig();
    final String url = getOwnersType().getDefaultURL() + "/" + method;
    return handleRPCMethodInvocationWithConfig(config, url, argsMap);
  }
}
