package org.jschema.typeloader;

import gw.lang.parser.ISymbol;
import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IConstructorInfo;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.PropertyInfoBuilder;
import gw.lang.reflect.TypeInfoBase;
import gw.lang.reflect.IRelativeTypeInfo.Accessibility;
import gw.lang.reflect.java.IJavaType;
import gw.util.concurrent.LazyVar;
import gw.lang.reflect.IEnumValue;
import org.jschema.rpc.SimpleRPCCallHandler;
import org.jschema.util.JSchemaUtils;

import java.util.*;

import java.io.BufferedReader; 
import java.io.InputStreamReader;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.StreamHandler;

public class JSchemaTypeInfo extends TypeInfoBase {
  private static final String ENUM_KEY = "enum";
  
  private static final Map<String, IJavaType> TYPES = new HashMap<String, IJavaType>();
  static {
    TYPES.put("bigdecimal", IJavaType.BIGDECIMAL);
    TYPES.put("biginteger", IJavaType.BIGINTEGER);
    TYPES.put("decimal", IJavaType.DOUBLE);
    TYPES.put("integer", IJavaType.INTEGER);
    TYPES.put("string", IJavaType.STRING);
    TYPES.put("date", IJavaType.DATE);
    TYPES.put("boolean", IJavaType.BOOLEAN);
    TYPES.put("enum", IJavaType.ENUM);
    TYPES.put("map_of", IJavaType.MAP);
  }

  private JSchemaType owner;
  private Map json;
  private Map<String, String> propertyNames = new HashMap<String, String>();
  private List<IPropertyInfo> properties;
  private LazyVar<List<IMethodInfo>> methods = new LazyVar<List<IMethodInfo>>() {
    @Override
    protected List<IMethodInfo> init() {
      List<IMethodInfo> typeMethods = new ArrayList<IMethodInfo>();
    	typeMethods.add(new MethodInfoBuilder()
    		.withName("write")
    		.withReturnType(IJavaType.STRING)
    		.withStatic(true)
    		.withCallHandler(new IMethodCallHandler() {
    			@Override
    			public Object handleCall(Object ctx, Object... args) {
    				Json me = (Json)ctx;
    				return me.serialize(-1);
    			}
    		})
    		.build(JSchemaTypeInfo.this));
    	typeMethods.add(new MethodInfoBuilder()
    		.withName("prettyPrint")
    		.withReturnType(IJavaType.STRING)
    		.withStatic(true)
    		.withCallHandler(new IMethodCallHandler() {
    			@Override
    			public Object handleCall(Object ctx, Object... args) {
    				Json me = (Json)ctx;
    				return me.serialize(2);
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
    				String content = (String)args[0];
    				try {
    					return new Json(content, JSchemaTypeInfo.this);
    				} catch (Exception e) {
    					throw new RuntimeException(e);
    				}
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
    				  java.net.URL content = (java.net.URL)args[0];
      				BufferedReader reader = new BufferedReader(new InputStreamReader(content.openConnection().getInputStream()));
      				StringBuilder builder = new StringBuilder();
      				String line = reader.readLine();
      				while (line != null) {
      				  builder.append(line);
      				  line = reader.readLine();
      				}
    					return new Json(builder.toString(), JSchemaTypeInfo.this);
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
            return new Json(SimpleRPCCallHandler.doGet((String) args[0], fixedArgs), JSchemaTypeInfo.this);
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
            return new Json(SimpleRPCCallHandler.doPost((String) args[0], fixedArgs), JSchemaTypeInfo.this);
          }
        })
        .build(JSchemaTypeInfo.this));

    	return typeMethods;
    }
    
    private MethodInfoBuilder parseMethod() {
      return new MethodInfoBuilder()
        .withName("parse")
        .withReturnType(getOwnersType())
        .withStatic(true);
    }
  };

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
  
  private PropertyInfoBuilder create(final String originalName) {
    String propertyName = JSchemaUtils.convertJSONStringToGosuIdentifier(originalName);
	  propertyNames.put(propertyName, originalName);
    return new PropertyInfoBuilder()
      .withName(propertyName).withWritable(true)
      .withAccessor(new IPropertyAccessor() {
      	@Override
      	public void setValue(Object ctx, Object value) {
      		Json json = (Json) ctx;
      		try {
      			json.put(originalName, value);
      		} catch (Exception e) {
      			throw new RuntimeException(e);
      		}
      	}

      	@Override
      	public Object getValue(Object ctx) {
      		try {
      		  Object o = ((Json) ctx).get(originalName);
      		  if (o instanceof List) {
      		  }
      			return ((Json) ctx).get(originalName);
      		} catch (Exception e) {
      			throw new RuntimeException(e);
      		}
      	}
      });
  }

	private IPropertyInfo createWithType(final String name, IType type) {
	  PropertyInfoBuilder property = create(name);
	  return property.withType(type).build(this);
	}

