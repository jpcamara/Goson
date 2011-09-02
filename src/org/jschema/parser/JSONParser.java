package org.jschema.parser;

import gw.lang.reflect.IType;
import org.jschema.model.JsonMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class JSONParser {

  private JSONToken _currentToken;

  public JSONParser(String json) {
    _currentToken = JSONToken.tokenize(json).removeTokens(JSONTokenType.COMMENT);
  }

  public static Object parseJSON(String json, IType rootType) {
    JSONParser jsonParser = new JSONParser(json);
    return jsonParser.parseValue();
  }

  public static Object parseJSON(String json) {
    return parseJSON(json, null);
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
      JSONToken value = _currentToken;
      consumeToken();
      return unescapeStringLiteral(value);
    } else {
      return  null;
    }
  }

  private String unescapeStringLiteral(JSONToken token) {
    String value = token.getValue();
    StringBuilder result = new StringBuilder();
    for (int i = 1; i < value.length() - 1; i++) {
      char c = value.charAt(i);
      if (c == '\\') {
        c = value.charAt(++i);
        if (c == '"') {
          result.append('"');
        } else if (c == '\\') {
          result.append('\\');
        } else if (c == '/') {
          result.append('/');
        } else if (c == 'b') {
          result.append('\b');
        } else if (c == 'f') {
          result.append('\f');
        } else if (c == 'n') {
          result.append('\n');
        } else if (c == 'r') {
          result.append('\r');
        } else if (c == 't') {
          result.append('\t');
        } else if (c == 'u') {
          if (i + 4 < value.length()) {
            String hexValue = "" + value.charAt(++i) + value.charAt(++i) + value.charAt(++i) + value.charAt(++i);
            result.append((char) Integer.parseInt(hexValue, 16));
          }
        }
      } else {
        result.append(c);
      }
    }
    return result.toString();
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
        Map map = new JsonMap();
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
    throw new JsonParseException("Unexpected token '" + _currentToken.getValue() + "' at line " + _currentToken.getLine() + ", column " + _currentToken.getColumn());
  }

}
