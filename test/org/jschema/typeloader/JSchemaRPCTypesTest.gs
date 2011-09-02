package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses java.math.*
uses org.jschema.test.*
uses org.jschema.rpc.*
uses org.jschema.examples.rpc.Defaults
uses org.jschema.examples.rpc.Sample1
uses org.jschema.examples.rpc.ThrowsExceptions
uses org.jschema.examples.rpc.Sample1.GetEmployee
uses org.jschema.examples.rpc.Sample1.UpdateEmployee.Employee
uses org.jschema.examples.rpc.Sample2
uses org.jschema.examples.rpc.ValidationBasis

class JSchemaRPCTypesTest extends GosonTest {

 function testBootstrapFile() {
   var emp = Sample1
                .with( :handler = \ method, url, args -> '{ "first_name" : "Joe", "last_name" : "Blow", "age" : 21, "id" : 42 }' )
                .getEmployee(22)

   assertEquals( "Joe", emp.FirstName )
   assertEquals( "Blow", emp.LastName )
   assertEquals( 21, emp.Age )
   assertEquals( 42, emp.Id )
 }

 function testBootstrapAdd() {
   var server = new RPCServer()
   server.addEndPoint( new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) )
   using( server ) {
     assertEquals( 2, Sample1.add(1, 1) )
     assertEquals( 0, Sample1.add(1, -1) )
     assertEquals( -1, Sample1.add(0, -1) )
   }
 }

 function testBootstrapFile2() {
   var emp = new Employee() {
                  :FirstName = "Joe",
                  :LastName = "Blow",
                  :Age = 21
                }
   var result = Sample1
                .with( :handler = \ method, url, args -> {
                  assertNotNull(args["employee"])
                  return 'true'
                 } )
                .updateEmployee(emp)

   assertTrue( result )
 }

 function testBootstrapFileRemotelyWithGet() {
   var server = new RPCServer()
   server.addEndPoint( new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) )
   using( server ) {
     var emp = Sample1
                  .with( :method = GET )
                  .getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21, emp.Age )
     assertEquals( 42, emp.Id )
   }
 }

 function testBootstrapFileRemotelyWithPost() {
   var server = new RPCServer()
   server.addEndPoint( new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) )
   using( server ) {
     var emp = Sample1.getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21, emp.Age )
     assertEquals( 42, emp.Id )
   }
 }


 function testBootstrapFileRemotelyWithGetUsingApacheHTTPClient() {
   var server = new RPCServer()
   server.addEndPoint( new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) )
   using( server ) {
     var emp = Sample1
                  .with( :handler = new ApacheHTTPClientCallHandler(), :method = GET )
                  .getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21, emp.Age )
     assertEquals( 42, emp.Id )
   }
 }

 function testBootstrapFileRemotelyWithPostUsingApacheHTTPClient() {
   var server = new RPCServer()
   server.addEndPoint( new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) )
   using( server ) {
     var emp = Sample1
                   .with( :handler = new ApacheHTTPClientCallHandler(),
                          :method = Post )
                   .getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21, emp.Age )
     assertEquals( 42, emp.Id )
   }
 }

 class Impl1 {
   function getEmployee( id : Integer ) : GetEmployee {
     return new GetEmployee() {
       :FirstName = "Joe",
       :LastName = "Blow",
       :Age = 21,
       :Id = 42
     }
   }

   function updateEmployee(emp : Employee) : boolean
   {
     return(true)
   }

   function add( i1 : Integer , i2 : Integer ) : Integer {
     return i1 + i2
   }
 }

 function testBootstrapWithTypeDef() {
 
   var emp = Sample2
                .with( :handler = \ method, url, args -> '{ "first_name" : "Joe", "last_name" : "Blow", "age" : 21, "id" : 42 }' )
                .getEmployee(22)

   assertEquals( "Joe", emp.FirstName )
   assertEquals( "Blow", emp.LastName )
   assertEquals( 21, emp.Age )

   var result = Sample2
                .with( :handler = \ method, url, args -> 'true' )
                .updateEmployee(emp)

   assertTrue( result )
 }

 function testException() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( ThrowsExceptions, new ThrowsImpl(), "/throws" ) )
    using( server ) {
      try {
        ThrowsExceptions.exception()
        fail("Should have thrown an exception")
      } catch( e ) {
        //pass
      }
    }
 }

 function testNestedException() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( ThrowsExceptions, new ThrowsImpl(), "/throws" ) )
    using( server ) {
      try {
        ThrowsExceptions.deepException(10)
        fail("Should have thrown an exception")
      } catch( e ) {
        //pass
      }
    }
 }

 function testNpeException() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( ThrowsExceptions, new ThrowsImpl(), "/throws" ) )
    using( server ) {
      try {
        ThrowsExceptions.npeException()
        fail("Should have thrown an NPE")
      } catch (npe : java.lang.NullPointerException) {
        var stackTrace = npe.StackTraceAsString
        assertTrue( stackTrace.contains("at org.jschema.typeloader.JSchemaRPCTypesTest$ThrowsImpl.npeException") )
        assertTrue( stackTrace.contains("at org.jschema.examples.rpc.ThrowsExceptions.npeException()") )
      }
    }
 }

 // RPCEndPoint.validate() tests.

 function testValidateThrowsForIncompleteImpl()
 {
    try{
        var endPoint = new RPCEndPoint(ValidationBasis, new IncompleteValidation(), "/validation")
        fail("Exception not thrown")
    }
    catch(iae : java.lang.IllegalArgumentException) {
        // Gulp
    }
 }

 function testValidateThrowsForIncorrectArgType()
 {
     try{
         var endPoint = new RPCEndPoint(ValidationBasis, new IncorrectArgTypeValidation(), "/validation")
         fail("Exception not thrown")
     }
     catch(iae : java.lang.IllegalArgumentException) {
         // Gulp
     }
 }

 function testValidateThrowsForIncorrectArgCount()
 {
     try{
         var endPoint = new RPCEndPoint(ValidationBasis, new IncorrectArgCountValidation(), "/validation")
         fail("Exception not thrown")
     }
     catch(iae : java.lang.IllegalArgumentException) {
         // Gulp
     }
 }

 function testValidateThrowsForIncorrectReturnType()
 {
    try{
        var endPoint = new RPCEndPoint(ValidationBasis, new IncorrectReturnValidation(), "/validation")
        fail("Exception not thrown")
    }
    catch(iae : java.lang.IllegalArgumentException) {
        // Gulp
    }
 }

 class IncompleteValidation {
    function intArgVoidReturn(arg1 : Integer)
    {
        return;
    }
    // Missing intArgBoolArgBooleanReturn
 }

 class IncorrectArgTypeValidation{
    function intArgVoidReturn(arg1 : Integer)
    {
        return;
    }

    function intArgBoolArgBooleanReturn(arg1 : String, arg2 : boolean) : boolean
    {
        return(true)
    }
 }

 class IncorrectArgCountValidation{
    function intArgVoidReturn(arg1 : Integer)
    {
        return;
    }

    function intArgBoolArgBooleanReturn(arg1 : Integer) : boolean
    {
        return(true)
    }
 }



  class IncorrectReturnValidation{
     function intArgVoidReturn(arg1 : Integer)
     {
         return;
     }

     function intArgBoolArgBooleanReturn(arg1 : Integer, arg2 : boolean) : Integer
     {
         return(new Integer(1))
     }
  }

 class ThrowsImpl {
   function exception() {
     throw "Good times, good times"
   }

   function deepException( depth : Integer ) {
     if(depth.intValue() <= 0) {
       throw "Good times, good times"
     } else {
       deepException( new Integer(depth.intValue() - 1) )
     }
   }

   function npeException() {
     var x : String = null
     print( x.length() )
   }
 }

  function testDefaultsImplementation() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( Defaults, new DefaultsImpl(), "/defaults" ) )
    using( server ) {
      assertEquals( "foo", Defaults.identityString( "foo" ) )
      assertEquals( "foo", Defaults.identityString( :s = "foo" ) )
      assertEquals( null, Defaults.identityString( null ) )
      assertEquals( null, Defaults.identityString( :s = null ) )

      assertEquals( "foo", Defaults.identityStringWithDefault( "foo" ) )
      assertEquals( "foo", Defaults.identityStringWithDefault( :s = "foo" ) )
      assertEquals( "this is the default", Defaults.identityStringWithDefault() )
      assertEquals( "this is the default", Defaults.identityStringWithDefault( null ) )
      assertEquals( "this is the default", Defaults.identityStringWithDefault( :s = null ) )
      assertEquals( null, Defaults.with( :includeNulls = true ).identityStringWithDefault() )
      assertEquals( null, Defaults.with( :includeNulls = true ).identityStringWithDefault( null ) )
      assertEquals( null, Defaults.with( :includeNulls = true ).identityStringWithDefault( :s = null ) )

      assertEquals( 123421, Defaults.identityInteger( 123421 ) )
      assertEquals( 123421, Defaults.identityInteger( :i = 123421 ) )
      assertEquals( null, Defaults.identityInteger( null ) )
      assertEquals( null, Defaults.identityInteger( :i = null ) )

      assertEquals( 123421, Defaults.identityIntegerWithDefault( 123421 ) )
      assertEquals( 123421, Defaults.identityIntegerWithDefault( :i = 123421 ) )
      assertEquals( 42, Defaults.identityIntegerWithDefault( null ) )
      assertEquals( 42, Defaults.identityIntegerWithDefault() )
      assertEquals( 42, Defaults.identityIntegerWithDefault( :i = null ) )
      assertEquals( null, Defaults.with( :includeNulls = true ).identityIntegerWithDefault() )
      assertEquals( null, Defaults.with( :includeNulls = true ).identityIntegerWithDefault( null ) )
      assertEquals( null, Defaults.with( :includeNulls = true ).identityIntegerWithDefault( :i = null ) )

      assertEquals( "foobar", Defaults.twoArgsWithDefaults( "foo", "bar" ) )
      assertEquals( "barblow", Defaults.twoArgsWithDefaults( :arg1 = "bar" ) )
      assertEquals( "joebar", Defaults.twoArgsWithDefaults( :arg2 = "bar" ) )
      assertEquals( "joeblow", Defaults.twoArgsWithDefaults() )
      assertEquals( "barnull", Defaults.with( :includeNulls = true ).twoArgsWithDefaults( :arg1 = "bar" ) )
      assertEquals( "nullbar", Defaults.with( :includeNulls = true ).twoArgsWithDefaults( :arg2 = "bar" ) )
      assertEquals( "nullnull", Defaults.with( :includeNulls = true ).twoArgsWithDefaults() )
    }
  }

  function testObjectTypeImplementation() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( Defaults, new DefaultsImpl(), "/defaults" ) )
    using( server ) {
      assertEquals( "foo", Defaults.identityObject( "foo" ) )
      assertEquals( "foo", Defaults.identityObject( :obj = "foo" ) )
      assertEquals( 1, Defaults.identityObject( 1 ) )
      assertEquals( 1, Defaults.identityObject( :obj = 1 ) )
      assertEquals( true, Defaults.identityObject( true ) )
      assertEquals( true, Defaults.identityObject( :obj = true ) )
      assertEquals( {1, 2, 3}, Defaults.identityObject( {1, 2, 3} ) )
      assertEquals( {1, 2, 3}, Defaults.identityObject( :obj = {1, 2, 3} ) )
      assertEquals( {"a" -> 1, "b" -> 2, "c" -> 3}, Defaults.identityObject( {"a" -> 1, "b" -> 2, "c" -> 3} ) )
      assertEquals( {"a" -> 1, "b" -> 2, "c" -> 3}, Defaults.identityObject( :obj = {"a" -> 1, "b" -> 2, "c" -> 3} ) )
      assertEquals( null, Defaults.identityObject( null ) )
      assertEquals( null, Defaults.identityObject( :obj = null ) )
    }
  }

  class DefaultsImpl {

    function identityString(s : String) : String {
      return s
    }

    function identityStringWithDefault(s : String) : String {
      return s
    }

    function identityInteger(i : Integer) : Integer {
      return i
    }

    function identityObject(o : Object) : Object {
      return o
    }

    function identityIntegerWithDefault(i : Integer) : Integer {
      return i
    }

    function twoArgsWithDefaults(a : String, b : String ) : String {
      return a + b
    }
  }

 function testSchemaPublishesCorrectly() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( ThrowsExceptions, new ThrowsImpl(), "/throws" ) )
    using( server ) {
      assertEquals(ThrowsExceptions.Schema, SimpleRPCCallHandler.doGet("http://localhost:12321/throws?JSchema-RPC"))
    }
 }

}