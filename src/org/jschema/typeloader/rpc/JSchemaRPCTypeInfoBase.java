package org.jschema.typeloader.rpc;

import gw.lang.GosuShop;
import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuExceptionUtil;
import gw.util.GosuStringUtil;
import org.jschema.rpc.JSchemaRPCException;
import org.jschema.rpc.RPCConfig;
import org.jschema.rpc.RPCInvocationWrapper;
import org.jschema.rpc.RPCLoggerCallback;
import org.jschema.util.JSchemaUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.Callable;

public abstract class JSchemaRPCTypeInfoBase extends TypeInfoBase {
  private JSchemaRPCTypeBase _owner;
  private List<? extends IMethodInfo> _methods;
  private List<? extends IPropertyInfo> _properties;
  private Map<String, IPropertyInfo> _propertiesMap;
  private List<? extends IMethodInfo> _jsonDeclaredMethods;

  public JSchemaRPCTypeInfoBase(JSchemaRPCTypeBase owner) {
    _owner = owner;
    _methods = buildMethods();
    _propertiesMap = buildProperties();
    _properties = new ArrayList(_propertiesMap.values());
  }

  private Map<String, IPropertyInfo> buildProperties() {
    HashMap<String, IPropertyInfo> props = new HashMap<String, IPropertyInfo>();
    props.put("Schema", new PropertyInfoBuilder()
            .withName("Schema")
            .withStatic(true)
            .withType(String.class)
            .withReadable(true)
            .withWritable(false)
            .withAccessor(new IPropertyAccessor() {
              @Override
              public Object getValue(Object o) {
                return getOwnersType().getSchemaContent();
              }

              @Override
              public void setValue(Object o, Object o1) {
                throw new IllegalStateException("Schema Property isn't writable");
              }
            }).build(this));
    return props;
  }

  protected List<IMethodInfo> buildMethods() {
    ArrayList<IMethodInfo> methods = new ArrayList<IMethodInfo>();
    buildFunctionMethods(methods);
    _jsonDeclaredMethods = new ArrayList<IMethodInfo>(methods);
    return methods;
  }

  private void buildFunctionMethods(ArrayList<IMethodInfo> methods) {
    for (Map function : _owner.getFunctions()) {

      final String name = (String) function.get("name");
      String functionTypeName = getRootTypeName() + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(name);
      String description = (String) function.get("description");
      List<ParameterInfoBuilder> argBuilders = new ArrayList<ParameterInfoBuilder>();
      final List<String> argNames = new ArrayList<String>();

      if(function.get("args") != null){
        for (Map arg : (List<Map>) function.get("args")) {
          String argName = (String) arg.keySet().iterator().next();
          argNames.add(argName);
          Object type = arg.get(argName);
          String argDescription = (String) arg.get("description");
          Object defaultValue = arg.get("default");
          ParameterInfoBuilder pib = new ParameterInfoBuilder()
                  .withName(argName)
                  .withType(getOwnersType().resolveInnerType(functionTypeName + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(argName), type));
          if (argDescription != null) {
            pib.withDescription(argDescription);
          }
          if (defaultValue != null) {
            pib.withDefValue(GosuShop.getNullExpressionInstance());
          }
          argBuilders.add(pib);
        }
      }

      Object returnTypeSpec = function.get("returns");
      final IType returnType;
      if (returnTypeSpec == null) {
        returnType = JavaTypes.pVOID();
      } else {
        returnType = getOwnersType().resolveInnerType(functionTypeName, returnTypeSpec);
      }

      methods.add(new MethodInfoBuilder()
        .withName(JSchemaUtils.convertJSONStringToGosuIdentifier(name, false))
        .withDescription(description)
        .withStatic(areRPCMethodsStatic())
        .withParameters(argBuilders.toArray(new ParameterInfoBuilder[argBuilders.size()]))
        .withReturnType(returnType)
        .withCallHandler(new JSchemaMethodCallHandler(argNames, name, returnType))
        .build(this)
      );
    }
  }

  protected abstract String getRootTypeName();

  protected abstract String handleRPCMethodInvocation(Object ctx, String method, Map<String, String> argsMap);

  protected abstract boolean areRPCMethodsStatic();

  protected String handleRPCMethodInvocationWithConfig(final RPCConfig config, final String url, final Map<String, String> argsMap) {
    RPCLoggerCallback logger = config.getLogger();
    if (logger != null) {
      logger.log("Invoking RPC call at " + url + " with arguments " + argsMap);
    }
    RPCInvocationWrapper wrapper = config.getWrapper();
    if (wrapper == null) {
      return config.getCallHandler().handleCall(config.getMethod().name(), url, argsMap);
    } else {
      return wrapper.invoke(url, new Callable<String>() {
        @Override
        public String call() throws Exception {
          return config.getCallHandler().handleCall(config.getMethod().name(), url, argsMap);
        }
      });
    }
  }

  @Override
  public List<? extends IPropertyInfo> getProperties() {
    return _properties;
  }

  @Override
  public IPropertyInfo getProperty(CharSequence propName) {
    return _propertiesMap.get(propName.toString());
  }

  @Override
  public CharSequence getRealPropertyName(CharSequence propName) {
    return propName;
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return _methods;
  }

