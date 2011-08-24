package org.jschema.util;

import gw.lang.reflect.IType;
import gw.util.GosuEscapeUtil;
import gw.util.GosuStringUtil;
import org.jschema.parser.JSONParser;
import org.jschema.typeloader.IJsonType;
import org.jschema.typeloader.Json;

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
}
