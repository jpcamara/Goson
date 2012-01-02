package org.jschema.parser;

public class JsonParseError {
  private String _message;
  private int _start;
  private int _end;

  public JsonParseError(String message, int start, int end)
  {
    _message = message;
    _start = start;
    _end = end;
  }

  public String getMessage()
  {
    return(_message);
  }

  public int getStart() {
    return _start;
  }

  public int getEnd() {
    return _end;
  }
}
