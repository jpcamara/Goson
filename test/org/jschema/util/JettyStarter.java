package org.jschema.util;

import gw.lang.IReentrant;
import gw.util.GosuExceptionUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import org.jschema.rpc.RPCEndPoint;
import org.jschema.rpc.RPCFilter;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;


public class JettyStarter {

  public static IReentrant server(int port, RPCEndPoint endPoint) {
    final Server server = new Server(port);

    ServletHandler handler = new ServletHandler();

    RPCFilter filter = new RPCFilter().withEndPoint(endPoint);

    FilterHolder filterHolder = new FilterHolder();
    filterHolder.setName("rpc");
    filterHolder.setFilter(filter);
    handler.addFilter(filterHolder);

    FilterMapping mapping = new FilterMapping();
    mapping.setFilterName("rpc");
    mapping.setPathSpec("/*");
    handler.addFilterMapping(mapping);

    ServletHolder servletHolder = new ServletHolder(new GenericServlet() {
      @Override
      public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        servletResponse.getOutputStream().print("Test");
      }
    });
    servletHolder.setName("test-servlet");
    handler.addServlet(servletHolder);

    ServletMapping servletMapping = new ServletMapping();
    servletMapping.setServletName("test-servlet");
    servletMapping.setPathSpec("/*");
    handler.addServletMapping(servletMapping);

    server.setHandler(handler);

    return new IReentrant() {
      @Override
      public void enter() {
        try {
          server.start();
        } catch (Exception e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }

      @Override
      public void exit() {
        try {
          server.stop();
        } catch (Exception e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
    };
  }

}
