package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses org.jschema.test.*
uses org.jschema.examples.rpc.Sample1

class JSchemaRPCTypesTest extends GosonTest {

 function testBootstrapFile() {
   var emp = Sample1.getEmployee(22)
 }

}