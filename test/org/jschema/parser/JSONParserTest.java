package org.jschema.parser;

import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import org.jschema.test.GosonTest;
import org.jschema.util.JSchemaUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class JSONParserTest extends GosonTest {

  public void testBasics() {
    // null
    assertNull(JSONParser.parseJSONValue("null"));

    // strings
    assertEquals("", JSONParser.parseJSONValue("\"\""));
    assertEquals("hello", JSONParser.parseJSONValue("\"hello\""));
    assertEquals("hello world", JSONParser.parseJSONValue("\"hello world\""));

    // numbers
    assertEquals(1L, JSONParser.parseJSONValue("1"));
    assertEquals(bd("1.1"), JSONParser.parseJSONValue("1.1"));
    assertEquals(-1L, JSONParser.parseJSONValue("-1"));
    assertEquals(bd("-1.1"), JSONParser.parseJSONValue("-1.1"));
    assertEquals(bd("1e1"), JSONParser.parseJSONValue("1e1"));
    assertEquals(bd("1E1"), JSONParser.parseJSONValue("1E1"));
    assertEquals(bd("1e+1"), JSONParser.parseJSONValue("1e+1"));
    assertEquals(bd("1E+1"), JSONParser.parseJSONValue("1E+1"));
    assertEquals(bd("1e-1"), JSONParser.parseJSONValue("1e-1"));
    assertEquals(bd("1E-1"), JSONParser.parseJSONValue("1E-1"));

    // booleans
    assertEquals(Boolean.TRUE, JSONParser.parseJSONValue("true"));
    assertEquals(Boolean.FALSE, JSONParser.parseJSONValue("false"));

    // lists
    assertEquals(Collections.EMPTY_LIST, JSONParser.parseJSONValue("[]"));
    assertEquals(Arrays.asList("asdf", 1L, true), JSONParser.parseJSONValue("[\"asdf\", 1, true]"));

    // maps
    assertEquals(Collections.EMPTY_MAP, JSONParser.parseJSONValue("{}"));
    // A map literal! A map literal!  My kingdom for a map literal!
    HashMap map = new HashMap();
    map.put("foo", 10L);
    map.put("bar", false);
    assertEquals(map, JSONParser.parseJSONValue("{\"foo\" : 10, \"bar\" : false}"));
  }

  private Object bd(String s) {
    return new BigDecimal(s);
  }

  public void testLongBigDecimal() {
    BigDecimal bigDecimal = new BigDecimal(Double.MAX_VALUE).add(new BigDecimal(".1"));
    assertEquals(bigDecimal, JSONParser.parseJSONValue(bigDecimal.toString()));

    BigDecimal negativeBigDecimal = new BigDecimal(Double.MIN_VALUE).add(new BigDecimal("-.1"));
    assertEquals(negativeBigDecimal, JSONParser.parseJSONValue(negativeBigDecimal.toString()));
  }

  public void testComments() {
    assertNull(JSONParser.parseJSONValue("null // test comment"));
    assertNull(JSONParser.parseJSONValue("\nnull \n// test comment\n"));
    assertNull(JSONParser.parseJSONValue("\nnull \n/* test \ncomment */\n"));
    assertNull(JSONParser.parseJSONValue("/* test comment */ null "));
    assertNull(JSONParser.parseJSONValue("\n/* \ntest comment */\n null \n"));
  }

  public void testBasicNestedDataStructures() {
    Map obj = (Map) JSONParser.parseJSONValue(
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
    assertEquals("blah\"blah", JSONParser.parseJSONValue("\"blah\\\"blah\""));
    assertEquals("blah\\blah", JSONParser.parseJSONValue("\"blah\\\\blah\""));
    assertEquals("blah/blah", JSONParser.parseJSONValue("\"blah\\/blah\""));
    assertEquals("blah\bblah", JSONParser.parseJSONValue("\"blah\\bblah\""));
    assertEquals("blah\fblah", JSONParser.parseJSONValue("\"blah\\fblah\""));
    assertEquals("blah\nblah", JSONParser.parseJSONValue("\"blah\\nblah\""));
    assertEquals("blah\rblah", JSONParser.parseJSONValue("\"blah\\rblah\""));
    assertEquals("blah\tblah", JSONParser.parseJSONValue("\"blah\\tblah\""));
    assertEquals("blah\u1234blah", JSONParser.parseJSONValue("\"blah\\u1234blah\""));
  }

  public void testURIsParseCorrectly() throws URISyntaxException {
    URI uri = new URI("http://example.com");
    assertEquals(uri, JSONParser.parseJSONValue(JSchemaUtils.serializeJson(uri), TypeSystem.get(URI.class)));

    URI email = new URI("mailto:test@test.com");
    assertEquals(email, JSONParser.parseJSONValue(JSchemaUtils.serializeJson(email), TypeSystem.get(URI.class)));

    Object val = JSONParser.parseJSONValue("[\"http://example.com\"]", IJavaType.LIST.getParameterizedType(TypeSystem.get(URI.class)));
    assertEquals(Arrays.asList(new URI("http://example.com")), val);

    Object val2 = JSONParser.parseJSONValue("{\"foo\" : \"http://example.com\"}", IJavaType.MAP.getParameterizedType(IJavaType.STRING, TypeSystem.get(URI.class)));
    Map m = new HashMap();
    m.put("foo", new URI("http://example.com"));
    assertEquals(m, val2);

    assertEquals(uri, JSONParser.parseJSONValue(JSchemaUtils.serializeJson(uri), TypeSystem.get(URI.class)));

    URI email2 = new URI("mailto:test@test.com");
    assertEquals(email2, JSONParser.parseJSONValue(JSchemaUtils.serializeJson(email2), TypeSystem.get(URI.class)));
  }

  public void testDateParsing() {
    assertEquals(makeDate(1999, 1, 1, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 1, 1, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-01\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 1, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 1, 2, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-01-02\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T00:00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T00:00:00.00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T00:00:00.00Z\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T00:00:00.00+00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T00:00:00.00-00:00\"", IJavaType.DATE));

    assertEquals(makeDate(1999, 12, 31, 1, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:00:00.00-00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:00:00.00-00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:00:00.00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:00:00.00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 0, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:00\"", IJavaType.DATE));

    assertEquals(makeDate(1999, 12, 31, 1, 1, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01:00.00-00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59:00.00-00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01:00.00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59:00.00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 0, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59\"", IJavaType.DATE));

    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01:01.00-00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59:59.00-00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01:01.00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59:59.00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01:01\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 0, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59:59\"", IJavaType.DATE));

    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 10, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01:01.01-00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 999, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59:59.999-00:00\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 10, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T01:01:01.01\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 999, 0, 0), JSONParser.parseJSONValue("\"1999-12-31T23:59:59.999\"", IJavaType.DATE));

    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 10, -10, 30), JSONParser.parseJSONValue("\"1999-12-31T01:01:01.01-10:30\"", IJavaType.DATE));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 999, 10, 30), JSONParser.parseJSONValue("\"1999-12-31T23:59:59.999+10:30\"", IJavaType.DATE));

    try{
      JSONParser.parseJSONValue("\"1999-12-31T23:59.999+10:30\"", IJavaType.DATE);
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      // gulp
    }
  }

  public void testParseDocumentThrowsIfNotMapOrList()
  {
    try{
      JSONParser.parseJSON("\"hello world\"");
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      // Gulp
    }
    // And these should not throw.
    JSONParser.parseJSON("{ \"a\" : \"some value\" }");
    JSONParser.parseJSON("[ \"a\"]");
  }

  public void testParserGathersError()
  {
    try{
      JSONParser.parseJSONValue("\"1999-12-31T23:59.999+10:30\"", IJavaType.DATE);
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      assertEquals(1, jpe.getErrorList().size());
    }
  }

  public void testParserDetectsExtraneousCharactersAtEndOfDocument()
  {
    String badJson = "{}{}";
    try{
      JSONParser.parseJSON(badJson);
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      // gulp
    }

    badJson = "[]{";
    try{
      JSONParser.parseJSON(badJson);
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      // gulp
    }

  }


  private Object makeDate(int year, int month, int day, int hour, int minute, int second, int milli, int offsetHours, int offsetMinutes) {
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.set(Calendar.YEAR, year);
    gregorianCalendar.set(Calendar.MONTH, month - 1);
    gregorianCalendar.set(Calendar.DAY_OF_MONTH, day);
    gregorianCalendar.set(Calendar.HOUR_OF_DAY, hour);
    gregorianCalendar.set(Calendar.MINUTE, minute);
    gregorianCalendar.set(Calendar.SECOND, second);
    gregorianCalendar.set(Calendar.MILLISECOND, milli);
    int millis = ((offsetHours * 60) + ((offsetHours < 0 ? - 1 : 1 ) * offsetMinutes)) * 60 * 1000;
    gregorianCalendar.setTimeZone(new SimpleTimeZone(millis, "Custom"));
    Date time = gregorianCalendar.getTime();
    System.out.println(time.toGMTString());
    return time;
  }
  
}
