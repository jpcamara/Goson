package org.jschema.rpc;

import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaArrayType;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuExceptionUtil;
import org.jschema.typeloader.rpc.IJSchemaRPCType;
import org.jschema.util.JSchemaUtils;
import sun.jvm.hotspot.debugger.remote.RemoteThread;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class RPCEndPoint {

  private IJSchemaRPCType _rpcType;
  private Object _impl;
  private String _rootPath;
  private IType _implType;

  public RPCEndPoint(IJSchemaRPCType rpcType, Object impl, String rootPath) {
    _rpcType = rpcType;
    _impl = impl;
    _implType = TypeSystem.getFromObject(impl);
    _rootPath = rootPath;
  }

  public void validate() {
    //TODO validate the the impl satisfies the schema
  }

  public boolean handles(URI uri) {
    return uri.getPath().startsWith(_rootPath);
  }

  public String handle(URI uri, Map<String, String> args) {
    //TODO cache methods by name
    try {
      String method = uri.getPath().substring(_rootPath.length() + 1);
      for (IMethodInfo mi : _implType.getTypeInfo().getMethods()) {
        if (mi.getDisplayName().equals(method)) {
          IParameterInfo[] parameters = mi.getParameters();
          Object[] paramValues = new Object[parameters.length];
          for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            IParameterInfo parameter = parameters[i];
            String s = args.get(parameter.getName());
            paramValues[i] = JSchemaUtils.parseJson(s, parameter.getFeatureType());
          }
          Object value = mi.getCallHandler().handleCall(_impl, paramValues);
          if (!mi.getReturnType().equals(IJavaType.pVOID)) {
            return JSchemaUtils.serializeJson(value);
          } else {
            return "";
          }
        }
      }
      return JSchemaUtils.createExceptionJSON("No method found with name '" + method + "'");
    } catch (Exception e) {
      String message = e.getMessage();
      StringBuilder sb = new StringBuilder();

      StackTraceElement[] stackTrace = e.getStackTrace();
      StackTraceElement[] currentStackTrace = new RuntimeException().getStackTrace();
      for (int i = 0; i < stackTrace.length; i++) {
        String stackTraceElement = stackTrace[stackTrace.length - 1 - i].toString();
        String currentTraceElement = 0 <= currentStackTrace.length - 1 - i ? currentStackTrace[currentStackTrace.length - 1 - i].toString() : "";
        if (!stackTraceElement.equals(currentTraceElement)) {
          sb.insert(0, stackTraceElement);
          sb.insert(0, "\n");
        }
      }

      return JSchemaUtils.createExceptionJSON(message, e.getClass().getName(), sb.toString());
    }
  }

}
