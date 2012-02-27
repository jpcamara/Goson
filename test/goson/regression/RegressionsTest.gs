package goson.regression

uses goson.test.GosonTest

class RegressionsTest extends GosonTest {

  function testCorysExample() {
    var x = new goson.examples.regressions.FromCory()
    assertNotNull(x)
  }

}