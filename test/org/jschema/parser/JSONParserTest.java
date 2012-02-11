package org.jschema.parser;

import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import org.jschema.test.GosonTest;
import org.jschema.util.JSchemaUtils;
import org.junit.Ignore;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Ignore
public class JSONParserTest extends GosonTest {

  public void testBasics() {
    // null
    assertNull(JSchemaUtils.parseJson("null"));

    // strings
    assertEquals("", JSchemaUtils.parseJson("\"\""));
    assertEquals("hello", JSchemaUtils.parseJson("\"hello\""));
    assertEquals("hello world", JSchemaUtils.parseJson("\"hello world\""));

    // numbers
    assertEquals(1L, JSchemaUtils.parseJson("1"));
    assertEquals(bd("1.1"), JSchemaUtils.parseJson("1.1"));
    assertEquals(-1L, JSchemaUtils.parseJson("-1"));
    assertEquals(bd("-1.1"), JSchemaUtils.parseJson("-1.1"));
    assertEquals(bd("1e1"), JSchemaUtils.parseJson("1e1"));
    assertEquals(bd("1E1"), JSchemaUtils.parseJson("1E1"));
    assertEquals(bd("1e+1"), JSchemaUtils.parseJson("1e+1"));
    assertEquals(bd("1E+1"), JSchemaUtils.parseJson("1E+1"));
    assertEquals(bd("1e-1"), JSchemaUtils.parseJson("1e-1"));
    assertEquals(bd("1E-1"), JSchemaUtils.parseJson("1E-1"));

    // booleans
    assertEquals(Boolean.TRUE, JSchemaUtils.parseJson("true"));
    assertEquals(Boolean.FALSE, JSchemaUtils.parseJson("false"));

    // lists
    assertEquals(Collections.EMPTY_LIST, JSchemaUtils.parseJson("[]"));
    assertEquals(Arrays.asList("asdf", 1L, true), JSchemaUtils.parseJson("[\"asdf\", 1, true]"));

    // maps
    assertEquals(Collections.EMPTY_MAP, JSchemaUtils.parseJson("{}"));
    // A map literal! A map literal!  My kingdom for a map literal!
    HashMap map = new HashMap();
    map.put("foo", 10L);
    map.put("bar", false);
    assertEquals(map, JSchemaUtils.parseJson("{\"foo\" : 10, \"bar\" : false}"));
  }

  private Object bd(String s) {
    return new BigDecimal(s);
  }

  public void testLongBigDecimal() {
    BigDecimal bigDecimal = new BigDecimal(Double.MAX_VALUE).add(new BigDecimal(".1"));
    assertEquals(bigDecimal, JSchemaUtils.parseJson(bigDecimal.toString()));

    BigDecimal negativeBigDecimal = new BigDecimal(Double.MIN_VALUE).add(new BigDecimal("-.1"));
    assertEquals(negativeBigDecimal, JSchemaUtils.parseJson(negativeBigDecimal.toString()));
  }

  public void testComments() {
    assertNull(JSchemaUtils.parseJson("null // test comment"));
    assertNull(JSchemaUtils.parseJson("\nnull \n// test comment\n"));
    assertNull(JSchemaUtils.parseJson("\nnull \n/* test \ncomment */\n"));
    assertNull(JSchemaUtils.parseJson("/* test comment */ null "));
    assertNull(JSchemaUtils.parseJson("\n/* \ntest comment */\n null \n"));
  }

