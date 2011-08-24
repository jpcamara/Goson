package org.jschema.rpc;

import java.util.Map;

/**
 * The interface that
 */
public interface RPCCallHandler {
  String handleCall(String method, String url, Map<String, String> args);
}
