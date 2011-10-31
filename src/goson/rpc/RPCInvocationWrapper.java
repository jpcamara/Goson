package org.jschema.rpc;

import java.util.concurrent.Callable;

public interface RPCInvocationWrapper {
  String invoke(String url, Callable<String> toInvoke);
}
