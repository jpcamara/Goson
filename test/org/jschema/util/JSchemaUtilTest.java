package org.jschema.util;

import junit.framework.TestCase;
import org.jschema.parser.JSONParser;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class JSchemaUtilTest extends TestCase {

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

    Map json = (Map) JSONParser.parseJSON(content);
    json = (Map)JSchemaUtils.convertJsonToJSchema(json);
    Map someType = (Map)json.get("some_type");
    Map nested = (Map)someType.get("nested_type");
    Map nestedListEntry = (Map)((List)someType.get("type_in_array")).get(0);

    assertEquals("biginteger", someType.get("big_int_ex"));
    assertEquals("string", someType.get("string_ex"));
    assertEquals("boolean", someType.get("boolean_ex"));
    assertEquals("string", nestedListEntry.get("content"));
    assertEquals("string", nested.get("nested_string_ex"));
    assertEquals("string", ((Map)((List)nested.get("nested_type_in_array")).get(0)).get("value"));
    assertEquals("biginteger", ((List) nested.get("big_int_array_ex")).get(0));
    assertEquals("string", ((List)nested.get("string_array_ex")).get(0));
    assertEquals("biginteger", nested.get("nested_big_int_ex"));
    assertEquals("bigdecimal", nested.get("nested_big_decimal_ex"));
  }
}
