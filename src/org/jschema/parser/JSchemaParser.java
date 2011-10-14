package org.jschema.parser;

import org.jschema.model.JsonMap;
import org.jschema.util.JSchemaUtils;

import java.util.Map;

public class JSchemaParser extends JSONParser {

  public JSchemaParser(String jschema) {
    super(jschema, null);
  }

  public Object parseJSchema()
  {
    Object retVal = parseJSONDocument();
    return(retVal);
  }

  @Override
  protected void putWithSemantics(JsonMap map, String key, Object value)
  {
    JsonParseError error = null;
    if(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY.equals(key)){
      if(value instanceof Map == false){
        // Real tight coupling. We've consumed the typedefs@ and the : and we're sitting on whatever is after that.
        JSONToken errorToken = _currentToken;
        int errorLine = -1;
        int errorCol = -1;
        while(errorToken != null && errorToken.match("typedefs@") == false){
          errorToken = errorToken.previous();
        }
        if(errorToken != null){
          errorLine = errorToken.getLine();
          errorCol = errorToken.getColumn();
        }

        error = new JsonParseError("typedefs@ at line " + errorLine + " column " + errorCol + " is not followed by an struct definition");
        _errors.add(error);
      }
    }
    if(error == null){
      super.putWithSemantics(map, key, value);
    }
  }
}
