package org.jschema.rpc;

public class RPCDefaults {

  private static RPCCallHandler DEFAULT_CALL_HANDLER = new SimpleRPCCallHandler();

  private static HttpMethod DEFAULT_METHOD = HttpMethod.POST;

  public static RPCCallHandler getCallHandler() {
    return DEFAULT_CALL_HANDLER;
  }

  public static void setCallHandler(RPCCallHandler handler) {
    RPCDefaults.DEFAULT_CALL_HANDLER = handler;
  }

  public static HttpMethod getDefaultMethod() {
    return DEFAULT_METHOD;
  }

  public static void setDefaultMethod(HttpMethod method) {
    RPCDefaults.DEFAULT_METHOD = method;
  }
}