	private IPropertyInfo createWithListType(final String name, IType type) {
	  PropertyInfoBuilder property = create(name);
	  return property.withType(IJavaType.ARRAYLIST.getParameterizedType(new IType[] { type })).build(this);
	}
	
	private IPropertyInfo createWithMapType(final String name, IType value) {
	  PropertyInfoBuilder property = create(name);
	  return property.withType(IJavaType.HASHMAP.getParameterizedType(IJavaType.STRING, value)).build(this);
	}

  public IType findNamedType(String typeName) {
    if (typeName.equals("self")) {
      return getOwnersType();
    }
    IJavaType javaType = findJavaType(typeName);
    if (javaType != null) {
      return javaType;
    }

    String typeDefType = getOwnersType().getTypeDefs().get(typeName);
    if (typeDefType != null) {
      return TypeSystem.getByFullName(typeDefType);
    }

    return null;
  }

	public static IJavaType findJavaType(String typeName) {
	  IJavaType t = TYPES.get(typeName);
	  if (t == null) {
	    return null;
	  }
	  return t;
	}
	
	private void createEnumProperties(final String key, List enumCodes) {
	  JSchemaEnumType type = (JSchemaEnumType)getOwnersType();
    try {
      for (int i = 0; i < enumCodes.size(); i++) {
        final String code = (String)enumCodes.get(i);
        final String enumified = JSchemaEnumType.enumify(code);
        final IEnumValue value = type.getEnumValue(enumified);
        
    	  propertyNames.put(enumified, code);
        PropertyInfoBuilder property = new PropertyInfoBuilder()
          .withName(enumified).withWritable(false)
          .withAccessor(new IPropertyAccessor() {
          	@Override
          	public Object getValue(Object ctx) {
          		try {
          			return value;
          		} catch (Exception e) {
          			throw new RuntimeException(e);
          		}
          	}
          	
          	@Override
          	public void setValue(Object ctx, Object value) {
          		throw new UnsupportedOperationException("Cannot set an enum");
          	}
          });
        
        properties.add(property.withStatic(true)
                .withType(type).build(this));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
	}
	
	private void createProperties() {
    properties = new ArrayList<IPropertyInfo>();
    for (Object k : json.keySet()) {
      String key = (String)k;
      Object value = json.get(key);
      if (key.equals(ENUM_KEY)) {
        createEnumProperties(key, (java.util.List)value);
        continue;
      }
      if (value instanceof Map) {
        Map val = (Map)value;
        if (val.get("map_of") != null) {
          Object o = val.get("map_of");
          addMapProperty(key, o);
          continue;
        }
        IType propertyType = findIType(key);
        properties.add(createWithType(key, propertyType));
        continue;
      } else if (value instanceof List) {
    		Object firstEntry = ((List)value).get(0);
    		if (firstEntry instanceof Map) {
    			IType propertyType = findIType(key);
    			if (propertyType == null) {
    				throw new RuntimeException("No type found for " + key + " with owner of " + getOwnersType());
    			}
    			properties.add(createWithListType(key, propertyType));
        } else {
          properties.add(createWithListType(key, findNamedType((String) firstEntry)));
        }
    	  continue;
    	}
      
      if (!(value instanceof String)) {
        throw new RuntimeException("Unless a JSON Object or a JSON Array, the value should be" +
          " a string name of the desired type");
      }
      
      IType type = findNamedType((String) value);
      if (type != null) {
        properties.add(createWithType(key, type));
      }
      if (type == IJavaType.ENUM) {
        continue; //no properties for the enum, they're at the type level?
      } else if (json.get(key) == null) {
    		Logger.getLogger(getClass().getName()).log(Level.FINE, 
    		  "Cannot handle NULL values. No property created.");
    	}
    }
	}
	
	private IType findIType(String key) {
    return TypeSystem.getByFullName(getOwnersType().getName() + "." + JSchemaUtils.convertJSONStringToGosuIdentifier(key));
  }
	
	private void addMapProperty(String key, Object o) {
    IType valueType = null;
    if (o instanceof Map) {
      IType ownerType = getOwnersType();
      valueType = findIType(key);
    } else {
      valueType = findNamedType((String) o);
    }
    properties.add(createWithMapType(key, valueType));
	}

	private IConstructorInfo defaultConstructor = new ConstructorInfoBuilder()
			.withConstructorHandler(new IConstructorHandler() {
				@Override
				public Object newInstance(Object... args) {
					Json j = new Json(JSchemaTypeInfo.this.getOwnersType());
					return j;
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
	
	public String getJsonPropertyName(String propertyName) {
	  return propertyNames.get(propertyName);
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
	public JSchemaType getOwnersType() {
		return owner;
	}

	public String toString() {
		return properties.toString();
	}
}
