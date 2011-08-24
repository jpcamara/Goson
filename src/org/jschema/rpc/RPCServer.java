package org.jschema.rpc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import gw.lang.IReentrant;
import gw.util.GosuExceptionUtil;
import org.jschema.util.JSchemaUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPCServer implements IReentrant {

  List<RPCEndPoint> _endPoints = new ArrayList<RPCEndPoint>();
  private HttpServer _httpServer;
  private int _port = 12321;

  public void start() {
    try {
      _httpServer = HttpServer.create(new InetSocketAddress(_port), 0);
      _httpServer.createContext("/", new JSchemaHandler());
      _httpServer.setExecutor(null);
      _httpServer.start();
    } catch (Exception e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  public void stop() {
    _httpServer.stop(0);
  }

  public void addEndPoint(RPCEndPoint endPoint) {
    _endPoints.add(endPoint);
  }

  public void removeEndPoint(RPCEndPoint endPoint) {
    _endPoints.remove(endPoint);
  }

  @Override
  public void enter() {
    start();
  }

  @Override
  public void exit() {
    stop();
  }

  private class JSchemaHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      String path = httpExchange.getRequestURI().getPath();
      for (RPCEndPoint endPoint : _endPoints) {
        if (endPoint.handles(path)) {
          Map<String, String> args = new HashMap<String, String>();
          if (httpExchange.getRequestMethod().equals("GET")) {
            String query = httpExchange.getRequestURI().getQuery();
            String[] argArray = query.split("&");
            for (String arg : argArray) {
              String[] pair = arg.split("=");
              if (pair.length == 2) {
                args.put(URLDecoder.decode(pair[0]), URLDecoder.decode(pair[1]));
              } else if (pair.length == 1) {
                args.put(URLDecoder.decode(pair[0]), null);
              }
            }
            writeResponse(httpExchange, 200, endPoint.handle(path, args));
          } else if (httpExchange.getRequestMethod().equals("POST")) {
            writeResponse(httpExchange, 501, "'{' \"@@exception\" : \"Post not implemented!\" }");
            return;
          }
          writeResponse(httpExchange, 200, endPoint.handle(path, args));
        }
      }
      writeResponse(httpExchange, 404, JSchemaUtils.createExceptionJSON("No JSchema end point was found at '" + path + "'"));
    }

    private void writeResponse(HttpExchange httpExchange, int responseCode, String message) throws IOException {
      httpExchange.sendResponseHeaders(responseCode, message.length());
      OutputStream os = httpExchange.getResponseBody();
      os.write(message.getBytes());
      os.close();
    }
  }
}
