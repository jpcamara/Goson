package org.jschema.util;

import junit.framework.TestCase;
import org.jschema.model.JsonMap;
import org.jschema.parser.JSONParser;
import org.jschema.test.GosonSuite;
import org.jschema.test.GosonTest;
import org.junit.Ignore;

import java.io.*;

@Ignore
public class JSONToJSchemaTest extends GosonTest {
  private static final String JSON_FILE = "test/org/jschema/examples/json/GithubCreate.json";
  private static final String JSC_OUTPUT = "result.jsc";
  private static final PrintStream SYS_OUT = System.out;

  public void testValidation() throws IOException {
    try {
      JSONToJSchema.main(new String[] {});
    } catch (IllegalArgumentException e) {}

    try {
      JSONToJSchema.main(new String[] {"-blahhh", "somefile.json"});
    } catch (IllegalArgumentException e) {}

    try {
      JSONToJSchema.main(new String[] {"-json", JSON_FILE, "third without a fourth"});
    } catch (IllegalArgumentException e) {}

    try {
      JSONToJSchema.main(new String[] {"-json", JSON_FILE, "-brickhouse", JSC_OUTPUT});
    } catch (IllegalArgumentException e) {}

    JSONToJSchema.main(new String[] {"-json", JSON_FILE, "-jschema", JSC_OUTPUT});
  }

  public void testCreateStdoutOutput() throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream outRedirect = new PrintStream(output);
    System.setOut(outRedirect);
    JSONToJSchema.main(new String[] {"-json", JSON_FILE});
    System.setOut(SYS_OUT);
    System.out.println(output.toString());
//    JsonMap schema = (JsonMap)JSONParser.parseJSON(output.toString());
    assertTrue(!output.toString().isEmpty());
  }

  public void testCreateOutputJschemaFile() throws Exception {
    JSONToJSchema.main(new String[] {"-json", JSON_FILE, "-jschema", JSC_OUTPUT});
    assertTrue(new File(JSC_OUTPUT).exists());
  }

  @Override
  public void tearDown() throws Exception {
    new File(JSC_OUTPUT).delete();
    System.setOut(SYS_OUT);
  }

}
