package org.jschema.parser;

public class JSONParseError {
  private String _message;

  public JSONParseError(String message)
  {
    _message = message;
    return;
  }

  public String getMessage()
  {
    return(_message);
  }
}
