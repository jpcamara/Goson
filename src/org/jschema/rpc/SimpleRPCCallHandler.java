package org.jschema.rpc;

import java.util.Map;

public class SimpleRPCCallHandler implements RPCCallHandler {
  @Override
  public String handleCall(String method, String url, Map<String, String> args) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
