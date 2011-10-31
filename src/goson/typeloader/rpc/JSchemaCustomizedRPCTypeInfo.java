package org.jschema.typeloader.rpc;

import org.jschema.rpc.CustomRPCInstance;
import org.jschema.rpc.RPCConfig;

import java.util.*;

public class JSchemaCustomizedRPCTypeInfo extends JSchemaRPCTypeInfoBase
{
  public JSchemaCustomizedRPCTypeInfo(JSchemaCustomizedRPCType owner) {
    super(owner);
  }

  @Override
  protected boolean areRPCMethodsStatic() {
    return false;
  }

  protected String getRootTypeName() {
    String name = getOwnersType().getName();
    return name.substring(0, name.length() - JSchemaCustomizedRPCType.TYPE_SUFFIX.length());
  }

  @Override
  protected boolean includeNulls(Object ctx) {
    CustomRPCInstance customRPCInstance = (CustomRPCInstance) ctx;
    return customRPCInstance.shouldIncludeNulls();
  }

  @Override
  protected String handleRPCMethodInvocation(Object ctx, String method, Map<String, String> argsMap) {
    CustomRPCInstance customRPCInstance = (CustomRPCInstance) ctx;
    RPCConfig config = customRPCInstance.getConfig();
    String url = customRPCInstance.getUrl();
    if (url == null || "".equals(url)) {
      url = getOwnersType().getDefaultURL();
    }
    url = url + "/" + method;
    return handleRPCMethodInvocationWithConfig(config, url, argsMap);
  }
}
