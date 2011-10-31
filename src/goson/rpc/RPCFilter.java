package org.jschema.rpc;

import gw.util.GosuExceptionUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class RPCFilter implements Filter {

  private Config config;

  public RPCFilter() {
    config = new DefaultConfig();
  }

  public RPCFilter withEndPoint(RPCEndPoint endPoint) {
    config.getEndPoints().add(endPoint);
    return this;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    try {
      init(filterConfig.getInitParameter("config"));
    } catch (Exception e) {
      GosuExceptionUtil.forceThrow(e);
    }
  }

  private void init(String configClass) throws Exception {
    if (configClass != null) {
      Class<?> aClass = Class.forName(configClass);
      config = (Config) aClass.newInstance();
    }
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    try {
      for (RPCEndPoint rpcEndPoint : config.getEndPoints()) {
        URI uri = new URI(httpRequest.getRequestURI());
        if (rpcEndPoint.handles(uri)) {
          servletResponse.getOutputStream().print(rpcEndPoint.handle(uri, makeArgs(httpRequest)));
          return;
        }
      }
    } catch (URISyntaxException e) {
      GosuExceptionUtil.forceThrow(e);
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  private Map<String, String> makeArgs(HttpServletRequest httpRequest) {
    HashMap<String, String> map = new HashMap<String, String>();
    Enumeration parameterNames = httpRequest.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String name = (String) parameterNames.nextElement();
      map.put(name, httpRequest.getParameter(name));
    }
    return map;
  }

  @Override
  public void destroy() {
  }

  public interface Config {
    List<RPCEndPoint> getEndPoints();
  }

  private class DefaultConfig implements Config {

    private List<RPCEndPoint> _endPoints;

    private DefaultConfig() {
      _endPoints = new ArrayList<RPCEndPoint>();
    }

    @Override
    public List<RPCEndPoint> getEndPoints() {
      return _endPoints;
    }
  }
}
