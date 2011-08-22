package org.jschema.util;

import junit.framework.TestCase;

public class JSONUtilTest extends TestCase {

  public void testBasicNames() throws Exception {
    assertEquals("NiceName", JSONUtils.convertJSONStringToGosuIdentifier("nice_name"));
    assertEquals("NiceNameSecondName", JSONUtils.convertJSONStringToGosuIdentifier("nice_name_second_name"));
    assertEquals("niceName", JSONUtils.convertJSONStringToGosuIdentifier("nice_name", false));
  }

}
