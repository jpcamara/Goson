package org.jschema.parser;

import org.jschema.typeloader.JsonName;
import junit.framework.TestCase;

import java.util.Arrays;

public class JsonNameTest extends TestCase {
  public void testJsonNames() throws Exception {
    assertEquals(new String[] {"nice", "name"}, new JsonName("nice", "name").getOriginalNames());
  }
}
