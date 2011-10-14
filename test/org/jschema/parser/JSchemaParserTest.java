package org.jschema.parser;

import org.jschema.test.GosonTest;

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

    String goodJSchema = "{ \"typedefs@\" : { \"name\" : \"string\"}}";
    parser = new JSchemaParser(goodJSchema);
    parser.parseJSchema();
  }
}
