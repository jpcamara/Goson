package org.jschema.parser;

import gw.internal.gosu.parser.TypeLord;
import gw.lang.reflect.IType;
import gw.lang.reflect.java.JavaTypes;
import org.jschema.model.JsonMap;
import org.jschema.typeloader.IJSchemaType;
import org.jschema.util.JSchemaUtils;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSchemaParser extends JSONParser {

  private boolean _processingTypedefs;

  public JSchemaParser(String jschema) {
    super(jschema, null);
  }

  public Object parseJSchema()
  {
    Object retVal = parseJSONDocument();
    return(retVal);
  }

  @Override
  protected Object putWithSemantics(JsonMap map, String key, Object value)
  {
    Object retVal = null;
    if(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY.equals(key)){
      processTypedefs(map, key, value);
    }
    else if(JSchemaUtils.JSCHEMA_FUNCTIONS_KEY.equals(key)){
      if(value instanceof List == false){
        ErrorInfo errorInfo = new ErrorInfo(JSchemaUtils.JSCHEMA_FUNCTIONS_KEY);
        JsonParseError error = new JsonParseError(MessageFormat.format("functions at line {0} column {1} is not followed by an array definition", errorInfo.getErrorLine(), errorInfo.getErrorCol()),
          _currentToken.getStart(), _currentToken.getEnd());
        _errors.add(error);
      }
      else{
        super.putWithSemantics(map, key, value);
      }
    }
    else{
      retVal = super.putWithSemantics(map, key, value);
      if(retVal != null){
        if(_processingTypedefs == true){
          ErrorInfo errorInfo = new ErrorInfo(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY);
          JsonParseError error = new JsonParseError(MessageFormat.format("duplicate type {0} declared at line {1} column {2}", key, errorInfo.getErrorLine(), errorInfo.getErrorCol()), _currentToken.getStart(), _currentToken.getEnd());
          _errors.add(error);
        }
        else{
          int errorLine = _currentToken.previousToken().getLine();
          int errorCol = _currentToken.previousToken().getColumn();
          JsonParseError error = new JsonParseError("duplicate type " + key + " declared at line " + errorLine + " column " + errorCol, _currentToken.getStart(), _currentToken.getEnd());
          _errors.add(error);
        }
      }
    }
    return(retVal);
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
            if(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY.equals(key)){
              _processingTypedefs = true;
            }
            Object value = parseValueImpl();
            putWithSemantics(map, key, value);
            if(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY.equals(key)){
              _processingTypedefs = false;
            }
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

  private void processTypedefs(JsonMap map, String key, Object value)
  {
    JsonParseError error = null;
    if(value instanceof Map == false){
      // Real tight coupling. We've consumed the typedefs@ and the : and we're sitting on whatever is after that.
      ErrorInfo errorInfo = new ErrorInfo(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY);
      error = new JsonParseError(MessageFormat.format("typedefs@ at line {0} column {1} is not followed by an struct definition", errorInfo.getErrorLine(), errorInfo.getErrorCol()), _currentToken.getStart(), _currentToken.getEnd());
      _errors.add(error);
    }

    if(error == null){
      // Handle multiple typedefs@ in at the same level in a single document
      JsonMap previousTypedefs = (JsonMap) map.get(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY);
      if(previousTypedefs != null){
        // Merge the typedefs
        JsonMap newTypedefs = (JsonMap) value;
        Set<? extends String> keys = newTypedefs.keySet();
        for(String newTypeName : keys){
          if(previousTypedefs.containsKey(newTypeName) == true){
            ErrorInfo errorInfo = new ErrorInfo(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY);
            error = new JsonParseError(MessageFormat.format("duplicate type {0} declared at line {1} column {2}", newTypeName, errorInfo.getErrorLine(), errorInfo.getErrorCol()), _currentToken.getStart(), _currentToken.getEnd());
            _errors.add(error);
          }
          else{
            previousTypedefs.putAll(newTypedefs);
          }
        }
      }
      else{
        map.put(key, value);
      }
    }
    return;
  }

  public List<JsonParseError> getErrors() {
    return _errors;
  }

  private class ErrorInfo{

    private int _errorLine;
    private int _errorCol;
    private int _errorStart;
    private int _errorEnd;

    public ErrorInfo(String keyOfErrorToken)
    {
      JSONToken errorToken = _currentToken;
      while(errorToken != null && errorToken.match(keyOfErrorToken) == false){
        errorToken = errorToken.previousToken();
      }
      _errorLine = -1;
      _errorCol = -1;
      _errorStart = 0;
      _errorEnd = 0;
      if(errorToken != null){
        _errorLine = errorToken.getLine();
        _errorCol = errorToken.getColumn();
        _errorStart = errorToken.getStart();
        _errorEnd = errorToken.getEnd();
      }
    }

    public int getErrorLine() {
      return _errorLine;
    }

    public int getErrorCol() {
      return _errorCol;
    }

    public int getErrorStart() {
      return _errorStart;
    }

    public int getErrorEnd() {
      return _errorEnd;
    }
  }
}
