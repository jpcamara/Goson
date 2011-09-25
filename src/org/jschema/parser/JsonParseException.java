package org.jschema.parser;


import java.util.List;

public class JsonParseException extends RuntimeException {

  private List<JsonParseError> _errorList;

  public JsonParseException(List<JsonParseError> errorList)
  {
    super(makeMessage(errorList));
    _errorList = errorList;
  }

  private static String makeMessage(List<JsonParseError> errorList) {
    String msg = Integer.toString(errorList.size()) + " errors detected:\n";
    for (JsonParseError error : errorList) {
      msg += "  " + error.getMessage();
    }
    return msg;
  }

  public List<JsonParseError> getErrorList() {
    return _errorList;
  }
}
