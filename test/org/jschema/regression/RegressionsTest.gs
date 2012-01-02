package org.jschema.regression

uses org.jschema.test.GosonTest

class RegressionsTest extends GosonTest {

  function testCorysExample() {
    var x = new org.jschema.examples.regressions.FromCory()
    assertNotNull(x)
  }

}