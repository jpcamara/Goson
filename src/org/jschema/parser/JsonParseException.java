package org.jschema.parser;


import java.util.List;

public class JsonParseException extends RuntimeException {

  private List<JSonParseError> _errorList;

  public JsonParseException(String msg, List<JSonParseError> errorList)
  {
    super(msg);
    _errorList = errorList;
  }

  public List<JSonParseError> getErrorList() {
    return _errorList;
  }
}
