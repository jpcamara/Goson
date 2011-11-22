package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses java.math.*
uses org.jschema.test.GosonTest
uses org.jschema.rpc.*
uses org.jschema.examples.rpc.*

class AssignabilityTest extends GosonTest {

 function testWrapper() {
    var server = new RPCServer()

    server.addEndPoint( new RPCEndPoint( AssignabilityTest1, new Impl1(), "/assignability1" ) )

    server.addEndPoint( new RPCEndPoint( AssignabilityTest2, new Impl2(), "/assignability2" ) )

    using( server ) {
      var result = AssignabilityTest1.func1( new AssignabilityTest2.Type1(){ :Name = "Foo",
                                                                             :Integer = 42,
                                                                             :Enum = A } )
      print(typeof result)
      print(typeof result.Enum)
      print(statictypeof result.Enum)
      assertEquals( "Foo", result.Name )
      assertEquals( 42L, result.Integer )
      assertEquals( AssignabilityTest1.Type1.Enum.A, result.Enum )
    }
 }

 class Impl1 {
   function func1( t : AssignabilityTest1.Type1 ) : AssignabilityTest1.Type1 {
     return t
   }

   function func2( t : List<AssignabilityTest1.Type1> ) : List<AssignabilityTest1.Type1> {
     return t
   }
 }

 class Impl2 {
   function func1( t : AssignabilityTest2.Type1 ) : AssignabilityTest2.Type1 {
     return t
   }

   function func2( t : List<AssignabilityTest2.Type1> ) : List<AssignabilityTest2.Type1> {
     return t
   }
 }

}