package org.jschema.rpc;

import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import org.jschema.typeloader.rpc.IJSchemaRPCType;
import org.jschema.util.JSchemaUtils;

import java.net.URI;
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
    try {
      String method = uri.getPath().substring(_rootPath.length() + 1);
      //TODO cache methods by name
      IMethodInfo decl = findMethodNamed(method, _rpcType);
      if (decl == null) {
        throw new IllegalArgumentException("Could not find a method named " + method + " on " + _rpcType.getName());
      }
      IMethodInfo impl = findMethodNamed(method, _implType);
      if (impl == null) {
        throw new IllegalArgumentException("No implementation for " + method + " on " + _implType.getName());
      }
      IParameterInfo[] parameters = decl.getParameters();
      Object[] paramValues = new Object[parameters.length];
      for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
        IParameterInfo parameter = parameters[i];
        String s = args.get(parameter.getName());
        Object value;
        if (s != null) {
          value = JSchemaUtils.parseJson(s, parameter.getFeatureType());
        } else {
          value = _rpcType.getDefaultValue(method, parameter.getName());
        }
        paramValues[i] = value;
      }
      Object value = impl.getCallHandler().handleCall(_impl, paramValues);
      if (!impl.getReturnType().equals(IJavaType.pVOID)) {
        return JSchemaUtils.serializeJson(value);
      } else {
        return "";
      }
    } catch (Exception e) {
      String message = e.getMessage();
      return JSchemaUtils.createExceptionJSON(message, e.getClass().getName(), scrubbedTrace(e));
    }
  }

  private IMethodInfo findMethodNamed(String method, IType type) {
    for (IMethodInfo mi : type.getTypeInfo().getMethods()) {
      if (mi.getDisplayName().equals(method)) {
        return mi;
      }
    }
    return null;
  }

  private String scrubbedTrace(Exception e) {
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
    return sb.toString();
  }

}
