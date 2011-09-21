package org.jschema.parser;


import java.util.List;

public class JsonParseException extends RuntimeException {

  private List<JsonParseError> _errorList;

  public JsonParseException(String msg, List<JsonParseError> errorList)
  {
    super(msg);
    _errorList = errorList;
  }

  public List<JsonParseError> getErrorList() {
    return _errorList;
  }
}
