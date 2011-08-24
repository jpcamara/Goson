package org.jschema.typeloader.rpc;

import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import org.jschema.parser.JSONParser;
import org.jschema.typeloader.IJsonType;
import org.jschema.typeloader.Json;
import org.jschema.typeloader.JsonTypeInfo;
import org.jschema.util.JSchemaUtils;

import java.util.*;

public abstract class JSchemaRPCTypeInfoBase extends TypeInfoBase {
  private JSchemaRPCTypeBase _owner;
  private List<? extends IMethodInfo> _methods;

  public JSchemaRPCTypeInfoBase(JSchemaRPCTypeBase owner) {
    _owner = owner;
    _methods = buildMethods();
  }

  protected List<IMethodInfo> buildMethods() {
    ArrayList<IMethodInfo> methods = new ArrayList<IMethodInfo>();
    buildFunctionMethods(methods);
    return methods;
  }

  private void buildFunctionMethods(ArrayList<IMethodInfo> methods) {
    for (Map function : _owner.getFunctions()) {

      final String name = (String) function.get("name");
      String functionTypeName = getRootTypeName() + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(name);
      String description = (String) function.get("description");
      List<ParameterInfoBuilder> argBuilders = new ArrayList<ParameterInfoBuilder>();
      final List<String> argNames = new ArrayList<String>();

      for (Map arg : (List<Map>) function.get("args")) {
        String argName = (String) arg.keySet().iterator().next();
        argNames.add(argName);
        Object type = arg.get(argName);
        String argDescription = (String) arg.get("description");
        String defaultValue = (String) arg.get("default");
        ParameterInfoBuilder pib = new ParameterInfoBuilder()
          .withName(argName)
          .withType(getType(functionTypeName + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(argName), type));
        if (argDescription != null) {
          pib.withDescription(argDescription);
        }
        argBuilders.add(pib);
      }

      Object returnTypeSpec = function.get("returns");
      final IType returnType;
      if (returnTypeSpec == null) {
        returnType = IJavaType.pVOID;
      } else {
        returnType = getType(functionTypeName, returnTypeSpec);
      }

      methods.add(new MethodInfoBuilder()
        .withName(JSchemaUtils.convertJSONStringToGosuIdentifier(name, false))
        .withDescription(description)
        .withStatic(areRPCMethodsStatic())
        .withParameters(argBuilders.toArray(new ParameterInfoBuilder[argBuilders.size()]))
        .withReturnType(returnType)
        .withCallHandler(new IMethodCallHandler() {

          @Override
          public Object handleCall(Object ctx, Object... args) {

            Map<String, String> argsMap = new HashMap<String, String>();
            for (int i = 0; i < args.length; i++) {
              Object value = args[i];
              String name = argNames.get(i);
              String valueString = JSchemaUtils.serializeJson(value);
              argsMap.put(name, valueString);
            }

            String json = handleRPCMethodInvocation(ctx, name, argsMap);

            return JSchemaUtils.parseJson(json, returnType);
          }
        })
        .build(this)
      );
    }
  }

  protected abstract String getRootTypeName();

  protected abstract String handleRPCMethodInvocation(Object ctx, String method, Map<String, String> argsMap);

  protected abstract boolean areRPCMethodsStatic();

  private IType getType(String s, Object type) {
    if (type instanceof String) {
      return JsonTypeInfo.findJavaType((String) type);
    } else if (type instanceof List) {
      return IJavaType.LIST.getParameterizedType(getType(s, ((List) type).get(0)));
    } else if (type instanceof Map) {
      if (((Map) type).get("map_of") != null) {
        return IJavaType.MAP.getParameterizedType(IJavaType.STRING, getType(s, ((Map) type).get("map_of")));
      } else {
        return TypeSystem.getByFullName(s);
      }
    } else {
      throw new IllegalArgumentException("Don't know how to create a type for " + type);
    }
  }

  @Override
  public List<? extends IPropertyInfo> getProperties() {
    return Collections.emptyList();
  }

  @Override
  public IPropertyInfo getProperty(CharSequence propName) {
    return null;
  }

  @Override
  public CharSequence getRealPropertyName(CharSequence propName) {
    return propName;
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return _methods;
  }

  @Override
  public List<? extends IConstructorInfo> getConstructors() {
    return Collections.emptyList();
  }

  @Override
  public List<IAnnotationInfo> getDeclaredAnnotations() {
    return Collections.emptyList();
  }

  @Override
  public JSchemaRPCTypeBase getOwnersType() {
    return _owner;
  }
}
