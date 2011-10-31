package org.jschema.typeloader;

import gw.lang.reflect.*;

import java.util.HashSet;
import java.util.Map;

public class JSchemaType extends JSchemaTypeBase implements IJsonType {

  private Map<String, String> _typeDefs;

  public JSchemaType(String name, ITypeLoader typeloader, final Object object, Map<String, String> typeDefs) {
    super(name, typeloader, object);
    _typeDefs = typeDefs;
  }

  public Map<String, String> getTypeDefs() {
    return _typeDefs;
  }

  protected JSchemaTypeInfo initTypeInfo(Object object) {
    return new JSchemaTypeInfo(JSchemaType.this, object);
  }

  @Override
  public IType getTypeForJsonSlot(String key) {
    return getTypeInfo().getTypeForJsonSlot(key);
  }

  @Override
  public String getJsonSlotForPropertyName(String propName) {
    return getTypeInfo().getJsonSlotForPropertyName(propName);
  }

  @Override
  public JSchemaTypeInfo getTypeInfo() {
    return (JSchemaTypeInfo) super.getTypeInfo();
  }

  public IType getSelfType() {
    IType byFullNameIfValid = TypeSystem.getByFullNameIfValid(getNamespace());
    if (byFullNameIfValid instanceof JSchemaType) {
      return ((JSchemaType) byFullNameIfValid).getSelfType();
    } else if (byFullNameIfValid == null) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  public boolean isAssignableFrom(IType type) {
    if (super.isAssignableFrom(type)) {
      return true;
    } else {
      return hasSamePropsAsMe(type);
    }
  }

  ThreadLocal<HashSet<IType>> activeComparisonsThreadLocal = new ThreadLocal<HashSet<IType>>();
  private boolean hasSamePropsAsMe(IType type) {
    if (type instanceof IJSchemaType) {

      boolean instantiatedThreadLocalSet = false;
      try {
        HashSet<IType> activeComparisons = activeComparisonsThreadLocal.get();
        if (activeComparisons == null) {
          instantiatedThreadLocalSet = true;
          activeComparisons = new HashSet<IType>();
          activeComparisonsThreadLocal.set(activeComparisons);
        } else if (activeComparisons.contains(type)) {
          return true;
        }

        activeComparisons.add(type);

        for (IPropertyInfo pi : getTypeInfo().getProperties()) {
          IPropertyInfo property = type.getTypeInfo().getProperty(pi.getName());
          if (property == null) {
            return false;
          }
          if (!pi.getFeatureType().isAssignableFrom(property.getFeatureType())) {
            return false;
          }
        }
      } finally {
        if (instantiatedThreadLocalSet) {
          activeComparisonsThreadLocal.set(null);
        }
      }

      return true;
    } else {
      return false;
    }
  }
}
