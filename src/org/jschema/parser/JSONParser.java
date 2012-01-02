package org.jschema.parser;

import gw.internal.gosu.parser.TypeLord;
import gw.lang.reflect.IEnumType;
import gw.lang.reflect.IEnumValue;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import org.jschema.model.JsonList;
import org.jschema.model.JsonMap;
import org.jschema.typeloader.IJSchemaType;
import org.jschema.util.JSchemaUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

public class JSONParser {

  protected JSONToken _currentToken;
  protected IType _currentType;
  protected List<JsonParseError> _errors = new ArrayList<JsonParseError>();
  private Object _retVal;

  public JSONParser(String json, IType rootType) {
    _currentToken = JSONToken.tokenize(json).removeTokens(JSONTokenType.COMMENT);
    _currentType = rootType;
    return;
  }

  public JSONParser(String json) {
    this(json, null);
    return;
  }

  /**
   * Parses a complete JSON document, which must start either an object ('{') or an array ('[')
   *
   * @return An object representative of the json document
   *
   * @throws JsonParseException if something goes wrong
   */

  public Object parseJSONDocument()
  {
    _errors.clear();
    _retVal = parseObject();
    if (_retVal == null ) {
      _retVal = parseArray();
    }
    if(_retVal != null){
      if(_currentToken.isEOF() == false){
        badToken();
      }
    }
    else{
      badToken();
    }

    if(_errors.size() != 0){
      throw(new JsonParseException(_errors));
    }
    return(_retVal);
  }

  public Object getValue() {
    return _retVal;
  }

  /**
   * Parses a JSON document fragment. In fact this can parse an entire JSON document, but it
   * doesn't demand that the formal arg json conform to the JSON grammar defined in the
   * RFC
   *
   * @return Really? You have to ask this?
   */

  public Object parseJSONFragment()
  {
    Object retVal;
    _errors.clear();
    retVal = parseValueImpl();
    if(_errors.size() != 0){
      throw(new JsonParseException(_errors));
    }
    return(retVal);
  }

  protected Object parseValueImpl()
  {
    Date date = parseDate();
    if (date != null ) {
      return date;
    } 

    URI uri = parseURI();
    if (uri != null ) {
      return uri;
    }

    Object enumValue = parseEnumValue();
    if (enumValue != null ) {
      return enumValue;
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
    if (JavaTypes.DATE().equals(_currentType)) {
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
      URI uri = null;
      if (s != null) {
        uri = JSchemaUtils.parseURI(s);
      }
      if (uri == null) {
        _errors.add(new JsonParseError("Bad URI value : " + s, _currentToken.getStart(), _currentToken.getEnd()));
      }
      return uri;
    }
    return null;
  }

  private Object parseEnumValue() {
    if (_currentType instanceof IEnumType) {
      String s = parseString();
      if (s != null) {
        List<IEnumValue> values = ((IEnumType) _currentType).getEnumValues();
        for (IEnumValue value : values) {
          if (value.getValue().equals(s)) {
            return value;
          }
        }
        _errors.add(new JsonParseError("Bad Enum Value for " + _currentType + " : " + s, _currentToken.getStart(), _currentToken.getEnd()));
        return s;
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
      if (value.contains(".") || value.contains("e") || value.contains("E") || JavaTypes.BIG_DECIMAL().equals(_currentType)) {
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

  protected String parseString() {
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
            IType parameterizedType = TypeLord.findParameterizedType(lstType, JavaTypes.LIST().getGenericType());
            if (parameterizedType != null) {
             _currentType = parameterizedType.getTypeParameters()[0];
            }
          }
          do {
            lst.add(parseValueImpl());
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

  protected Map parseObject() {
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
        if (ctxType != null && JavaTypes.MAP().isAssignableFrom(ctxType)) {
          IType parameterizedType = TypeLord.findParameterizedType(ctxType, JavaTypes.MAP().getGenericType());
          if (parameterizedType != null && parameterizedType.getTypeParameters() != null) {
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
            Object value = parseValueImpl();
            putWithSemantics(map, key, value);
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

  protected Object putWithSemantics(JsonMap map, String key, Object value)
  {
    Object retVal = map.put(key, value);
    return(retVal);
  }

  protected boolean match(String val) {
    boolean match = _currentToken.match(val);
    if (match) {
      consumeToken();
    }
    return match;
  }

  private void consumeToken() {
    _currentToken = _currentToken.nextToken();
  }

  protected void badToken() {
    JsonParseError error = new JsonParseError("Unexpected token '" + _currentToken.getValue() + "' at line " + _currentToken.getLine() + ", column " + _currentToken.getColumn(),
                                              _currentToken.getStart(), _currentToken.getEnd());
    _errors.add(error);
  }
}
