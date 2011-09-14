package org.jschema.rpc;

public class RPCDefaults {

  private static RPCCallHandler DEFAULT_CALL_HANDLER = new SimpleRPCCallHandler();
  private static HttpMethod DEFAULT_METHOD = HttpMethod.POST;
  private static RPCInvocationWrapper INVOCATION_WRAPPER = null;
  private static RPCInvocationWrapper HANDLER_WRAPPER = null;
  private static RPCLoggerCallback LOGGER_CALLBACK = null;

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

  public static RPCInvocationWrapper getCallWrapper() {
    return RPCDefaults.INVOCATION_WRAPPER;
  }

  public static void setCallWrapper(RPCInvocationWrapper method) {
    RPCDefaults.INVOCATION_WRAPPER = method;
  }

  public static RPCInvocationWrapper getHandlerWrapper() {
    return RPCDefaults.HANDLER_WRAPPER;
  }

  public static void setHandlerWrapper(RPCInvocationWrapper method) {
    RPCDefaults.HANDLER_WRAPPER = method;
  }

  public static RPCLoggerCallback getLogger() {
    return RPCDefaults.LOGGER_CALLBACK;
  }

  public static void setLogger(RPCLoggerCallback method) {
    RPCDefaults.LOGGER_CALLBACK = method;
  }
}
