package goson.parser;

import junit.framework.TestCase;

public class JSONTokenizerTest extends TestCase {

  public void testTokenizerLineNumberIsCorrect() {
    JSONTokenizer tk = new JSONTokenizer("1\n 2\n  3");
    int i = 1;
    while (tk.hasMoreTokens()) {
      JSONToken jsonToken = tk.nextToken();
      assertEquals(i, jsonToken.getLine());
      assertEquals(i, jsonToken.getColumn());
      i++;
    }
  }
}
