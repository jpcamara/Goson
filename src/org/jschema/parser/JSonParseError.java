package org.jschema.parser;

public class JSonParseError {
  private String _message;

  public JSonParseError(String message)
  {
    _message = message;
    return;
  }

  public String getMessage()
  {
    return(_message);
  }
}
