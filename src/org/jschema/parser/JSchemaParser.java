package org.jschema.parser;

import org.jschema.model.JsonMap;
import org.jschema.util.JSchemaUtils;
import sun.java2d.SunGraphicsEnvironment;

import java.util.Map;
import java.util.Set;

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
    if(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY.equals(key)){
      processTypedefs(map, key, value);
    }
    else{
      super.putWithSemantics(map, key, value);
    }
  }

  private void processTypedefs(JsonMap map, String key, Object value)
  {
    JsonParseError error = null;
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

    if(error == null){
      // Handle multiple typedefs@ in at the same level in a single document
      JsonMap previousTypedefs = (JsonMap) map.get(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY);
      if(previousTypedefs != null){
        // Merge the typedefs
        JsonMap newTypedefs = (JsonMap) value;
        Set<? extends String> keys = newTypedefs.keySet();
        for(String newTypeName : keys){
          if(previousTypedefs.containsKey(newTypeName) == true){
            // TODO: Centralize this error handling
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

            error = new JsonParseError("duplicate type " + newTypeName + " declared at line " + errorLine + " column " + errorCol);
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
}
