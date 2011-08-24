package org.jschema.rpc;

public class RPCConfig {

  private RPCCallHandler _callHandler;
  private HttpMethod _method;

  public RPCConfig() {
    _callHandler = RPCDefaults.getCallHandler();
    _method = RPCDefaults.getDefaultMethod();
  }

  public void setCallHandler(RPCCallHandler callHandler) {
    _callHandler = callHandler;
  }

  public RPCCallHandler getCallHandler() {
    return _callHandler;
  }

  public void setMethod(HttpMethod defaultMethod) {
    _method = defaultMethod;
  }

  public HttpMethod getMethod() {
    return _method;
  }
}
