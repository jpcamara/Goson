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
uses org.jschema.examples.rpc.Adder
uses org.jschema.examples.rpc.ReturnsArrays
uses org.jschema.util.JettyStarter
uses gw.lang.reflect.*

class JSchemaRPCTypesTest extends GosonTest {

  function testSample1Loads() {
    var type = TypeSystem.getByFullName("org.jschema.examples.rpc.Sample1")
    assertTrue(type.Valid)
  }

 function testBootstrapFile() {
   var emp = Sample1
                .with( :handler = \ method, url, args -> '{ "first_name" : "Joe", "last_name" : "Blow", "age" : 21, "id" : 42 }' )
                .getEmployee(22)

   assertEquals( "Joe", emp.FirstName )
   assertEquals( "Blow", emp.LastName )
   assertEquals( 21L, emp.Age )
   assertEquals( 42L, emp.Id )
 }

 function testBootstrapAdd() {
   var server = new RPCServer()
   server.addEndPoint( new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) )
   using( server ) {
     assertEquals( 2L, Sample1.add(1, 1) )
     assertEquals( 0L, Sample1.add(1, -1) )
     assertEquals( -1L, Sample1.add(0, -1) )
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
     assertEquals( 21L, emp.Age )
     assertEquals( 42L, emp.Id )
   }
 }

 function testBootstrapFileRemotelyWithPost() {
   var server = new RPCServer()
   server.addEndPoint( new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) )
   using( server ) {
     var emp = Sample1.getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21L, emp.Age )
     assertEquals( 42L, emp.Id )
   }
 }

 function testBootstrapFileRemotelyWithGetOnJetty() {
   using( JettyStarter.server( 12321, new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) ) ) {
     var emp = Sample1
                  .with( :method = GET )
                  .getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21L, emp.Age )
     assertEquals( 42L, emp.Id )
   }
 }

 function testBootstrapFileRemotelyWithPostOnJetty() {
   using( JettyStarter.server( 12321, new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) ) ) {
     var emp = Sample1
                  .getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21L, emp.Age )
     assertEquals( 42L, emp.Id )
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
     assertEquals( 21L, emp.Age )
     assertEquals( 42L, emp.Id )
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
     assertEquals( 21L, emp.Age )
     assertEquals( 42L, emp.Id )
   }
 }

 function testBootstrapFileRemotelyWithGetUsingApacheHTTPClientOnJetty() {
   using( JettyStarter.server( 12321, new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) ) ) {
     var emp = Sample1
                  .with( :handler = new ApacheHTTPClientCallHandler(),
                         :method = GET )
                  .getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21L, emp.Age )
     assertEquals( 42L, emp.Id )
   }
 }

 function testBootstrapFileRemotelyWithPostUsingApacheHTTPClientOnJetty() {
   using( JettyStarter.server( 12321, new RPCEndPoint( Sample1, new Impl1(), "/sample1" ) ) ) {
     var emp = Sample1
                  .with(:handler = new ApacheHTTPClientCallHandler())
                  .getEmployee(22)

     assertEquals( "Joe", emp.FirstName )
     assertEquals( "Blow", emp.LastName )
     assertEquals( 21L, emp.Age )
     assertEquals( 42L, emp.Id )
   }
 }

 class Impl1 {
   function getEmployee( id : Long ) : GetEmployee {
     return new GetEmployee() {
       :FirstName = "Joe",
       :LastName = "Blow",
       :Age = 21,
       :Id = 42
     }
   }

   function updateEmployee(emp : Employee) : Boolean
   {
     return(true)
   }

   function add( i1 : Long , i2 : Long ) : Long {
     return i1 + i2
   }
 }

 function testBootstrapWithTypeDef() {
 
   var emp = Sample2
                .with( :handler = \ method, url, args -> '{ "first_name" : "Joe", "last_name" : "Blow", "age" : 21, "id" : 42 }' )
                .getEmployee(22)

   assertEquals( "Joe", emp.FirstName )
   assertEquals( "Blow", emp.LastName )
   assertEquals( 21L, emp.Age )

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

   function deepException( depth : Long ) {
     if(depth.intValue() <= 0) {
       throw "Good times, good times"
     } else {
       deepException( new Long(depth.intValue() - 1) )
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

      assertEquals( 123421L, Defaults.identityInteger( 123421 ) )
      assertEquals( 123421L, Defaults.identityInteger( :i = 123421 ) )
      assertEquals( null, Defaults.identityInteger( null ) )
      assertEquals( null, Defaults.identityInteger( :i = null ) )

      assertEquals( 123421L, Defaults.identityIntegerWithDefault( 123421 ) )
      assertEquals( 123421L, Defaults.identityIntegerWithDefault( :i = 123421 ) )
      assertEquals( 42L, Defaults.identityIntegerWithDefault( null ) )
      assertEquals( 42L, Defaults.identityIntegerWithDefault() )
      assertEquals( 42L, Defaults.identityIntegerWithDefault( :i = null ) )
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
      assertEquals( 1L, Defaults.identityObject( 1 ) )
      assertEquals( 1L, Defaults.identityObject( :obj = 1 ) )
      assertEquals( true, Defaults.identityObject( true ) )
      assertEquals( true, Defaults.identityObject( :obj = true ) )
      assertEquals( {1L, 2L, 3L}, Defaults.identityObject( {1, 2, 3} ) )
      assertEquals( {1L, 2L, 3L}, Defaults.identityObject( :obj = {1, 2, 3} ) )
      assertEquals( {"a" -> 1L, "b" -> 2L, "c" -> 3L}, Defaults.identityObject( {"a" -> 1, "b" -> 2, "c" -> 3} ) )
      assertEquals( {"a" -> 1L, "b" -> 2L, "c" -> 3L}, Defaults.identityObject( :obj = {"a" -> 1, "b" -> 2, "c" -> 3} ) )
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

    function identityInteger(i : Long) : Long {
      return i
    }

    function identityObject(o : Object) : Object {
      return o
    }

    function identityIntegerWithDefault(i : Long) : Long {
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

 function testLogger() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( Adder, new AdderImpl(), "/adder" ) )
    using( server ) {
      try {
        var logMessages = {}
        var theAnswer = Adder
                          .with( :logger = \ s -> {
                            print( s )
                            logMessages.add( s )
                          })
                          .add(40, 2)
        assertEquals( 42L, theAnswer )
        assertFalse( logMessages.Empty )
      } catch( e ) {
        //pass
      }
    }
 }

 function testWrapper() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( Adder, new AdderImpl(), "/adder" ) )
    using( server ) {
      try {
        var logMessages = {}
        var theAnswer = Adder
                          .with( :wrapper = \ url, callback -> {
                            logMessages.add("Before")
                            print("Before ${url}")
                            print(statictypeof callback)
                            var val = callback.call()
                            logMessages.add("After ${url}")
                            print("After ${url}")
                            return val as String
                          })
                          .add(40, 2)
        assertEquals( 42L, theAnswer )
        assertEquals( 2, logMessages.Count )
      } catch( e ) {
        //pass
      }
    }
 }

 function testGlobalLogger() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( Adder, new AdderImpl(), "/adder" ) )

    var logMessages = {}
    RPCDefaults.setLogger( \ s -> {
                            print( s )
                            logMessages.add( s )
                          })
    try {
      using( server ) {
        try {
          var theAnswer = Adder.add(40, 2)
          assertEquals( 42L, theAnswer )
          assertEquals(2, logMessages.Count)
        } catch( e ) {
          //pass
        }
      }
    } finally {
      RPCDefaults.setLogger(null)
    }
 }

 function testGlobalWrapper() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( Adder, new AdderImpl(), "/adder" ) )

    var logMessages = {}
    RPCDefaults.setCallWrapper( \ url, callback -> {
                              logMessages.add("Before")
                              print("Before ${url}")
                              print(statictypeof callback)
                              var val = callback.call()
                              logMessages.add("After ${url}")
                              print("After ${url}")
                              return val as String
                          } )
    RPCDefaults.setHandlerWrapper( \ url, callback -> {
                              logMessages.add("Before")
                              print("Before ${url}")
                              print(statictypeof callback)
                              var val = callback.call()
                              logMessages.add("After ${url}")
                              print("After ${url}")
                              return val as String
                          } )
    try {
      using( server ) {
        try {
          var theAnswer = Adder.add(40, 2)
          assertEquals( 42L, theAnswer )
          assertEquals(4, logMessages.Count)
        } catch( e ) {
          //pass
        }
      }
    } finally {
      RPCDefaults.setCallWrapper(null)
      RPCDefaults.setHandlerWrapper(null)
    }
 }

 class AdderImpl {
   function add(i : Long, j : Long) : Long {
     return i + j
   }
 }

  function testArrays() {
    var server = new RPCServer()
    server.addEndPoint( new RPCEndPoint( ReturnsArrays, new ReturnsArraysImpl(), "/arrs" ) )
    using( server ) {
      assertEquals( {1L, 2L}, ReturnsArrays.intArray() )
      assertEquals( { new ReturnsArrays.Example(){ :Foo = "blah" } }, ReturnsArrays.refArray() )
    }
  }

  class ReturnsArraysImpl {
    function intArray() : List<Long> {
      return {1L, 2L}
    }

    function refArray() : List<ReturnsArrays.Example> {
      return { new ReturnsArrays.Example(){ :Foo = "blah" } }
    }
  }


}