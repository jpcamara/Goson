package org.jschema.parser;

import com.sun.xml.internal.bind.v2.model.core.TypeInfo;
import org.eclipse.jetty.util.ajax.JSON;
import org.jschema.model.JsonMap;
import org.jschema.test.GosonTest;
import org.jschema.util.JSchemaUtils;

import java.util.Map;

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

}
