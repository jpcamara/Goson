package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses org.jschema.test.*
uses org.jschema.rpc.*
uses org.jschema.examples.rpc.Sample1
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

 class Impl1 {
   function getEmployee( id : int ) : GetEmployee {
     return new GetEmployee() {
       :FirstName = "Joe",
       :LastName = "Blow",
       :Age = 21
     }
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



}