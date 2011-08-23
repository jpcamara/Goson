package org.jschema.test;

import junit.framework.TestCase;

public abstract class GosonTest extends TestCase {
  @Override
  protected void setUp() throws Exception {
    GosonSuite.maybeInitGosu();
  }

}
