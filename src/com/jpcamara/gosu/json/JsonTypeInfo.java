package com.jpcamara.gosu.json;

import gw.lang.parser.expressions.ITypeVariableDefinition;
import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IConstructorInfo;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.PropertyInfoBuilder;
import gw.lang.reflect.TypeInfoBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.IRelativeTypeInfo.Accessibility;
import gw.lang.reflect.gs.IGenericTypeVariable;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.IDefaultArrayType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonTypeInfo extends TypeInfoBase {

	private JsonType owner;
	private Json json;
	private List<IPropertyInfo> properties;

	public JsonTypeInfo(JsonType owner, Json object) {
		this.owner = owner;
		this.json = object;
		createProperties();
	}

	private IPropertyInfo createWithType(final String name, IType type) {
		return new PropertyInfoBuilder().withName(name).withWritable(true)
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

	private void createProperties() {
		properties = new ArrayList<IPropertyInfo>();
		for (String key : json.keys()) {
			if (json.get(key) instanceof String) {
				properties.add(createWithType(key, IJavaType.STRING));
			} else if (json.get(key) instanceof JSONObject) {
				JsonType type = getOwnersType();
				IType propertyType = type.getTypeLoader()
					.getType(type.getNamespace() + "." + key);
				properties.add(createWithType(key, propertyType));
			} else if (json.get(key) instanceof Integer) {
				properties.add(createWithType(key, IJavaType.INTEGER));
			} else if (json.get(key) instanceof Double) {
				properties.add(createWithType(key, IJavaType.DOUBLE));
			} else if (json.get(key) instanceof Boolean) {
				properties.add(createWithType(key, IJavaType.BOOLEAN));

			} else if (json.get(key) instanceof JSONArray) {
				JsonType type = getOwnersType();
				IType propertyType = type.getTypeLoader()
						.getType(type.getNamespace() + "." + key).getArrayType();
				properties.add(createWithType(key, propertyType));
			} else if (json.get(key) == org.json.JSONObject.NULL) {
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
		return Collections.emptyList();
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
		// TODO Auto-generated method stub
		for (IPropertyInfo prop : properties) {
			if (propName.equals(prop.getName())) {
				return prop.getName();
			}
		}
		return null;
	}

	@Override
	public Map<IType, List<IAnnotationInfo>> getDeclaredAnnotations() {
		return Collections.emptyMap();
	}

	@Override
	public boolean hasAnnotation(IType type) {
		// TODO Auto-generated method stub
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
