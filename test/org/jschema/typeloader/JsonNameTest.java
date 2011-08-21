package org.jschema.typeloader;

import static org.junit.Assert.*;

import org.jschema.typeloader.JsonName;
import org.junit.Test;

import java.util.Arrays;

public class JsonNameTest {
  private JsonName name = new JsonName("nice_name", "second_name");

  @Test
  public void jsonNames() throws Exception {
    assertEquals(Arrays.asList("nice_name", "second_name"), name.getOriginalNames());
    assertEquals(Arrays.asList("NiceName", "SecondName"), name.getNames());
  }

  @Test
  public void pathName() throws Exception {
    assertEquals("NiceName.SecondName", name.toString());
  }

  @Test
  public void appendToJsonName() throws Exception {
    JsonName full = name.copyAndAppend("third_name").copyAndAppend("fourth_name");
    assertEquals("NiceName.SecondName.ThirdName.FourthName", full.toString());
  }
}
