package org.jschema.util;

import gw.lang.reflect.*;
import gw.lang.reflect.gs.IGenericTypeVariable;
import gw.util.GosuEscapeUtil;
import gw.util.GosuStringUtil;
import org.jschema.model.JsonList;
import org.jschema.model.JsonMap;
import org.jschema.parser.JSONParser;
import org.jschema.typeloader.IJSchemaType;
import org.jschema.typeloader.JSchemaType;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class JSchemaUtils {

  public static final String JSCHEMA_EXCEPTION_KEY = "exception@";
  public static final String JSCHEMA_TRACE_KEY = "trace@";
  public static final String JSCHEMA_EXCEPTION_TYPE_KEY = "exception_type@";
  public static final String JSCHEMA_TYPEDEFS_KEY = "typedefs@";
  public static final String JSCHEMA_REF_KEY = "ref@";
  public static final String JSCHEMA_ENUM_KEY = "enum";
  public static final String JSCHEMA_MAP_KEY = "map_of";


  //TODO use Character.isJavaIdentifierPart() to scrub bad characters?
  public static String convertJSONStringToGosuIdentifier(String name) {
    return convertJSONStringToGosuIdentifier(name, true);
  }

  public static String convertJSONStringToGosuIdentifier(String name, boolean capitalizeFirst) {
    StringBuilder typeName = new StringBuilder();
    String normalizedName = name.replaceAll("\\s", "_");
    for (String piece : normalizedName.split("_")) {
      if (capitalizeFirst) {
        typeName.append(GosuStringUtil.capitalize(piece));
      }
      else
      {
        capitalizeFirst = true;
        typeName.append(piece);
      }
    }
    return typeName.toString();
  }

  public static String createExceptionJSON(String msg) {
    msg = msg == null ? "null" : msg;
    return "'{' \"" + JSCHEMA_EXCEPTION_KEY + "\" : \"" + GosuEscapeUtil.escapeForGosuStringLiteral(msg) + "\" }";
  }

  public static String createExceptionJSON(String msg, String trace) {
    msg = msg == null ? "null" : msg;
    return "{ \"" + JSCHEMA_EXCEPTION_KEY + "\" : \"" + GosuEscapeUtil.escapeForGosuStringLiteral(msg) + "\"," +
      " \"" + JSCHEMA_TRACE_KEY + "\" : \"" + GosuEscapeUtil.escapeForGosuStringLiteral(trace) + "\"  }";
  }

  public static String createExceptionJSON(String msg, String type, String trace) {
    msg = msg == null ? "null" : msg;
    return "{ \"" + JSCHEMA_EXCEPTION_KEY + "\" : \"" + GosuEscapeUtil.escapeForGosuStringLiteral(msg) + "\"," +
      " \"" + JSCHEMA_EXCEPTION_TYPE_KEY  + "\" : \"" + type + "\"," +
      " \"" + JSCHEMA_TRACE_KEY + "\" : \"" + GosuEscapeUtil.escapeForGosuStringLiteral(trace) + "\"  }";
  }

  public static Object parseJson(String json, IType rootType) {
    return JSONParser.parseJSON(json, rootType);
  }

  public static String serializeJson(Object json) {
    return serializeJson(json, -1);
  }

  public static String serializeJson(Object json, int indent) {
    return buildJSON(new StringBuilder(), json, indent, 0).toString();
  }

  private static StringBuilder buildJSON(StringBuilder stringBuilder, Object json, int indent, int depth) {
    if (json instanceof String) {
      stringBuilder.append("\"");
      appendCharacters(stringBuilder, (String) json);
      return stringBuilder.append('\"');
    } else if (json instanceof Integer ||
      json instanceof Double ||
      json instanceof Long ||
      json instanceof BigDecimal ||
      json instanceof BigInteger) {
      return stringBuilder.append(json.toString());
    } else if (json instanceof Boolean) {
      return stringBuilder.append(json.toString());
    } else if (json instanceof Date) {
      // TODO cgross - properly serialize dates
      return buildJSON(stringBuilder, json.toString(), indent, depth);
    } else if (json instanceof IEnumValue) {
      return buildJSON(stringBuilder, ((IEnumValue) json).getValue(), indent, depth);
    } else if (json == null) {
      return stringBuilder.append("null");
    } else if (json instanceof List) {
      stringBuilder.append("[");
      List lst = (List) json;
      for (int i = 0, lstSize = lst.size(); i < lstSize; i++) {
        if (i != 0) {
          stringBuilder.append(", ");
        }
        Object listValue = lst.get(i);
        if (listValue instanceof Map && indent >= 0 && ((Map) listValue).size() > 0) {
          stringBuilder.append("\n");
          addWhitespace(stringBuilder, indent, depth);
        }
        buildJSON(stringBuilder, listValue, indent, depth + 1);
      }
      if (indent >= 0 && lst.size() > 0 && lst.get(lst.size() - 1) instanceof Map && ((Map) lst.get(lst.size() - 1)).size() > 0) {
        stringBuilder.append("\n");
        addWhitespace(stringBuilder, indent, depth - 1);
      }
      return stringBuilder.append("]");
    } else if (json instanceof Map) {
      Map map = (Map) json;
      Set<Map.Entry> set = map.entrySet();
      stringBuilder.append("{");
      if (map.size() > 0 && indent >= 0) {
        stringBuilder.append("\n");
      }
      for (Iterator<Map.Entry> iterator = set.iterator(); iterator.hasNext(); ) {
        Map.Entry entry = iterator.next();
        Object key = entry.getKey();
        if (!(key instanceof String)) {
          throw new IllegalArgumentException("All keys in a map must be of type string, but found : " + json);
        }
        addWhitespace(stringBuilder, indent, depth);
        buildJSON(stringBuilder, entry.getKey().toString(), indent, depth);
        stringBuilder.append(" : ");
        buildJSON(stringBuilder, entry.getValue(), indent, depth + 1);
        if (iterator.hasNext()) {
          stringBuilder.append(", ");
          if (indent >= 0) {
            stringBuilder.append("\n");
          }
        }
      }
      if (set.size() > 0 && indent >= 0) {
        stringBuilder.append("\n");
        addWhitespace(stringBuilder, indent, depth - 1);
      }
      return stringBuilder.append("}");
    } else {
      throw new IllegalArgumentException("Do not know how to serialize object : " + json);
    }
  }

  private static void addWhitespace(StringBuilder stringBuilder, int indent, int depth) {
    if (indent > 0) {
      int fullindent = indent * (depth + 1);
      while (fullindent-- > 0) {
        stringBuilder.append(" ");
      }
    }
  }

  private static void appendCharacters(StringBuilder result, String value) {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '\"') {
        result.append("\\\"");
      } else if (c == '\\') {
        result.append("\\\\");
      } else if (c == '\b') {
        result.append("\\b");
      } else if (c == '\f') {
        result.append("\\f");
      } else if (c == '\n') {
        result.append("\\n");
      } else if (c == '\r') {
        result.append("\\r");
      } else if (c == '\t') {
        result.append("\\t");
      } else if (c > 0xfff) {
        result.append("\\u" + hex(c));
      } else if (c > 0xff) {
        result.append("\\u0" + hex(c));
      } else if (c > 0x7f) {
        result.append("\\u00" + hex(c));
      } else {
        result.append(c);
      }
    }
  }

  private static String hex(char c) {
    return Integer.toHexString(c).toUpperCase();
  }

  public static Object convertJsonToJSchema(Object json) {
    if (json instanceof List && !((List)json).isEmpty()) {
      List jsonList = (List)json;
      ListIterator it = jsonList.listIterator();
      while (it.hasNext()) {
        Object current = it.next();
        it.set(convertJsonToJSchema(current));
      }
    }
    if (json instanceof Integer ||
        json instanceof Long ||
        json instanceof BigInteger) {
      return "biginteger";
    } else if (json instanceof Double ||
        json instanceof BigDecimal) {
      return "bigdecimal";
    } else if (json instanceof String) {
      return "string";
    } else if (json instanceof Boolean) {
      return "boolean";
    } else if (json instanceof Map) {
      Map jsonMap = (Map)json;
      for (Object key : jsonMap.keySet()) {
        jsonMap.put(key, convertJsonToJSchema(jsonMap.get(key)));
      }
    }
    return json;
  }

  public static Date parseDate(String s) {
    return new Date(s);
  }

  public static Object cloneToType(IJSchemaType targetType, JsonMap fromMap, IJSchemaType fromType) {
    return deepCopy(new Stack<String>(), targetType, fromMap, fromType, new IdentityHashMap<Object, Object>());
  }

  private static Object deepCopy(Stack<String> propertyPath, IType toType, Object from, IType fromType, IdentityHashMap<Object, Object> serializationMap) {

    if (from == null) {
      return from;
    }
    
    // if we've already copied it, just return it
    Object previousValue = serializationMap.get(from);
    if (previousValue != null) {
      return previousValue;
    }

    if (toType instanceof IEnumType && fromType instanceof IEnumType) {
      String code = ((IEnumValue) from).getCode();
      IEnumValue enumValue = ((IEnumType) toType).getEnumValue(code);
      if (enumValue != null) {
        return enumValue;
      } else {
        throw new IllegalArgumentException("Enum mismatch at path : \'" + makePath(propertyPath) + "\', Didn't find Enum value '" + code + "' in Enum " + toType.getName());
      }
    } else if (toType instanceof IJSchemaType && fromType instanceof IJSchemaType) {
      return deepCopyJSchemaObject(propertyPath, (IJSchemaType) toType, (JsonMap) from, (IJSchemaType) fromType, serializationMap);
    } else if (TypeSystem.get(JsonList.class).isAssignableFrom(toType) &&
      TypeSystem.get(JsonList.class).isAssignableFrom(fromType)) {
      return deepCopyJsonList(propertyPath, toType, (JsonList) from, fromType, serializationMap);
    } else if (TypeSystem.get(JsonMap.class).isAssignableFrom(toType) &&
      TypeSystem.get(JsonMap.class).isAssignableFrom(fromType)) {
      return deepCopyJsonMap(propertyPath, toType, (JsonMap) from, fromType, serializationMap);
    } else {
      if (toType.isAssignableFrom(TypeSystem.getFromObject(from))) {
        return from;
      } else {
        throw new IllegalArgumentException("Type mismatch at path : \'" + makePath(propertyPath) + "\', Expected " + toType.getName() + " but found " + fromType.getName());
      }
    }
  }

  private static Object deepCopyJsonMap(Stack<String> propertyPath, IType toType, JsonMap from, IType fromType, IdentityHashMap<Object,Object> serializationMap) {
    IType toComponent = toType.getTypeParameters()[0];
    IType fromComponent = fromType.getTypeParameters()[0];
    JsonMap copy = new JsonMap(toType);
    serializationMap.put(from, copy);
    for (Object o : from.keySet()) {
      copy.put((String) o, deepCopy(propertyPath, toComponent, from.get(o), fromComponent, serializationMap));
    }
    return copy;
  }

  private static Object deepCopyJsonList(Stack<String> propertyPath, IType toType, JsonList from, IType fromType, IdentityHashMap<Object,Object> serializationMap) {
    IType toComponent = toType.getTypeParameters()[0];
    IType fromComponent = fromType.getTypeParameters()[0];
    JsonList copy = new JsonList(toType);
    serializationMap.put(from, copy);
    for (Object o : from) {
      copy.add(deepCopy(propertyPath, toComponent, o, fromComponent, serializationMap));
    }
    return copy;
  }

  private static Object deepCopyJSchemaObject(Stack<String> propertyPath, IJSchemaType toType, JsonMap from, IJSchemaType fromType, IdentityHashMap<Object, Object> serializationMap) {
    JsonMap to = new JsonMap(toType);
    serializationMap.put(from, to);
    List<? extends IPropertyInfo> properties = toType.getTypeInfo().getProperties();
    for (IPropertyInfo property : properties) {
      if (property.isWritable()) {
        String toSlotName = toType.getJsonSlotForPropertyName(property.getName());
        String fromSlotName = fromType.getJsonSlotForPropertyName(property.getName());
        IType fromPropertyType = fromType.getTypeForJsonSlot(fromSlotName);
        if (toSlotName != null && fromSlotName != null) {
          IType toPropertyType = toType.getTypeForJsonSlot(toSlotName);
          Object fromSlotValue = from.get(fromSlotName);
          propertyPath.push(property.getName());
          try {
            to.put(toSlotName, deepCopy(propertyPath, toPropertyType, fromSlotValue, fromPropertyType, serializationMap));
          } finally {
            propertyPath.pop();
          }
        }
      }
    }
    return to;
  }

  private static String makePath(Stack<String> propertyPath) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<String> iterator = propertyPath.iterator(); iterator.hasNext(); ) {
      String s = iterator.next();
      sb.append(s);
      if (iterator.hasNext()) {
        sb.append(".");
      }
    }
    return sb.toString();
  }
}
