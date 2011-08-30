package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses org.jschema.test.*
uses org.jschema.rpc.*
uses org.jschema.examples.rpc.Sample1
uses org.jschema.examples.rpc.ThrowsExceptions
uses org.jschema.examples.rpc.Sample1.GetEmployee
uses org.jschema.examples.rpc.Sample1.UpdateEmployee.Employee
uses org.jschema.examples.rpc.Sample2

class JSchemaRPCTypesTest extends GosonTest {

 function testBootstrapFile() {
   var emp = Sample1
                .with( :handler = \ method, url, args -> '{ "first_name" : "Joe", "last_name" : "Blow", "age" : 21, "id" : 42 }' )
                .getEmployee(22)

   assertEquals( "Joe", emp.FirstName )
   assertEquals( "Blow", emp.LastName )
   assertEquals( 21, emp.Age )
   //TODO cgross - need to do context sensitive parsing and produce an actual BigInteger
//   assertEquals( 42, emp.Id )
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
     //TODO cgross - need to do context sensitive parsing and produce an actual BigInteger
  //   assertEquals( 42, emp.Id )
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
     //TODO cgross - need to do context sensitive parsing and produce an actual BigInteger
  //   assertEquals( 42, emp.Id )
   }
 }

 class Impl1 {
   function getEmployee( id : int ) : GetEmployee {
     return new GetEmployee() {
       :FirstName = "Joe",
       :LastName = "Blow",
       :Age = 21
     }
   }

   function add( i1 : int , i2 : int ) : int {
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

 class ThrowsImpl {
   function exception() {
     throw "Good times, good times"
   }

   function deepException( depth : int ) {
     if(depth <= 0) {
       throw "Good times, good times"
     } else {
       deepException( depth - 1 )
     }
   }

   function npeException() {
     var x : String = null
     print( x.length() )
   }
 }

}