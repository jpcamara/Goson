package org.jschema.rpc;

import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaArrayType;
import gw.lang.reflect.java.IJavaType;
import org.jschema.typeloader.rpc.IJSchemaRPCType;
import org.jschema.util.JSchemaUtils;
import sun.jvm.hotspot.debugger.remote.RemoteThread;

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

  public boolean handles(String path) {
    return path.startsWith(_rootPath);
  }

  public String handle(String path, Map<String, String> args) {
    //TODO cache methods by name
    String method = path.substring(_rootPath.length() + 1);
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
  }
}
