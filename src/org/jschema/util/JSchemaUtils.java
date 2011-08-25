package org.jschema.util;

import gw.lang.reflect.IType;
import gw.util.GosuEscapeUtil;
import gw.util.GosuStringUtil;
import org.jschema.parser.JSONParser;
import org.jschema.typeloader.IJsonType;
import org.jschema.typeloader.Json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class JSchemaUtils {

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
    return "'{' \"@@exception\" : \"" + GosuEscapeUtil.escapeForGosuStringLiteral(msg) + "\" }";
  }

  public static String createExceptionJSON(String msg, String trace) {
    return "{ \"@@exception\" : \"" + GosuEscapeUtil.escapeForGosuStringLiteral(msg) + "\"," +
      " \"@@trace\" : \"" + GosuEscapeUtil.escapeForGosuStringLiteral(trace) + "\"  }";

  }

  public static Object parseJson(String json, IType rootType) {
    if (rootType instanceof IJsonType) {
      return new Json(JSONParser.parseJSON(json), rootType);
    } else {
      return JSONParser.parseJSON(json);
    }
  }

  public static String serializeJson(Object value) {
    if (value instanceof Json) {
      return ((Json) value).serialize(0);
    } else {
      return JSONParser.serializeJSON(value);
    }
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
}
