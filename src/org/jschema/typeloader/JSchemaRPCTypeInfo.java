package org.jschema.typeloader;

import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaArrayType;
import gw.lang.reflect.java.IJavaType;
import org.jschema.util.JSONUtils;

import java.util.*;

public class JSchemaRPCTypeInfo extends TypeInfoBase
{
  private JSchemaRPCType _owner;
  private List<? extends IMethodInfo> _methods;

  public JSchemaRPCTypeInfo(JSchemaRPCType owner) {
    _owner = owner;
    _methods = buildMethods();
  }

  private List<? extends IMethodInfo> buildMethods() {
    ArrayList<IMethodInfo> methods = new ArrayList<IMethodInfo>();

    for (Map function : _owner.getFunctions()) {

      String name = (String) function.get("name");
      String functionTypeName = getOwnersType().getName() + "." + JSONUtils.convertJSONStringToGosuIdentifier(name);
      String description = (String) function.get("description");
      List<ParameterInfoBuilder> argBuilders = new ArrayList<ParameterInfoBuilder>();

      for (Map arg : (List<Map>) function.get("args")) {
        String argName = (String) arg.keySet().iterator().next();
        Object type = arg.get(argName);
        String argDescription = (String) arg.get("description");
        String defaultValue = (String) arg.get("default");
        ParameterInfoBuilder pib = new ParameterInfoBuilder()
          .withName(argName)
          .withType(getType(functionTypeName + "." + JSONUtils.convertJSONStringToGosuIdentifier(argName), type));
        if (argDescription != null) {
          pib.withDescription(argDescription);
        }
        argBuilders.add(pib);
      }

      Object returnTypeSpec = function.get("returns");
      IType returnType;
      if (returnTypeSpec == null) {
        returnType = IJavaType.pVOID;
      } else {
        returnType = getType(functionTypeName, returnTypeSpec);
      }

      methods.add(new MethodInfoBuilder()
        .withName(JSONUtils.convertJSONStringToGosuIdentifier(name, false))
        .withDescription(description)
        .withStatic()
        .withParameters(argBuilders.toArray(new ParameterInfoBuilder[argBuilders.size()]))
        .withReturnType(returnType)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            throw new RuntimeException("Not implemented");
          }
        })
        .build(this)
      );
    }
    return methods;
  }

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
  public IType getOwnersType() {
    return _owner;
  }
}
