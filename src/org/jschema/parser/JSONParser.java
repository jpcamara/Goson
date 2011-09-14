package org.jschema.parser;

import gw.internal.gosu.parser.TypeLord;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import org.jschema.model.JsonList;
import org.jschema.model.JsonMap;
import org.jschema.typeloader.IJSchemaType;
import org.jschema.util.JSchemaUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class JSONParser {

  private JSONToken _currentToken;
  private IType _currentType;

  public JSONParser(String json, IType rootType) {
    _currentToken = JSONToken.tokenize(json).removeTokens(JSONTokenType.COMMENT);
    _currentType = rootType;
  }

  public static Object parseJSON(String json, IType rootType) {
    JSONParser jsonParser = new JSONParser(json, rootType);
    return jsonParser.parseValue();
  }

  public static Object parseJSON(String json) {
    return parseJSON(json, null);
  }

  public Object parseValue() {

    Date date = parseDate();
    if (date != null ) {
      return date;
    } 

    URI uri = parseURI();
    if (uri != null ) {
      return uri;
    }

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

  private Date parseDate() {
    if (IJavaType.DATE.equals(_currentType)) {
      String s = parseString();
      if (s != null) {
        return JSchemaUtils.parseDate(s);
      }
    }
    return null;
  }

  private URI parseURI() {
    if (TypeSystem.get(URI.class).equals(_currentType)) {
      String s = parseString();
      if (s != null) {
        try {
          return JSchemaUtils.parseURI(s);
        } catch (URISyntaxException e) {
          //TODO parse error
        }
      }
    }
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
      if (value.contains(".") || value.contains("e") || value.contains("E") || IJavaType.BIGDECIMAL.equals(_currentType)) {
        if (leadingNegative) {
          return new BigDecimal("-" + value);
        } else {
          return new BigDecimal(value);
        }
      } else {
        if (leadingNegative) {
          return Long.parseLong("-" + value);
        } else {
          return Long.parseLong(value);
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
        List lst = new JsonList(_currentType);
        IType lstType = _currentType;
        try {
          if (lstType != null) {
            IType parameterizedType = TypeLord.findParameterizedType(lstType, IJavaType.LIST.getGenericType());
            if (parameterizedType != null) {
             _currentType = parameterizedType.getTypeParameters()[0];
            }
          }
          do {
            lst.add(parseValue());
          } while (match(","));
        } finally {
          _currentType = lstType;
        }
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
        JsonMap map = new JsonMap(_currentType);

        IType ctxType = _currentType;

        IJSchemaType jschemaType = null;
        if (_currentType instanceof IJSchemaType) {
          jschemaType = (IJSchemaType) _currentType;
        }

        IType mapValueType = null;
        if (ctxType != null && IJavaType.MAP.isAssignableFrom(ctxType)) {
          IType parameterizedType = TypeLord.findParameterizedType(ctxType, IJavaType.MAP.getGenericType());
          if (parameterizedType != null) {
            mapValueType = parameterizedType.getTypeParameters()[1];
          }
        }

        try {
          do {
            String key = parseString();

            if (key == null) {
              badToken();
            }

            if (!match(":")) {
              badToken();
            }

            if (jschemaType != null) {
              _currentType = jschemaType.getTypeForJsonSlot(key);
            } else if (mapValueType != null) {
              _currentType = mapValueType;
            }
            map.put(key, parseValue());
          } while (match(","));
        } finally {
          _currentType = ctxType;
        }

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
