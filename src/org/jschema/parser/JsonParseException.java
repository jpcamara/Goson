package org.jschema.parser;


import java.util.List;

public class JsonParseException extends RuntimeException {

  private List<JSONParseError> _errorList;

  public JsonParseException(String msg, List<JSONParseError> errorList)
  {
    super(msg);
    _errorList = errorList;
  }

  public List<JSONParseError> getErrorList() {
    return _errorList;
  }
}
