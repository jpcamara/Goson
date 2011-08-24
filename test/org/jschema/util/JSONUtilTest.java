package org.jschema.util;

import junit.framework.TestCase;

public class JSONUtilTest extends TestCase {

  public void testBasicNames() throws Exception {
    assertEquals("NiceName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name"));
    assertEquals("NiceNameSecondName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name_second_name"));
    assertEquals("niceName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name", false));
  }

}
