package org.jschema.rpc;

public class RPCConfig {

  private RPCCallHandler _callHandler;
  private HttpMethod _method;
  private RPCInvocationWrapper _wrapper;
  private RPCLoggerCallback _logger;

  public RPCConfig() {
    _callHandler = RPCDefaults.getCallHandler();
    _method = RPCDefaults.getDefaultMethod();
    _wrapper = RPCDefaults.getCallWrapper();
    _logger = RPCDefaults.getLogger();
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

  public RPCInvocationWrapper getWrapper() {
    return _wrapper;
  }

  public void setWrapper(RPCInvocationWrapper wrapper) {
    _wrapper = wrapper;
  }

  public RPCLoggerCallback getLogger() {
    return _logger;
  }

  public void setLogger(RPCLoggerCallback logger) {
    _logger = logger;
  }
}
