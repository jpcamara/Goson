package org.jschema.rpc;

import com.sun.imageio.plugins.common.I18N;
import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaArrayType;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuExceptionUtil;
import org.jschema.typeloader.rpc.IJSchemaRPCType;
import org.jschema.typeloader.rpc.JSchemaRPCTypeInfoBase;
import org.jschema.util.JSchemaUtils;

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
    validate();
    return;
  }

  public void validate()
  {
    JSchemaRPCTypeInfoBase jsonTypeInfo = (JSchemaRPCTypeInfoBase) _rpcType.getTypeInfo();
    ITypeInfo implTypeInfo = _implType.getTypeInfo();
    IType[] paramTypes;
    for(IMethodInfo jsonMethodInfo : jsonTypeInfo.getJSONDeclaredMethods()){
      IParameterInfo[] jsonImplParameters = jsonMethodInfo.getParameters();
      paramTypes = new IType[jsonImplParameters.length];
      for(int cntr = 0; cntr < jsonImplParameters.length; cntr++){
        paramTypes[cntr] = jsonImplParameters[cntr].getOwnersType();
      }
      List<? extends IMethodInfo> methods = implTypeInfo.getMethods();
      boolean matched = false;
      for(IMethodInfo implMethodInfo : methods){
        if(implMethodInfo.getDisplayName().compareTo(jsonMethodInfo.getDisplayName()) == 0){
          matched = true;
          IParameterInfo[] implParameters = implMethodInfo.getParameters();
          matched = compareImplParameters(jsonImplParameters, implParameters);
        }
      }
      if(matched == false){
        throw(new IllegalArgumentException("Method " + jsonMethodInfo.getName() + " declared on type " + _rpcType.getName() + " does not exist on impl type " + _implType.getName()));
      }
    }
    return;
  }

  private boolean compareImplParameters(IParameterInfo[] jsonImplParameters, IParameterInfo[] implParameters)
  {
    boolean retVal = false;
    if(implParameters.length == jsonImplParameters.length){
      retVal = true;
      for(int cntr = 0; cntr < implParameters.length; cntr++){
        IType jsonParamType = jsonImplParameters[cntr].getFeatureType();
        IType implParamType = implParameters[cntr].getFeatureType();
        if(implParamType.equals(jsonParamType) == false){
          retVal = false;
          break;
        }
      }
    }
    return(retVal);
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
