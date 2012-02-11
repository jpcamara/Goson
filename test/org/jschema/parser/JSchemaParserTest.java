package org.jschema.parser;

import org.jschema.model.JsonMap;
import org.jschema.test.GosonTest;
import org.jschema.util.JSchemaUtils;
import org.junit.Ignore;

import java.util.Map;

@Ignore
public class JSchemaParserTest extends GosonTest {

  public void testTypedefsMustBeFollowedByObject()
  {
    String badJSchema = "{ \"typedefs@\" : \"foo\" }";
    JSchemaParser parser = new JSchemaParser(badJSchema);
    try{
      parser.parseJSchema();
      fail("Execption not thrown");
    }
    catch(JsonParseException jpe){
      // gulp
    }

    String goodJSchema = "{ \"typedefs@\" : {\n" +
            "    \"MyType\" : {\n" +
            "       \"line1\" : \"string\"\n" +
            "     }\n" +
            "   }\n" +
            "}";
    parser = new JSchemaParser(goodJSchema);
    parser.parseJSchema();
  }

  public void testMutltipleTypedefsAreMerged()
  {
    String schema = "{ \n" +
            "\n" +
            "\"typedefs@\" : {\n" +
            "    \"MyNewType\" : {\n" +
            "       \"line1\" : \"string\"\n" +
            "    }\n" +
            " },   \n" +
            " \"typedefs@\" : {\n" +
            "    \"MyOtherType\" : {\n" +
            "       \"line2\" : \"string\"\n" +
            "     }\n" +
            "   }   \n" +
            "}";
    JSchemaParser parser = new JSchemaParser(schema);
    JsonMap schemaMap = (JsonMap) parser.parseJSchema();
    JsonMap typedefsMap = (JsonMap) schemaMap.get(JSchemaUtils.JSCHEMA_TYPEDEFS_KEY);
    assertEquals(2, typedefsMap.size());
  }

  public void testDuplicateTypesdefsAreErrors()
  {
    String schema = "{ \n" +
            "\n" +
            "\"typedefs@\" : {\n" +
            "    \"MyNewType\" : {\n" +
            "       \"line1\" : \"string\"\n" +
            "    }\n" +
            " },   \n" +
            " \"typedefs@\" : {\n" +
            "    \"MyNewType\" : {\n" +
            "       \"line2\" : \"string\"\n" +
            "     }\n" +
            "   }   \n" +
            "}";
    JSchemaParser parser = new JSchemaParser(schema);
    try{
      parser.parseJSchema();
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      System.out.println(jpe.toString());
      // Gulp
    }
  }

  public void testDuplicateTypesdefsInSameBlockAreErrors()
  {
    String schema = "{ \n" +
            " \"typedefs@\" : {\n" +
            "    \"MyNewType\" : {\n" +
            "       \"line1\" : \"string\"\n" +
            "    },\n" +
            "    \"MyNewType\" : {\n" +
            "       \"line2\" : \"string\"\n" +
            "     }\n" +
            "    \n" +
            " }\n" +
            "}";
    JSchemaParser parser = new JSchemaParser(schema);
    try{
      parser.parseJSchema();
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      System.out.println(jpe.toString());
      // Gulp
    }
  }

  public void testDuplicateFieldDefsAreErrors()
  {

    String badSchema = "{\n" +
            "  \"sometype\" : {\n" +
            "    \"field1\" : \"int\",\n" +
            "    \"field1\" : \"int\"\n" +
            "  }\n" +
            "}";

    JSchemaParser parser = new JSchemaParser(badSchema);
    try{
      parser.parseJSchema();
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      System.out.println(jpe);
    }

    badSchema = "{\n" +
            "  \"map_of\" : {\n" +
            "    \"field2\" : \"int\",\n" +
            "    \"field2\" : \"int\"\n" +
            "  }\n" +
            "}";

    parser = new JSchemaParser(badSchema);
    try{
      parser.parseJSchema();
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      // gulp
    }
  }

  public void testFunctionsKeywordMustBeFollowedByAnArray()
  {
    String badSchema = "{\n" +
            "  \"url\" : \"http://localhost:12321/validation\",\n" +
            "\n" +
            "  \"functions\" : {\n" +
            "    \"name\" : \"intArgVoidReturn\",\n" +
            "      \"args\" : [ {\"arg1\" : \"int\"} ]\n" +
            "  }\n" +
            "}";

    JSchemaParser parser = new JSchemaParser(badSchema);
    try{
      parser.parseJSchema();
      fail("Exception not thrown");
    }
    catch(JsonParseException jpe){
      System.out.println(jpe);
    }
  }

}
