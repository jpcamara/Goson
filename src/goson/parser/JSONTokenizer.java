package org.jschema.parser;

import java.util.Arrays;
import java.util.List;

public class JSONTokenizer {

  private String _currentStringValue;
  private boolean _unescape;
  private String _contents;
  private int _line;
  private int _col;
  private int _offset;
  private int _currentCol;
  private int _currentStartOffset;
  private int _currentEndOffset;
  private JSONTokenType _type;
  private List<String> JSON_OPERATORS = Arrays.asList("{", "}", ":", "[", "]");

  public JSONTokenizer(String contents) {
    _contents = contents;
    _line = 1;
    _col = 1;
    _offset = 0;
    _currentStartOffset = 0;
    _currentEndOffset = 0;
    _currentStringValue = null;
  }

  public boolean hasMoreTokens() {
    return moveToNextToken();
  }

  private boolean moveToNextToken() {
    _unescape = false;

    eatWhitespace();

    if (atEndOfInput()) {
      return false;
    }

    _currentStartOffset = _offset;
    _currentCol = _col;

    if (consumeOperator()) {
      _type = JSONTokenType.OPERATOR;
    } else if (consumeSymbol()) {
      _type = JSONTokenType.SYMBOL;
    } else if (consumeString()) {
      _type = JSONTokenType.STRING;
    } else if (consumeNumber()) {
      _type = JSONTokenType.NUMBER;
    } else if (consumeComment()) {
      _type = JSONTokenType.COMMENT;
    } else {
      _type = JSONTokenType.UNKNOWN;
      consumeChar();
    }
    _currentEndOffset = _offset;
    _currentStringValue = _contents.substring(_currentStartOffset, _currentEndOffset);

    return true;
  }

  private boolean consumeComment()
  {
    if (!atEndOfInput() &&
        currentChar() == '/' && canPeek( 1 ) && peek() == '/') {
      consumeLineComment();
      return true;
    } else if (!atEndOfInput() && currentChar() == '/' && canPeek( 1 ) && peek() == '*') {
      incrementOffset();
      incrementOffset();
      consumeMultiLineComment();
      return true;
    }
    return false;
  }

  private void consumeLineComment()
  {
    while( !atEndOfInput() && currentChar() != '\n' )
    {
      incrementOffset();
    }
  }

  private void consumeMultiLineComment()
  {
    while( !atEndOfInput() && (lastChar() != '*' || currentChar() != '/') ) {
      if (Character.isWhitespace(currentChar())) {
        eatWhitespace();
      } else {
        incrementOffset();
      }
    }
    if (currentChar() == '/') {
      incrementOffset();
    }
  }

  private void consumeChar() {
    incrementOffset();
  }

  private boolean consumeNumber() {
    if (Character.isDigit(currentChar())) {
      consumeDigit();
      if (!atEndOfInput()) {
        if (currentChar() == '.') {
          if (canPeek(1) && Character.isDigit(peek())) {
            incrementOffset();
            consumeDigit();
          }
        } else if (currentChar() == 'e' || currentChar() == 'E') {
          if (canPeek(1) && Character.isDigit(peek())) {
            incrementOffset();
            consumeDigit();
          } else if (canPeek(2) && (peek() == '-' || peek() == '+') && Character.isDigit(peek(2))) {
            incrementOffset();
            incrementOffset();
            consumeDigit();
          }
        }
      }
      return true;
    }
    return false;
  }

  private char peek() {
    return _contents.charAt(_offset+1);
  }

  private void consumeDigit() {
    while (!atEndOfInput() && Character.isDigit(currentChar())) {
      incrementOffset();
    }
  }

  private boolean consumeString() {
    if ('\'' == currentChar() || '"' == currentChar()) {
      char initial = currentChar();
      char previous = initial;
      incrementOffset();
      while (!atEndOfInput() && currentChar() != '\n') {
        char current = currentChar();
        if (current == '\\') {
          _unescape = true;
        }
        if (current == initial && previous != '\\') {
          incrementOffset();
          break;
        } else {
          previous = current;
          incrementOffset();
        }
      }
      return true;
    }
    return false;
  }

  private boolean consumeSymbol() {
    if (Character.isLetter(currentChar())) {
      incrementOffset();
      while (!atEndOfInput() && Character.isJavaIdentifierPart(currentChar())) {
        incrementOffset();
      }
      return true;
    }
    return false;
  }

  private boolean consumeOperator() {
    for (String operator : JSON_OPERATORS) {
      boolean matched = true;
      for (int i = 0; i < operator.length(); i++) {
        if (!canPeek(i) || peek(i) != operator.charAt(i)) {
          matched = false;
          break;
        }
      }
      if (matched) {
        // consume additional pylons (er, tokens)
        for (int i = 1 /* NOTE WE START AT 1! */; i < operator.length(); i++) {
          incrementOffset();
        }
      }
    }
    return false;
  }

  private char peek(int i) {
    return _contents.charAt(_offset + i);
  }

  private boolean canPeek(int count) {
    return _offset + count < _contents.length();
  }


  private boolean atEndOfInput() {
    return _contents == null || _offset >= _contents.length();
  }

  private void eatWhitespace() {
    while (!atEndOfInput() && Character.isWhitespace(currentChar())) {
      if ('\n' == currentChar()) {
        _line++;
        _col = 0;
      }
      incrementOffset();
    }
  }

  private void incrementOffset() {
    _offset++;
    _col++;
  }

  private char lastChar() {
    return _contents.charAt(_offset - 1);
  }

  private char currentChar() {
    return _contents.charAt(_offset);
  }

  public JSONToken nextToken() {
    return new JSONToken(_type, _currentStringValue, _line, _currentCol, _currentStartOffset, _currentEndOffset);
  }
}
