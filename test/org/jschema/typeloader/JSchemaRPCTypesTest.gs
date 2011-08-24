package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses org.jschema.test.*
uses org.jschema.examples.rpc.Sample1
uses org.jschema.examples.rpc.Sample1.UpdateEmployee.Employee

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
   var result = Sample1
                .with( :handler = \ method, url, args -> {
                  assertNotNull(args["employee"])
                  return 'true'
                 } )
                .updateEmployee(new Employee() {
                  :FirstName = "Joe",
                  :LastName = "Blow",
                  :Age = 21
                })

   assertTrue( result )
 }

}