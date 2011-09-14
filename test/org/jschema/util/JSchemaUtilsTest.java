package org.jschema.util;

import junit.framework.TestCase;
import org.jschema.model.JsonMap;
import org.jschema.parser.JSONParser;

import java.io.InputStreamReader;
import java.util.*;

public class JSchemaUtilsTest extends TestCase {

  public void testBasicNames() throws Exception {
    assertEquals("NiceName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name"));
    assertEquals("NiceNameSecondName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name_second_name"));
    assertEquals("niceName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name", false));
  }

  public void testConvertingJsonToJSchema() throws Exception {
    String content = null;
    Scanner scan = null;
    try {
      InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/org/jschema/examples/RegularJson.json"));
      scan = new Scanner(reader);
      scan.useDelimiter("\\Z");
      content = scan.next();
    } finally {
      if (scan != null) { scan.close(); }
    }

    Map json = (Map) JSONParser.parseJSONValue(content);
    json = (Map)JSchemaUtils.convertJSONToJSchema(json);
    Map someType = (Map)json.get("some_type");
    Map nested = (Map)someType.get("nested_type");
    Map nestedListEntry = (Map)((List)someType.get("type_in_array")).get(0);

    assertEquals("int", someType.get("int_ex"));
    assertEquals("string", someType.get("string_ex"));
    assertEquals("boolean", someType.get("boolean_ex"));
    assertEquals("string", nestedListEntry.get("content"));
    assertEquals("string", nested.get("nested_string_ex"));
    assertEquals("string", ((Map)((List)nested.get("nested_type_in_array")).get(0)).get("value"));
    assertEquals("int", ((List) nested.get("int_array_ex")).get(0));
    assertEquals("string", ((List)nested.get("string_array_ex")).get(0));
    assertEquals("int", nested.get("nested_int_ex"));
    assertEquals("number", nested.get("nested_number_ex"));
  }

  public void testSerializeStrings() {
    assertEquals("\"blah\\\"blah\"", JSchemaUtils.serializeJson("blah\"blah"));
    assertEquals("\"blah\\\\blah\"", JSchemaUtils.serializeJson("blah\\blah"));
    assertEquals("\"blah\\bblah\"", JSchemaUtils.serializeJson("blah\bblah"));
    assertEquals("\"blah\\fblah\"", JSchemaUtils.serializeJson("blah\fblah"));
    assertEquals("\"blah\\nblah\"", JSchemaUtils.serializeJson("blah\nblah"));
    assertEquals("\"blah\\rblah\"", JSchemaUtils.serializeJson("blah\rblah"));
    assertEquals("\"blah\\tblah\"", JSchemaUtils.serializeJson("blah\tblah"));
    assertEquals("\"blah\\u1234blah\"", JSchemaUtils.serializeJson("blah\u1234blah"));
  }

  public void testSerialize() {
    Map m = new JsonMap();
    m.put("foo", 10);
    m.put("bar", Arrays.asList(1, 2, 3));
    m.put("empty_map", new JsonMap());
    JsonMap subMap = new JsonMap();
    m.put("map", subMap);
    subMap.put("empty_map", new JsonMap());
    JsonMap subMapMap = new JsonMap();
    subMap.put("map", subMapMap);
    subMapMap.put("foo", "bar");

    assertEquals("{\"foo\" : 10, \"bar\" : [1, 2, 3], \"empty_map\" : {}, \"map\" : {\"empty_map\" : {}, \"map\" : {\"foo\" : \"bar\"}}}",
      JSchemaUtils.serializeJson(m));

    assertEquals("{\n" +
      "  \"foo\" : 10, \n" +
      "  \"bar\" : [1, 2, 3], \n" +
      "  \"empty_map\" : {}, \n" +
      "  \"map\" : {\n" +
      "    \"empty_map\" : {}, \n" +
      "    \"map\" : {\n" +
      "      \"foo\" : \"bar\"\n" +
      "    }\n" +
      "  }\n" +
      "}",
      JSchemaUtils.serializeJSON(m, 2));

    assertEquals("{\n" +
      "    \"foo\" : 10, \n" +
      "    \"bar\" : [1, 2, 3], \n" +
      "    \"empty_map\" : {}, \n" +
      "    \"map\" : {\n" +
      "        \"empty_map\" : {}, \n" +
      "        \"map\" : {\n" +
      "            \"foo\" : \"bar\"\n" +
      "        }\n" +
      "    }\n" +
      "}",
      JSchemaUtils.serializeJSON(m, 4));


    HashMap map2 = new HashMap();
    map2.put("foo", "bar");
    List lst = Arrays.asList(Collections.EMPTY_MAP, Collections.EMPTY_MAP, map2);
    assertEquals("[{}, {}, {\"foo\" : \"bar\"}]", JSchemaUtils.serializeJson(lst));

    assertEquals("[{}, {}, \n" +
                 "  {\n" +
                 "    \"foo\" : \"bar\"\n" +
                 "  }\n" +
                 "]", JSchemaUtils.serializeJSON(lst, 2));

    assertEquals("[[{}, {}, \n" +
                 "    {\n" +
                 "      \"foo\" : \"bar\"\n" +
                 "    }\n" +
                 "  ]]", JSchemaUtils.serializeJSON(Arrays.asList(lst), 2));

    HashMap map3 = new HashMap();
    map3.put("foo", lst);

    assertEquals("{\n" +
                 "  \"foo\" : [{}, {}, \n" +
                 "    {\n" +
                 "      \"foo\" : \"bar\"\n" +
                 "    }\n" +
                 "  ]\n" +
                 "}", JSchemaUtils.serializeJSON(map3, 2));
  }

  public void testLongSerialize() {
    Map map = new HashMap();
    map.put("int_key", 123123123123l);
    assertEquals("{\"int_key\" : 123123123123}", JSchemaUtils.serializeJson(map));
  }


  public void testDateSerializationWorks() {
    Calendar cal = new GregorianCalendar(1999, 12, 30, 0, 0, 0);
    cal.setTimeZone(TimeZone.getTimeZone("Z"));
    while (cal.before(new GregorianCalendar(2001, 1, 2))) {
      Date time = cal.getTime();
      Date roundTrip = JSchemaUtils.parseDate(JSchemaUtils.serializeDate(time));
      if (!time.equals(roundTrip)) {
        String str = JSchemaUtils.serializeDate(time);
        Date reparsedDate = JSchemaUtils.parseDate(str);
        fail("Found unequal dates!");
      }
      cal.add(Calendar.MINUTE, 1);
      cal.add(Calendar.SECOND, 1);
      cal.add(Calendar.MILLISECOND, 1);
    }
  }


}
