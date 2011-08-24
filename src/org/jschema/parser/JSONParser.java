package org.jschema.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class JSONParser {

  private JSONToken _currentToken;

  public JSONParser(String json) {
    _currentToken = JSONToken.tokenize(json).removeTokens(JSONTokenType.COMMENT);
  }

  public static Object parseJSON(String json) {
    JSONParser jsonParser = new JSONParser(json);
    return jsonParser.parseValue();
  }

  public static String serializeJSON(Object json) {
    return buildJSON(new StringBuilder(), json).toString();
  }

  private static StringBuilder buildJSON(StringBuilder stringBuilder, Object json) {
    if (json instanceof String) {
      return stringBuilder.append("\"").append(json).append('\"');
    } else if (json instanceof Integer ||
      json instanceof Double ||
      json instanceof Long ||
      json instanceof BigDecimal ||
      json instanceof BigInteger) {
      return stringBuilder.append(json.toString());
    } else if (json instanceof Boolean) {
      return stringBuilder.append(json.toString());
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
        buildJSON(stringBuilder, listValue);
      }
      return stringBuilder.append("]");
    } else if (json instanceof Map) {
      Map map = (Map) json;
      Set<Map.Entry> set = map.entrySet();
      stringBuilder.append("{");
      for (Iterator<Map.Entry> iterator = set.iterator(); iterator.hasNext(); ) {
        Map.Entry entry = iterator.next();
        Object key = entry.getKey();
        if (!(key instanceof String)) {
          throw new IllegalArgumentException("All keys in a map must be of type string, but found : " + json);
        }
        buildJSON(stringBuilder, key.toString());
        stringBuilder.append(" : ");
        buildJSON(stringBuilder, entry.getValue());
        if (iterator.hasNext()) {
          stringBuilder.append(", ");
        }
      }
      return stringBuilder.append("}");
    } else {
      throw new IllegalArgumentException("Do not know how to serialize object : " + json);
    }
  }

  public Object parseValue() {

    String str = parseString();
    if (str != null ) {
      return str;
    } 

    Number number = parseNumber();
    if (number != null) {
      return number;
    }

    if (match("true")) {
      return Boolean.TRUE;
    }

    if (match("false")) {
      return Boolean.FALSE;
    }

    if (match("null")) {
      return null;
    }

    Map object = parseObject();
    if (object != null ) {
      return object;
    }

    List arr = parseArray();
    if (arr != null) {
      return arr;
    }

    badToken();
    return null;
  }

  private Number parseNumber() {
    boolean leadingNegative = false;
    if (match("-")) {
      leadingNegative = true;
    }
    if (_currentToken.isNumber()) {
      String value = _currentToken.getValue();
      consumeToken();
      if (value.contains(".") || value.contains("e") || value.contains("E")) {
        if (leadingNegative) {
          return Double.parseDouble("-" + value);
        } else {
          return Double.parseDouble(value);
        }
      } else {
        try {
          if (leadingNegative) {
            return Integer.parseInt("-" + value);
          } else {
            return Integer.parseInt(value);
          }
        } catch (NumberFormatException e) {
          try {
            if (leadingNegative) {
              return Long.parseLong("-" + value);
            } else {
              return Long.parseLong(value);
            }
          } catch (NumberFormatException e1) {
            if (leadingNegative) {
              return new BigInteger("-" + value);
            } else {
              return new BigInteger(value);
            }
          }
        }
      }
    } else if (leadingNegative) {
      badToken();
    }
    return null;
  }

  private String parseString() {
    if (_currentToken.isString()) {
      String value = _currentToken.getValue();
      consumeToken();
      return value.substring(1, value.length() - 1);
    } else {
      return  null;
    }
  }

  private List parseArray() {
    if (match("[")) {
      if (match("]")) {
        return Collections.EMPTY_LIST;
      } else {
        List lst = new ArrayList();
        do {
          lst.add(parseValue());
        } while (match(","));
        if (!match("]")) {
          badToken();
        }
        return lst;
      }
    }
    return null;
  }

  private Map parseObject() {
    if (match("{")) {
      if (match("}")) {
        return Collections.EMPTY_MAP;
      } else {
        Map map = new LinkedHashMap();
        do {
          String key = parseString();

          if (key == null) {
            badToken();
          }

          if (!match(":")) {
            badToken();
          }

          map.put(key, parseValue());
        } while (match(","));
        if (!match("}")) {
          badToken();
        }
        return map;
      }
    }
    return null;
  }

  private boolean match(String val) {
    boolean match = _currentToken.match(val);
    if (match) {
      consumeToken();
    }
    return match;
  }

  private void consumeToken() {
    _currentToken = _currentToken.nextToken();
  }

  private void badToken() {
    throw new IllegalStateException("Unexpected token '" + _currentToken.getValue() + "' at line " + _currentToken.getLine() + ", column " + _currentToken.getColumn());
  }

}
