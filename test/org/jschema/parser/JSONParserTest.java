package org.jschema.parser;

import gw.lang.reflect.java.IJavaType;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONParserTest extends TestCase {
  
  public void testBasics() {
    // null
    assertNull(JSONParser.parseJSON("null"));

    // strings
    assertEquals("", JSONParser.parseJSON("\"\""));
    assertEquals("hello", JSONParser.parseJSON("\"hello\""));
    assertEquals("hello world", JSONParser.parseJSON("\"hello world\""));

    // numbers
    assertEquals(1L, JSONParser.parseJSON("1"));
    assertEquals(bd("1.1"), JSONParser.parseJSON("1.1"));
    assertEquals(-1L, JSONParser.parseJSON("-1"));
    assertEquals(bd("-1.1"), JSONParser.parseJSON("-1.1"));
    assertEquals(bd("1e1"), JSONParser.parseJSON("1e1"));
    assertEquals(bd("1E1"), JSONParser.parseJSON("1E1"));
    assertEquals(bd("1e+1"), JSONParser.parseJSON("1e+1"));
    assertEquals(bd("1E+1"), JSONParser.parseJSON("1E+1"));
    assertEquals(bd("1e-1"), JSONParser.parseJSON("1e-1"));
    assertEquals(bd("1E-1"), JSONParser.parseJSON("1E-1"));

    // booleans
    assertEquals(Boolean.TRUE, JSONParser.parseJSON("true"));
    assertEquals(Boolean.FALSE, JSONParser.parseJSON("false"));

    // lists
    assertEquals(Collections.EMPTY_LIST, JSONParser.parseJSON("[]"));
    assertEquals(Arrays.asList("asdf", 1L, true), JSONParser.parseJSON("[\"asdf\", 1, true]"));

    // maps
    assertEquals(Collections.EMPTY_MAP, JSONParser.parseJSON("{}"));
    // A map literal! A map literal!  My kingdom for a map literal!
    HashMap map = new HashMap();
    map.put("foo", 10L);
    map.put("bar", false);
    assertEquals(map, JSONParser.parseJSON("{\"foo\" : 10, \"bar\" : false}"));
  }

  private Object bd(double v) {
    return new BigDecimal(v);
  }

  private Object bd(String s) {
    return new BigDecimal(s);
  }

  public void testLongBigDecimal() {
    BigDecimal bigDecimal = new BigDecimal(Double.MAX_VALUE).add(new BigDecimal(".1"));
    assertEquals(bigDecimal, JSONParser.parseJSON(bigDecimal.toString(), IJavaType.BIGDECIMAL));

    BigDecimal negativeBigDecimal = new BigDecimal(Double.MIN_VALUE).add(new BigDecimal("-.1"));
    assertEquals(negativeBigDecimal, JSONParser.parseJSON(negativeBigDecimal.toString(), IJavaType.BIGDECIMAL));
  }

  public void testComments() {
    assertNull(JSONParser.parseJSON("null // test comment"));
    assertNull(JSONParser.parseJSON("\nnull \n// test comment\n"));
    assertNull(JSONParser.parseJSON("\nnull \n/* test \ncomment */\n"));
    assertNull(JSONParser.parseJSON("/* test comment */ null "));
    assertNull(JSONParser.parseJSON("\n/* \ntest comment */\n null \n"));
  }

  public void testBasicNestedDataStructures() {
    Map obj = (Map) JSONParser.parseJSON(
            "{" +
                    "\"null\" : null, " +
                    " \"number1\" : 1, " +
                    " \"number2\" : 1.1, " +
                    " \"boolean\" : true, " +
                    " \"list1\" : [ 1, 2, 3 ], " +
                    " \"list2\" : [ { \"str\" : \"string\" } ]," +
                    " \"map\" : { " +
                    "    \"map_boolean\" : true," +
                    "    \"map_string\" : \"string\"" +
                    "  } " +
                    "}");
    assertEquals(null, obj.get("null"));
    assertEquals(1L, obj.get("number1"));
    assertEquals(bd("1.1"), obj.get("number2"));
    assertEquals(true, obj.get("boolean"));
    assertEquals(Arrays.asList(1L, 2L, 3L), obj.get("list1"));

    List list2 = (List) obj.get("list2");
    assertEquals(1, list2.size());

    Map o = (Map) list2.get(0);
    assertEquals("string", o.get("str"));

    Map map2 = (Map) obj.get("map");
    assertEquals(2, map2.size());
    assertEquals(true, map2.get("map_boolean"));
    assertEquals("string", map2.get("map_string"));
  }

  public void testStrings() {
    assertEquals("blah\"blah", JSONParser.parseJSON("\"blah\\\"blah\""));
    assertEquals("blah\\blah", JSONParser.parseJSON("\"blah\\\\blah\""));
    assertEquals("blah/blah", JSONParser.parseJSON("\"blah\\/blah\""));
    assertEquals("blah\bblah", JSONParser.parseJSON("\"blah\\bblah\""));
    assertEquals("blah\fblah", JSONParser.parseJSON("\"blah\\fblah\""));
    assertEquals("blah\nblah", JSONParser.parseJSON("\"blah\\nblah\""));
    assertEquals("blah\rblah", JSONParser.parseJSON("\"blah\\rblah\""));
    assertEquals("blah\tblah", JSONParser.parseJSON("\"blah\\tblah\""));
    assertEquals("blah\u1234blah", JSONParser.parseJSON("\"blah\\u1234blah\""));
  }

}
