package org.jschema.rpc;

import gw.util.GosuExceptionUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class SimpleRPCCallHandler implements RPCCallHandler {

  @Override
  public String handleCall(String method, String url, Map<String, String> args) {
    if (method.equalsIgnoreCase("GET")) {
      return doGet(url, args);
    } else {
      throw new UnsupportedOperationException("POST not yet supported!");
    }
  }

  private String doGet(String url, Map<String, String> args) {
    StringBuilder sb = new StringBuilder(url);
    sb.append("?");
    for (Map.Entry<String, String> entry : args.entrySet()) {
      sb.append(URLEncoder.encode(entry.getKey()));
      sb.append("=");
      sb.append(URLEncoder.encode(entry.getValue()));
    }
    try {
      URL urlObj = new URL(sb.toString());
      BufferedReader in = new BufferedReader(new InputStreamReader(urlObj.openStream()));
      StringBuilder result = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        result.append(inputLine);
      }
      in.close();
      return result.toString();
    } catch (Exception e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }
}
