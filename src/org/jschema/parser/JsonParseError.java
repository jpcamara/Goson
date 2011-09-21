package org.jschema.parser;

public class JsonParseError {
  private String _message;

  public JsonParseError(String message)
  {
    _message = message;
    return;
  }

  public String getMessage()
  {
    return(_message);
  }
}