  public void testBasicNestedDataStructures() {
    Map obj = (Map) JSchemaUtils.parseJson("{" +
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
    assertEquals("blah\"blah", JSchemaUtils.parseJson("\"blah\\\"blah\""));
    assertEquals("blah\\blah", JSchemaUtils.parseJson("\"blah\\\\blah\""));
    assertEquals("blah/blah", JSchemaUtils.parseJson("\"blah\\/blah\""));
    assertEquals("blah\bblah", JSchemaUtils.parseJson("\"blah\\bblah\""));
    assertEquals("blah\fblah", JSchemaUtils.parseJson("\"blah\\fblah\""));
    assertEquals("blah\nblah", JSchemaUtils.parseJson("\"blah\\nblah\""));
    assertEquals("blah\rblah", JSchemaUtils.parseJson("\"blah\\rblah\""));
    assertEquals("blah\tblah", JSchemaUtils.parseJson("\"blah\\tblah\""));
    assertEquals("blah\u1234blah", JSchemaUtils.parseJson("\"blah\\u1234blah\""));
  }

  public void testURIsParseCorrectly() throws URISyntaxException {
    URI uri = new URI("http://example.com");
    assertEquals(uri, JSchemaUtils.parseJson(JSchemaUtils.serializeJson(uri), TypeSystem.get(URI.class)));

    URI email = new URI("mailto:test@test.com");
    assertEquals(email, JSchemaUtils.parseJson(JSchemaUtils.serializeJson(email), TypeSystem.get(URI.class)));

    Object val = JSchemaUtils.parseJson("[\"http://example.com\"]", JavaTypes.LIST().getParameterizedType(TypeSystem.get(URI.class)));
    assertEquals(Arrays.asList(new URI("http://example.com")), val);

    Object val2 = JSchemaUtils.parseJson("{\"foo\" : \"http://example.com\"}", JavaTypes.MAP().getParameterizedType(JavaTypes.STRING(), TypeSystem.get(URI.class)));
    Map m = new HashMap();
    m.put("foo", new URI("http://example.com"));
    assertEquals(m, val2);

    assertEquals(uri, JSchemaUtils.parseJson(JSchemaUtils.serializeJson(uri), TypeSystem.get(URI.class)));

    URI email2 = new URI("mailto:test@test.com");
    assertEquals(email2, JSchemaUtils.parseJson(JSchemaUtils.serializeJson(email2), TypeSystem.get(URI.class)));
  }

  public void testDateParsing() {
    assertEquals(makeDate(1999, 1, 1, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 1, 1, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-01\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 1, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 1, 2, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-01-02\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T00:00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T00:00:00.00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T00:00:00.00Z\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T00:00:00.00+00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 0, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T00:00:00.00-00:00\"", JavaTypes.DATE()));

    assertEquals(makeDate(1999, 12, 31, 1, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:00:00.00-00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:00:00.00-00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:00:00.00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:00:00.00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 0, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:00\"", JavaTypes.DATE()));

    assertEquals(makeDate(1999, 12, 31, 1, 1, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01:00.00-00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59:00.00-00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01:00.00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59:00.00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 0, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59\"", JavaTypes.DATE()));

    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01:01.00-00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59:59.00-00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01:01.00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59:59.00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01:01\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 0, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59:59\"", JavaTypes.DATE()));

    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 10, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01:01.01-00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 999, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59:59.999-00:00\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 10, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T01:01:01.01\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 999, 0, 0), JSchemaUtils.parseJson("\"1999-12-31T23:59:59.999\"", JavaTypes.DATE()));

    assertEquals(makeDate(1999, 12, 31, 1, 1, 1, 10, -10, 30), JSchemaUtils.parseJson("\"1999-12-31T01:01:01.01-10:30\"", JavaTypes.DATE()));
    assertEquals(makeDate(1999, 12, 31, 23, 59, 59, 999, 10, 30), JSchemaUtils.parseJson("\"1999-12-31T23:59:59.999+10:30\"", JavaTypes.DATE()));

    try{
      JSchemaUtils.parseJson("\"1999-12-31T23:59.999+10:30\"", JavaTypes.DATE());
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      // gulp
    }
  }

  public void testParseDocumentThrowsIfNotMapOrList()
  {
    try{
      JSchemaUtils.parseJsonDocument("\"hello world\"");
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      // Gulp
    }
    // And these should not throw.
    JSchemaUtils.parseJsonDocument("{ \"a\" : \"some value\" }");
    JSchemaUtils.parseJsonDocument("[ \"a\"]");
  }

  public void testParserGathersError()
  {
    try{
      JSchemaUtils.parseJson("\"1999-12-31T23:59.999+10:30\"", JavaTypes.DATE());
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
      JSchemaUtils.parseJsonDocument(badJson);
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      // gulp
    }

    badJson = "[]{";
    try{
      JSchemaUtils.parseJsonDocument(badJson);
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
