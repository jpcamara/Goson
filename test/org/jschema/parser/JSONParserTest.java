package org.jschema.parser;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class JSONParserTest extends TestCase {
  
  public void testBasics() {
    // null
    assertNull(JSONParser.parseJSON("null"));

    // strings
    assertEquals("", JSONParser.parseJSON("\"\""));
    assertEquals("hello", JSONParser.parseJSON("\"hello\""));
    assertEquals("hello world", JSONParser.parseJSON("\"hello world\""));

    // numbers
    assertEquals(1, JSONParser.parseJSON("1"));
    assertEquals(1.1, JSONParser.parseJSON("1.1"));

    // booleans
    assertEquals(Boolean.TRUE, JSONParser.parseJSON("true"));
    assertEquals(Boolean.FALSE, JSONParser.parseJSON("false"));

    // lists
    assertEquals(Collections.EMPTY_LIST, JSONParser.parseJSON("[]"));
    assertEquals(Arrays.asList("asdf", 1, true), JSONParser.parseJSON("[\"asdf\", 1, true]"));

    // maps
    assertEquals(Collections.EMPTY_MAP, JSONParser.parseJSON("{}"));
    // A map literal! A map literal!  My kingdom for a map literal!
    HashMap map = new HashMap();
    map.put("foo", 10);
    map.put("bar", false);
    assertEquals(map, JSONParser.parseJSON("{\"foo\" : 10, \"bar\" : false}"));
  }

}
