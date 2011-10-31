package org.jschema.rpc;

import gw.util.GosuExceptionUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApacheHTTPClientCallHandler implements RPCCallHandler {

  private final HttpClient _client;

  public ApacheHTTPClientCallHandler() {
    this(new DefaultHttpClient());
  }
  
  public ApacheHTTPClientCallHandler(HttpClient client) {
    _client = client;
  }

  public String handleCall(String method, String url, Map<String, String> args) {
    try {
      HttpResponse resp;
      if ("GET".equalsIgnoreCase(method)) {
        resp = doGet(args, new URL(url));
      } else {
        resp = doPost(args, new URL(url));
      }
      return EntityUtils.toString(resp.getEntity());
    } catch (Exception e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  public HttpResponse doGet(Map<String, String> params, URL url) throws Exception {
    List<BasicNameValuePair> paramList = makeArgList(params);
    URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), url.getPath(), URLEncodedUtils.format(paramList, "UTF-8"), null);
    HttpGet getObj = new HttpGet(uri);
    return _client.execute(getObj);
  }

  public HttpResponse doPost(Map<String, String> params, URL url) throws Exception {
    List<BasicNameValuePair> paramList = makeArgList(params);
    URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), url.getPath(), "", null);
    HttpPost postObject = new HttpPost(uri);
    postObject.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
    return _client.execute(postObject);
  }

  private List<BasicNameValuePair> makeArgList(Map<String, String> params) {
    List<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
    }
    return paramList;
  }
}