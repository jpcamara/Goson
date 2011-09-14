package org.jschema.rpc;

import gw.lang.reflect.IType;
import gw.lang.reflect.gs.IGosuObject;

public class CustomRPCInstance implements IGosuObject {

  private IType _ownersType;
  private RPCConfig _config;
  private String _url;
  private boolean _includeNulls;

  public CustomRPCInstance(IType ownersType, RPCCallHandler handler, String url, HttpMethod method,
                           Boolean includeNulls, RPCLoggerCallback logger, RPCInvocationWrapper wrapper) {
    _ownersType = ownersType;
    _config = new RPCConfig();
    if (handler != null) {
      _config.setCallHandler(handler);
    }
    if (method != null) {
      _config.setMethod(method);
    }
    if (logger != null) {
      _config.setLogger(logger);
    }
    if (wrapper != null) {
      _config.setWrapper(wrapper);
    }
    _url = url;
    _includeNulls = Boolean.TRUE.equals(includeNulls);
  }

  @Override
  public IType getIntrinsicType() {
    return _ownersType;
  }

  public RPCConfig getConfig() {
    return _config;
  }

  public String getUrl() {
    return _url;
  }

  public boolean shouldIncludeNulls() {
    return _includeNulls;
  }
}
