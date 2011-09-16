package org.jschema.rpc;

import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import org.jschema.typeloader.rpc.IJSchemaRPCType;
import org.jschema.typeloader.rpc.JSchemaRPCTypeInfoBase;
import org.jschema.util.JSchemaUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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

    List<String> validationErrors = new ArrayList<String>();

    for(IMethodInfo jsonMethodInfo : jsonTypeInfo.getJSONDeclaredMethods()){
      IParameterInfo[] jsonImplParameters = jsonMethodInfo.getParameters();
      List<? extends IMethodInfo> methods = implTypeInfo.getMethods();
      boolean matched = false;
      for(IMethodInfo implMethodInfo : methods){
        if(implMethodInfo.getDisplayName().compareTo(jsonMethodInfo.getDisplayName()) == 0){
          matched = true;
          IParameterInfo[] implParameters = implMethodInfo.getParameters();
          compareFormalArgTypes(jsonImplParameters, implParameters, jsonMethodInfo, validationErrors);
          compareReturnTypes(jsonMethodInfo, implMethodInfo, validationErrors);
          break;
        }
      }
      if(matched == false){
        validationErrors.add("Method " + jsonMethodInfo.getName() + " declared on type " + _rpcType.getName() + " does not exist on impl type " + _implType.getName());
      }
    }
    if(validationErrors.size() != 0){
      String newline = String.format("%n");
      StringBuilder buf = new StringBuilder("Error validating RPC endpoint " + _rpcType.getName());
      buf.append(newline);

      for(int cntr = 0; cntr < validationErrors.size(); cntr++){
        buf.append(validationErrors.get(cntr));
        if(cntr < validationErrors.size()-1){
          buf.append(newline);
        }
      }
      throw(new JSchemaRPCException(buf.toString()));
    }

    return;
  }

  private void compareReturnTypes(IMethodInfo jsonMethodInfo, IMethodInfo implMethodInfo, List<String> validationErrors)
  {
    if(jsonMethodInfo.getReturnType().equals(implMethodInfo.getReturnType()) == false) {
      boolean isError = true;
      // Allow primitive types for returns...
      if(jsonMethodInfo.getReturnType().equals(IJavaType.LONG)){
        if(implMethodInfo.getReturnType().equals(IJavaType.pINT) ||  implMethodInfo.getReturnType().equals(IJavaType.pLONG)){
          isError = false;
        }
      }
      else if(jsonMethodInfo.getReturnType().equals(IJavaType.BIGDECIMAL)){
        if(implMethodInfo.getReturnType().equals(IJavaType.pFLOAT) ||  implMethodInfo.getReturnType().equals(IJavaType.pDOUBLE)){
          isError = false;
        }
      }
      if(isError == true){
        validationErrors.add("Method " + jsonMethodInfo.getName() + " declared on type " + _rpcType.getName() + " declares a different return type than does impl type " + _implType.getName());
      }
    }
    return;
  }

  private void compareFormalArgTypes(IParameterInfo[] jsonImplParameters, IParameterInfo[] implParameters, IMethodInfo jsonMethodInfo, List<String> validationErrors)
  {
    if(implParameters.length == jsonImplParameters.length){
      for(int cntr = 0; cntr < implParameters.length; cntr++){
        IType jsonParamType = jsonImplParameters[cntr].getFeatureType();
        IType implParamType = implParameters[cntr].getFeatureType();
        if(implParamType.equals(jsonParamType) == false){
          validationErrors.add("Method " + jsonMethodInfo.getName() + " declared on type " + _rpcType.getName() + " declares parameter(s) of differing type(s) than are on impl type " + _implType.getName());
          break;
        }
      }
    }
    else{
      validationErrors.add("Method " + jsonMethodInfo.getName() + " declared on type " + _rpcType.getName() + " declares a different number of parameters (" + jsonImplParameters.length + ") than are on impl type " + _implType.getName() + " (" + implParameters.length + ")");
    }
  }

  public boolean handles(URI uri) {
    return uri.getPath().startsWith(_rootPath);
  }

  public String handle(final URI uri, final Map<String, String> args) {
    if (RPCDefaults.getLogger() != null) {
      RPCDefaults.getLogger().log("Handling call to " + uri + " with args " + args);
    }
    RPCInvocationWrapper wrapper = RPCDefaults.getHandlerWrapper();
    if (wrapper != null) {
      return wrapper.invoke(uri.toString(), new Callable<String>() {
        @Override
        public String call() throws Exception {
          return _handle(uri, args);
        }
      });
    } else {
      return _handle(uri, args);
    }
  }

  private String _handle(URI uri, Map<String, String> args) {
    try {
      if (args.size() == 1 && args.containsKey("JSchema-RPC")) {
        return _rpcType.getSchemaContent();
      }
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
          value = JSchemaUtils.parseJSONValue(s, parameter.getFeatureType());
        } else {
          value = _rpcType.getDefaultValue(method, parameter.getName());
        }
        paramValues[i] = value;
      }
      Object value = impl.getCallHandler().handleCall(_impl, paramValues);
      if (!impl.getReturnType().equals(IJavaType.pVOID)) {
        return JSchemaUtils.serializeJSON(value);
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
