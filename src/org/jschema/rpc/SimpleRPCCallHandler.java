package org.jschema.rpc;

import gw.util.GosuExceptionUtil;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

public class SimpleRPCCallHandler implements RPCCallHandler {

  @Override
  public String handleCall(String method, String url, Map<String, String> args) {
    if (method.equalsIgnoreCase("GET")) {
      return doGet(url, args);
    } else {
      return doPost(url, args);
    }
  }

  private String doPost(String url, Map<String, String> args) {
    StringBuilder sb = new StringBuilder();
    sb = urlEncodeValues(args, sb);
    try {
      URL urlObj = new URL(url);
      URLConnection urlConnection = urlObj.openConnection();
      urlConnection.setDoOutput(true);
      OutputStream outputStream = urlConnection.getOutputStream();
      try
      {
        outputStream.write(sb.toString().getBytes());
        InputStream inputStream = urlConnection.getInputStream();
        return readResponse(inputStream);
      }
      finally
      {
        outputStream.close();
      }
    } catch (Exception e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  private String doGet(String url, Map<String, String> args) {
    StringBuilder sb = new StringBuilder(url).append("?");
    sb = urlEncodeValues(args, sb);
    try {
      URL urlObj = new URL(sb.toString());
      return readResponse(urlObj.openStream());
    } catch (Exception e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  private StringBuilder urlEncodeValues(Map<String, String> args, StringBuilder sb) {
    for (Map.Entry<String, String> entry : args.entrySet()) {
      sb.append(URLEncoder.encode(entry.getKey()));
      sb.append("=");
      sb.append(URLEncoder.encode(entry.getValue()));
    }
    return sb;
  }

  private String readResponse(InputStream inputStream) throws IOException {
    StringBuilder result;
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
      result = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        result.append(inputLine);
      }
      in.close();
    } finally {
      inputStream.close();
    }
    return result.toString();
  }

}
