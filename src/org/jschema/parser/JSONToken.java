package org.jschema.parser;

public class JSONToken {

  private static final JSONToken EOF = new JSONToken(JSONTokenType.EOF, null, 0, 0, 0, 0);

  static {
    EOF._next = EOF;
  }

  private JSONTokenType _type;
  private String _value;
  private JSONToken _next;
  private JSONToken _previous;
  private int _line;
  private int _col;
  private int _start;
  private int _end;

  public JSONToken(JSONTokenType type, String value, int line, int col, int start, int end) {
    _type = type;
    _value = value;
    _line = line;
    _col = col;
    _start = start;
    _end = end;
  }

  public void setNext(JSONToken t) {
    _next = t;
    if (!isEOF()||!t.isEOF()) {
      t._previous = this;
    }
  }

  public String getValue() {
    return _value;
  }

  public boolean isEOF() {
    return this == EOF;
  }

  public JSONToken previousToken()
  {
    return(_previous);
  }

  public JSONToken nextToken() {
    return _next;
  }

  public boolean match(String value) {
    return _value != null && _value.equalsIgnoreCase(value);
  }

  public static JSONToken tokenize(String contents) {
    JSONToken first = null;
    JSONToken previous = null;
    JSONTokenizer tokenizer = new JSONTokenizer(contents);
    while (tokenizer.hasMoreTokens()) {
      JSONToken t = tokenizer.nextToken();
      if (previous != null) {
        previous.setNext(t);
      }
      if (first == null) {
        first = t;
      }
      previous = t;
    }
    if (previous != null) {
      previous.setNext(JSONToken.EOF);
    }
    if (first == null) {
      first = JSONToken.EOF;
    }
    return first;
  }

  public JSONToken removeTokens( JSONTokenType... typesToRemove )
  {
    JSONToken first = null;
    JSONToken previous = null;
    JSONToken current = this;
    while( current != EOF ) {
      if (!isMatch(current, typesToRemove)) {
        JSONToken copy = new JSONToken(current._type, current._value, current._line, current._col, current._start, current._end);
        if (current.nextToken() == EOF) {
          copy.setNext(EOF);
        }
        if (first == null) {
          first = copy;
        }
        if (previous != null) {
          previous.setNext(copy);
        }
        previous = copy;
      }
      current = current.nextToken();
    }
    return first == null ? EOF : first;
  }

  private boolean isMatch(JSONToken token, JSONTokenType[] typesToRemove) {
    for( JSONTokenType JSONTokenType : typesToRemove )
    {
      if( token._type == JSONTokenType )
      {
        return true;
      }
    }
    return false;
  }


  @Override
  public String toString() {
    return getValue();
  }

  public String toStringForDebug() {
    return first().toStringForDebug( this );
  }

  private JSONToken first() {
    if (_previous == null) {
      return this;
    } else {
      return _previous.first();
    }
  }

  private String toStringForDebug(JSONToken current) {
    if (isEOF()) {
      if (this == current) {
        return "[|EOF]";
      } else {
        return "|EOF";
      }
    } else {
      String str = getValue();
      if (this == current) {
        str = "[" + str + "]";
      }
      return str + " " + _next.toStringForDebug(current);
    }
  }

  public int getLine() {
    return _line;
  }

  public int getColumn() {
    return _col;
  }

  public int getStart() {
    return _start;
  }

  public int getEnd() {
    return _end;
  }

  public boolean isSymbol() {
    return _type == JSONTokenType.SYMBOL;
  }

  public boolean isNumber() {
    return _type == JSONTokenType.NUMBER;
  }

  public boolean isString() {
    return _type == JSONTokenType.STRING;
  }

  public boolean isComment()
  {
    return _type == JSONTokenType.COMMENT;
  }

  public boolean endOf(String... tokens) {
    JSONToken current = this;
    for (int i = tokens.length - 1; i >= 0; i--) {
      if (!current.match(tokens[i])) {
        return false;
      }
      current = current.previousToken();
    }
    return true;
  }
}
