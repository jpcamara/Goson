package org.jschema.typeloader;

import gw.internal.gosu.parser.AnnotationInfo;
import gw.lang.IAnnotation;
import gw.lang.IAutocreate;
import gw.lang.function.Function0;
import gw.lang.parser.ISymbol;
import gw.lang.parser.coercers.FunctionFromInterfaceCoercer;
import gw.lang.reflect.*;
import gw.lang.reflect.IRelativeTypeInfo.Accessibility;
import gw.lang.reflect.java.IJavaType;
import gw.util.concurrent.LazyVar;
import org.jschema.model.JsonMap;
import org.jschema.model.JsonObject;
import org.jschema.rpc.SimpleRPCCallHandler;
import org.jschema.util.JSchemaUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;

public class JSchemaTypeInfo extends TypeInfoBase {

  private IJSchemaType owner;
  private Map json;
  private Map<String, String> jsonSlotToPropertyName = new HashMap<String, String>();
  private List<IPropertyInfo> properties;

  private LazyVar<List<IMethodInfo>> methods = new LazyVar<List<IMethodInfo>>() {
    @Override
    protected List<IMethodInfo> init() {
      return buildMethods();
    }
  };

  private List<IMethodInfo> buildMethods() {
    if (isJsonEnum()) {
      return (List) TypeSystem.get(JSchemaEnumType.JsonEnumValue.class).getTypeInfo().getMethods();
    } else {
      List<IMethodInfo> typeMethods = new ArrayList<IMethodInfo>();
      typeMethods.add(new MethodInfoBuilder()
        .withName("write")
        .withReturnType(IJavaType.STRING)
        .withStatic(true)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return JSchemaUtils.serializeJson(ctx);
          }
        })
        .build(JSchemaTypeInfo.this));
      typeMethods.add(new MethodInfoBuilder()
        .withName("prettyPrint")
        .withParameters(new ParameterInfoBuilder()
          .withType(IJavaType.INTEGER)
          .withName("indent")
          .withDefValue(ISymbol.NULL_DEFAULT_VALUE))
        .withReturnType(IJavaType.STRING)
        .withStatic(true)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            Integer indent = Integer.valueOf(2);
            if (args[0] != null) {
              indent = (Integer) args[0];
            }
            return JSchemaUtils.serializeJson(ctx, indent);
          }
        })
        .build(JSchemaTypeInfo.this));
      typeMethods.add(parseMethod()
        .withParameters(new ParameterInfoBuilder()
          .withType(IJavaType.STRING)
          .withName("content"))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return JSchemaUtils.parseJson((String) args[0], getOwnersType());
          }
        })
        .build(JSchemaTypeInfo.this));
      typeMethods.add(parseMethod()
        .withParameters(new ParameterInfoBuilder()
          .withType(TypeSystem.get(java.net.URL.class))
          .withName("content"))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            try {
              java.net.URL content = (java.net.URL) args[0];
              BufferedReader reader = new BufferedReader(new InputStreamReader(content.openConnection().getInputStream()));
              StringBuilder builder = new StringBuilder();
              String line = reader.readLine();
              while (line != null) {
                builder.append(line);
                line = reader.readLine();
              }
              return JSchemaUtils.parseJson(builder.toString(), getOwnersType());
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        })
        .build(JSchemaTypeInfo.this));
      typeMethods.add(new MethodInfoBuilder()
        .withName("get")
        .withParameters(new ParameterInfoBuilder()
          .withType(IJavaType.STRING)
          .withName("url"),
          new ParameterInfoBuilder()
            .withType(IJavaType.MAP.getParameterizedType(IJavaType.STRING, IJavaType.OBJECT))
            .withDefValue(ISymbol.NULL_DEFAULT_VALUE)
            .withName("args")
        )
        .withReturnType(getOwnersType())
        .withStatic(true)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            Map<String, String> fixedArgs = fixArgs((Map) args[1]);
            return JSchemaUtils.parseJson(SimpleRPCCallHandler.doGet((String) args[0], fixedArgs), getOwnersType());
          }
        })
        .build(JSchemaTypeInfo.this));

      typeMethods.add(new MethodInfoBuilder()
        .withName("post")
        .withParameters(new ParameterInfoBuilder()
          .withType(IJavaType.STRING)
          .withName("url"),
          new ParameterInfoBuilder()
            .withType(IJavaType.MAP.getParameterizedType(IJavaType.STRING, IJavaType.OBJECT))
            .withDefValue(ISymbol.NULL_DEFAULT_VALUE)
            .withName("args")
        )
        .withReturnType(getOwnersType())
        .withStatic(true)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            Map<String, String> fixedArgs = fixArgs((Map) args[1]);
            return JSchemaUtils.parseJson(SimpleRPCCallHandler.doPost((String) args[0], fixedArgs), getOwnersType());
          }
        })
        .build(JSchemaTypeInfo.this));

      typeMethods.add(new MethodInfoBuilder()
        .withName("asJsonMap")
        .withReturnType(TypeSystem.get(JsonMap.class))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return ctx;
          }
        })
        .build(JSchemaTypeInfo.this));

      return typeMethods;
    }
  }

  private MethodInfoBuilder parseMethod() {
    return new MethodInfoBuilder()
      .withName("parse")
      .withReturnType(getOwnersType())
      .withStatic(true);
  }

  private static Map<String, String> fixArgs(Map arg) {
    if (arg == null) {
      return Collections.emptyMap();
    }
    else {
      HashMap<String, String> fixedArgs = new HashMap(arg.size());
      for (Object key : arg.keySet()) {
        Object value = arg.get(key);
        if (!(key instanceof String)) {
          key = "" + key;
        }
        if (!(value instanceof String)) {
          value = "" + value;
        }
        fixedArgs.put((String) key, (String) value);
      }
      return fixedArgs;
    }
  }

  public JSchemaTypeInfo(JSchemaType owner, Object object) {
    this.owner = owner;
    this.json = (Map)object;
    createProperties();
  }

	private void createProperties() {
    if (isJsonEnum()) {
      properties = createEnumProperties();
    } else {
      properties = createStructProperties();
    }
  }

  private boolean isJsonEnum() {
    return getOwnersType() instanceof IEnumType;
  }

  private List<IPropertyInfo> createEnumProperties() {
    ArrayList<IPropertyInfo> props = new ArrayList<IPropertyInfo>();
    IEnumType type = (IEnumType) getOwnersType();
    for (final IEnumValue enumValue : type.getEnumValues()) {
      PropertyInfoBuilder property = new PropertyInfoBuilder()
        .withName(enumValue.getCode())
        .withStatic()
        .withType(type)
        .withWritable(false)
        .withAccessor(new IPropertyAccessor() {
          @Override
          public Object getValue(Object ctx) {
            return enumValue;
          }

          @Override
          public void setValue(Object ctx, Object value) {
            throw new UnsupportedOperationException("Cannot set an enum");
          }
        });
      props.add(property.build(this));
    }
    return props;
  }

  private List<IPropertyInfo> createStructProperties() {
    ArrayList<IPropertyInfo> props = new ArrayList<IPropertyInfo>();
    HashSet<String> propNames = new HashSet<String>();

    for (Object k : json.keySet()) {

      final String jsonSlotName = (String) k;
      String propertyName = JSchemaUtils.convertJSONStringToGosuIdentifier(jsonSlotName);
      final Object value = json.get(jsonSlotName);

      jsonSlotToPropertyName.put(jsonSlotName, propertyName);

      final IType propType = getOwnersType().resolveInnerType(getOwnersType() + "."+ propertyName, value);

      PropertyInfoBuilder pib = new PropertyInfoBuilder()
        .withName(propertyName)
        .withType(propType);
      propNames.add(propertyName);

      if (propType instanceof IEnumType) {
        pib.withAccessor(new IPropertyAccessor() {
          @Override
          public Object getValue(Object ctx) {
            JsonMap json = (JsonMap) ctx;
            return ((IEnumType) propType).getEnumValue(json.get(jsonSlotName).toString());
          }

          @Override
          public void setValue(Object ctx, Object value) {
            json.put(jsonSlotName, ((IEnumValue) value).getValue());
          }
        });
      } else {
        pib.withAccessor(new IPropertyAccessor() {
          @Override
          public void setValue(Object ctx, Object value) {
            JsonMap json = (JsonMap) ctx;
            json.put(jsonSlotName, value);
          }

          @Override
          public Object getValue(Object ctx) {
            JsonMap json = (JsonMap) ctx;
            return json.get(jsonSlotName);
          }
        });

        if (propType instanceof IJSchemaType) {
          pib.withAnnotations(makeMapAutoCreateAnnotation(propType));
        }

      }
      props.add(pib.build(this));
    }

    props.add(new PropertyInfoBuilder()
      .withName("Descendents" + (propNames.contains("Descendents") ? "$" : ""))
      .withType(Iterable.class)
      .withWritable(false)
      .withAccessor(new IPropertyAccessor() {
        @Override
        public Object getValue(Object ctx) {
          JsonMap jsonMap = (JsonMap) ctx;
          return jsonMap.getDescendents();
        }

        @Override
        public void setValue(Object ctx, Object value) {
          throw new IllegalAccessError("Descendents Is Read Only");
        }
      }).build(this));

    IType outerParent = TypeSystem.getByFullNameIfValid(getOwnersType().getNamespace());
    final IType parentPropertyType;
    if (outerParent instanceof IJSchemaType && !thisIsTypedefFor((IJSchemaType) outerParent)) {
      parentPropertyType = outerParent;
    } else {
      parentPropertyType = TypeSystem.get(JsonMap.class);
    }

    props.add(new PropertyInfoBuilder()
      .withName("Parent" + (propNames.contains("Parent") ? "$" : ""))
      .withType(parentPropertyType)
      .withWritable(false)
      .withAccessor(new IPropertyAccessor() {
        @Override
        public Object getValue(Object ctx) {
          JsonMap jsonMap = (JsonMap) ctx;
          JsonObject parent = jsonMap.getParent();
          while (parent != null && !isStronglyTypedMap(parent)) {
            parent = parent.getParent();
          }
          return parent;
        }

        @Override
        public void setValue(Object ctx, Object value) {
          throw new IllegalAccessError("Parent Is Read Only");
        }
      }).build(this));

    return props;
  }

  private IAnnotationInfo makeMapAutoCreateAnnotation(final IType propType) {
    return new AnnotationInfo(TypeSystem.get(JSchemaMapAutoCreate.class),
      new JSchemaMapAutoCreate(propType), this);
  }

  private boolean isStronglyTypedMap(JsonObject parent) {
    if (parent instanceof JsonMap) {
      return parent.getIntrinsicType() instanceof IJSchemaType;
    } else {
      return false;
    }
  }

  private boolean thisIsTypedefFor(IJSchemaType outerParent) {
    return  outerParent.getTypeDefs().containsValue(getOwnersType().getName());
  }

  private IConstructorInfo defaultConstructor = new ConstructorInfoBuilder()
			.withConstructorHandler(new IConstructorHandler() {
				@Override
				public Object newInstance(Object... args) {
          return new JsonMap(getOwnersType());
        }
			}).withAccessibility(Accessibility.PUBLIC).build(this);

	@Override
	public List<? extends IConstructorInfo> getConstructors() {
		List<IConstructorInfo> constructors = new ArrayList<IConstructorInfo>();
		constructors.add(defaultConstructor);
		return constructors;
	}

	@Override
	public List<? extends IMethodInfo> getMethods() {
		return methods.get();
	}

	@Override
	public List<? extends IPropertyInfo> getProperties() {
		return properties;
	}

	@Override
	public IPropertyInfo getProperty(CharSequence propName) {
		for (IPropertyInfo prop : properties) {
			if (propName.equals(prop.getName())) {
				return prop;
			}
		}
		return null;
	}

	@Override
	public CharSequence getRealPropertyName(CharSequence propName) {
		for (IPropertyInfo prop : properties) {
			if (propName.equals(prop.getName())) {
				return prop.getName();
			}
		}
		return null;
	}
	
	@Override
	public List<IAnnotationInfo> getDeclaredAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasAnnotation(IType type) {
		return false;
	}

	@Override
	public IJSchemaType getOwnersType() {
		return owner;
	}

	public String toString() {
		return properties.toString();
	}

  public IType getTypeForJsonSlot(String key) {
    String propertyName = jsonSlotToPropertyName.get(key);
    if (propertyName != null) {
      IPropertyInfo property = getProperty(propertyName);
      if (property != null) {
        return property.getFeatureType();
      }
    }
    return null;
  }

  private static class JSchemaMapAutoCreate implements IAnnotation, IAutocreate {
    private final IType _propType;

    public JSchemaMapAutoCreate(IType propType) {
      _propType = propType;
    }

    @Override
    public Object getBlock() {
      return new Function0() {
        @Override
        public Object invoke() {
          return new JsonMap(_propType);
        }
      };
    }
  }
}