  public List<? extends IMethodInfo> getJSONDeclaredMethods()
  {
    return(_jsonDeclaredMethods);
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

  private class JSchemaMethodCallHandler implements IMethodCallHandler {

    private final List<String> _argNames;
    private final String _name;
    private final IType _returnType;

    public JSchemaMethodCallHandler(List<String> argNames, String name, IType returnType) {
      _argNames = argNames;
      _name = name;
      _returnType = returnType;
    }

    @Override
    public Object handleCall(Object ctx, Object... args) {

      Map<String, String> argsMap = new HashMap<String, String>();
      for (int i = 0; i < args.length; i++) {
        Object value = args[i];
        String name = _argNames.get(i);
        if (value != null || includeNulls(ctx)) {
          String valueString = JSchemaUtils.serializeJson(value);
          argsMap.put(name, valueString);
        }
      }

      String json = handleRPCMethodInvocation(ctx, _name, argsMap);

      // TODO: This should be parseJSONValue.
      Object value = JSchemaUtils.parseJson(json, _returnType);

      handleRPCException(value);

      return value;
    }

    private void handleRPCException(Object value) {
      if (value instanceof Map) {
        Object msgObj = ((Map) value).get(JSchemaUtils.JSCHEMA_EXCEPTION_KEY);
        if (msgObj != null) {
          String message = msgObj.toString();
          Exception exception = createException(message, (String) ((Map) value).get(JSchemaUtils.JSCHEMA_EXCEPTION_TYPE_KEY));
          Object trace = ((Map) value).get(JSchemaUtils.JSCHEMA_TRACE_KEY);
          List<StackTraceElement> stackTrace = removeRPCCode(exception.getStackTrace());
          if (trace instanceof String) {
            addRemoteTrace((String) trace, stackTrace);
          }
          exception.setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
          throw GosuExceptionUtil.forceThrow(exception);
        }
      }
    }

    private Exception createException(String message, String className) {
      if (className != null) {
        try {
          Class<?> aClass = Class.forName(className);
          if (Exception.class.isAssignableFrom(aClass)) {
            Constructor<?> ctor = aClass.getConstructor(String.class);
            if (ctor != null) {
              return (Exception) ctor.newInstance(message);
            }
          }
        } catch (Exception e) {
          //can't instantiate it, ignore
        }
      }
      return new JSchemaRPCException(message);
    }

    List<StackTraceElement> removeRPCCode(StackTraceElement[] stackTrace) {
      LinkedList<StackTraceElement> scrubbedTrace = new LinkedList<StackTraceElement>();
      boolean foundRPCCode = false;
      for (int i = 0; i < stackTrace.length; i++) {
        StackTraceElement stackTraceElement = stackTrace[i];
        if (stackTraceElement.getClassName().startsWith("org.jschema.typeloader.rpc")) {
          foundRPCCode = true;
          //skip
        } else if (!foundRPCCode) {
          //skip
        } else {
          scrubbedTrace.add(stackTraceElement);
        }
      }
      scrubbedTrace.add(0, new StackTraceElement(getOwnersType().getName(), _name, "", -1));
      return scrubbedTrace;
    }

    private void addRemoteTrace(String trace, List<StackTraceElement> stackTrace) {
      String[] traceElements = trace.split("\n");
      for (int i = 0, traceElementsLength = traceElements.length; i < traceElementsLength; i++) {
        String traceElement = traceElements[traceElements.length - 1 - i];
        if (GosuStringUtil.isNotEmpty(traceElement)) {
          stackTrace.add(0, processTraceElement(traceElement));
        }
      }
    }

    private StackTraceElement processTraceElement(String traceElement) {

      String dotPath = extractFirstPart(traceElement);
      String className;
      String methodName;
      if (dotPath.lastIndexOf('.') != -1) {
        className = dotPath.substring(0, dotPath.lastIndexOf('.'));
        methodName = dotPath.substring(dotPath.lastIndexOf('.') + 1, dotPath.length());
      } else {
        className = dotPath;
        methodName = "";
      }

      String paren = extractParenthesizedPart(traceElement);
      String fileName;
      int lineNumber;
      if (paren.indexOf(':') != -1) {
        fileName = paren.substring(0, paren.indexOf(':'));
        try {
          lineNumber = Integer.parseInt(paren.substring(paren.indexOf(':') + 1, paren.length()));
        } catch (NumberFormatException e) {
          lineNumber = -1;
        }
      } else {
        fileName = paren;
        lineNumber = -1;
      }

      return new StackTraceElement(className, methodName, fileName, lineNumber);
    }

    private String extractFirstPart(String traceElement) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < traceElement.length(); i++) {
        char c = traceElement.charAt(i);
        if (c == '(') {
          return sb.toString();
        } else {
          sb.append(c);
        }
      }
      return sb.toString();
    }

    private String extractParenthesizedPart(String traceElement) {
      StringBuilder sb = new StringBuilder();
      boolean foundParen = false;
      for (int i = 0; i < traceElement.length(); i++) {
        char c = traceElement.charAt(i);
        if (c == ')' && foundParen) {
          return sb.toString();
        } else if (c == '(') {
          foundParen = true;
        } else if(foundParen) {
          sb.append(c);
        }
      }
      return sb.toString();
    }
  }

  protected abstract boolean includeNulls(Object ctx);
}
