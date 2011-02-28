package com.jpcamara.gosu.json;

import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IConstructorInfo;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.PropertyInfoBuilder;
import gw.lang.reflect.TypeInfoBase;
import gw.lang.reflect.IRelativeTypeInfo.Accessibility;
import gw.lang.reflect.java.IJavaType;
import gw.util.concurrent.LazyVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonTypeInfo extends TypeInfoBase {

	private JsonType owner;
	private Json json;
	private List<IPropertyInfo> properties;
	private LazyVar<List<IMethodInfo>> methods = new LazyVar<List<IMethodInfo>>() {
		private List<IMethodInfo> typeMethods = new ArrayList<IMethodInfo>();
		
		@Override
		protected List<IMethodInfo> init() {
			typeMethods.add(new MethodInfoBuilder()
				.withName("write")
				.withReturnType(IJavaType.STRING)
				.withStatic(true)
				.withCallHandler(new IMethodCallHandler() {
					@Override
					public Object handleCall(Object ctx, Object... args) {
						Json me = (Json)ctx;
						return me.serialize();
					}
				})
				.build(JsonTypeInfo.this));
			typeMethods.add(new MethodInfoBuilder()
				.withName("parse")
				.withReturnType(getOwnersType())
				.withStatic(true)
//				.withParameters(new ParameterInfoBuilder()
//					.like(new IParameterInfo() {
//						
//					}))
////					.withDescription("target"))
				.withCallHandler(new IMethodCallHandler() {
					@Override
					public Object handleCall(Object ctx, Object... args) {
						// TODO Auto-generated method stub
						return null;
					}
				})
				.build(JsonTypeInfo.this));
			return typeMethods;
		}
	};

	public JsonTypeInfo(JsonType owner, Json object) {
		this.owner = owner;
		this.json = object;
		createProperties();
	}

	private IPropertyInfo createWithType(final String name, IType type) {
		return new PropertyInfoBuilder()
				.withName(new JsonName(name).getName()).withWritable(true)
				.withType(type).withAccessor(new IPropertyAccessor() {
					@Override
					public void setValue(Object ctx, Object value) {
						Json json = (Json) ctx;
						try {
							json.put(name, value);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public Object getValue(Object ctx) {
						try {
							return ((Json) ctx).get(name);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}).build(this);
	}

	private IPropertyInfo createWithListType(final String name, IType type) {
		return new PropertyInfoBuilder()
				.withName(new JsonName(name).getName()).withWritable(true)
				.withType(IJavaType.LIST.getParameterizedType(type)).withAccessor(new IPropertyAccessor() {
					@Override
					public void setValue(Object ctx, Object value) {
						Json json = (Json) ctx;
						try {
//							List values = (List)value;
							json.put(name, value);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public Object getValue(Object ctx) {
						try {
							return ((Json) ctx).get(name);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}).build(this);
	}
	
	private void createProperties() {
		properties = new ArrayList<IPropertyInfo>();
		for (String key : json.keys()) {
			if (json.get(key) instanceof String) {
				properties.add(createWithType(key, IJavaType.STRING));
			} else if (Json.isJSONObject(json.get(key))) {
				JsonType type = getOwnersType();
				IType propertyType = type.getTypeLoader()
					.getType(type.getNamespace() + "." + new JsonName(key).getName());
				properties.add(createWithType(key, propertyType));
			} else if (json.get(key) instanceof Integer) {
				properties.add(createWithType(key, IJavaType.INTEGER));
			} else if (json.get(key) instanceof Double) {
				properties.add(createWithType(key, IJavaType.DOUBLE));
			} else if (json.get(key) instanceof Boolean) {
				properties.add(createWithType(key, IJavaType.BOOLEAN));

			} else if (Json.isJSONArray(json.get(key))) {
				Object firstEntry = json.getWithIndex(key, 0);
				
				if (firstEntry instanceof String) {
					properties.add(createWithListType(key, IJavaType.STRING));
				} else if (firstEntry instanceof Integer) {
					properties.add(createWithListType(key, IJavaType.INTEGER));
				} else if (firstEntry instanceof Double) {
					properties.add(createWithListType(key, IJavaType.DOUBLE));
				} else if (firstEntry instanceof Boolean) {
					properties.add(createWithListType(key, IJavaType.BOOLEAN));
				} else {
					JsonType type = getOwnersType();
					IType propertyType = type.getTypeLoader()
							.getType(type.getNamespace() + "." + new JsonName(key).getName());
					if (propertyType == null) {
						throw new RuntimeException("No type found");
					}
					properties.add(createWithListType(key, propertyType));
				}
			} else if (Json.isJSONNull(json.get(key))) {
				System.out.println("can't handle nulls");
			}
		}
	}

	private IConstructorInfo defaultConstructor = new ConstructorInfoBuilder()
			.withConstructorHandler(new IConstructorHandler() {
				@Override
				public Object newInstance(Object... args) {
					Json j = new Json();
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

	@Override
	public List<IAnnotationInfo> getDeclaredAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasAnnotation(IType type) {
		return false;
	}

	@Override
	public JsonType getOwnersType() {
		return owner;
	}

	public String toString() {
		return properties.toString();
	}
}
