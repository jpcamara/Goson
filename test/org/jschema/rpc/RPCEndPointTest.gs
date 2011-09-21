package org.jschema.rpc

uses java.util.*
uses java.lang.*
uses java.math.*
uses org.jschema.test.*
uses org.jschema.rpc.*
uses org.jschema.examples.rpc.ValidationBasis
uses org.jschema.examples.rpc.ReturnArgValidation

class RPCEndPointTest extends GosonTest {

// RPCEndPoint.validate() tests.

 function testValidateThrowsForIncompleteImpl()
 {
    try{
        var endPoint = new RPCEndPoint(ValidationBasis, new IncompleteValidation(), "/validation")
        fail("Exception not thrown")
    }
    catch(jse : JSchemaRPCException) {
        // Gulp
    }
 }

 function testValidateThrowsForIncorrectArgType()
 {
     try{
         var endPoint = new RPCEndPoint(ValidationBasis, new IncorrectArgTypeValidation(), "/validation")
         fail("Exception not thrown")
     }
     catch(jse : JSchemaRPCException) {
         // Gulp
     }
 }

 function testValidateThrowsForIncorrectArgCount()
 {
     try{
         var endPoint = new RPCEndPoint(ValidationBasis, new IncorrectArgCountValidation(), "/validation")
         fail("Exception not thrown")
     }
     catch(jse : JSchemaRPCException) {
         // Gulp
     }
 }

 function testValidateThrowsForIncorrectReturnType()
 {
    try{
        var endPoint = new RPCEndPoint(ValidationBasis, new IncorrectReturnValidation(), "/validation")
        fail("Exception not thrown")
    }
    catch(jse : JSchemaRPCException) {
        // Gulp
    }
 }

 function testValidateAggregatesMultipleErrors()
 {
     try{
        var endPoint = new RPCEndPoint(ValidationBasis, new MultipleErrorValidation(), "/validation")
        fail("Exception not thrown")
    }
    catch(rp : JSchemaRPCException){
        var lines = rp.getMessage().split("\n");
        assertEquals(4, lines.length)
    }

 }

 function testReturningPrimitiveTypeWorks()
 {
    var endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl1(), "/returntype")
    endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl2(), "/returntype")
    endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl3(), "/returntype")
    endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl4(), "/returntype")
    endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl5(), "/returntype")
 }


 class MultipleErrorValidation{
    function intArgVoidReturn(arg1 : String) : Boolean
    {
        return(Boolean.TRUE);
    }
 }


 class IncompleteValidation {
    function intArgVoidReturn(arg1 : Long)
    {
        return;
    }
    // Missing intArgBoolArgBooleanReturn
 }

 class IncorrectArgTypeValidation{
    function intArgVoidReturn(arg1 : Long)
    {
        return;
    }

    function intArgBoolArgBooleanReturn(arg1 : String, arg2 : Boolean) : Boolean
    {
        return(Boolean.TRUE)
    }
 }

 class IncorrectArgCountValidation{
    function intArgVoidReturn(arg1 : Long)
    {
        return;
    }

    function intArgBoolArgBooleanReturn(arg1 : Long) : Boolean
    {
        return(Boolean.TRUE)
    }
 }

 class IncorrectReturnValidation{
     function intArgVoidReturn(arg1 : Long)
     {
         return;
     }

     function intArgBoolArgBooleanReturn(arg1 : Integer, arg2 : Boolean) : Long
     {
         return(new Long(1))
     }
  }

  class ReturnArgValidationImpl1
  {
    function intArgIntReturn(arg1: Long) : int
    {
        return(23)
    }

    function intArgNumberReturn(arg1 : Long) : float
    {
        return(23.23)
    }

    function voidArgBooleanReturn() : Boolean
    {
        return(Boolean.TRUE)
    }
  }

  class ReturnArgValidationImpl2
  {
    function intArgIntReturn(arg1: Long) : long
    {
        return(23L)
    }

    function intArgNumberReturn(arg1 : Long) : double
    {
        return(23.23)
    }

    function voidArgBooleanReturn() : Boolean
    {
        return(Boolean.TRUE)
    }

  }

  class ReturnArgValidationImpl3
  {
    function intArgIntReturn(arg1: Long) : byte
    {
        return(23)
    }

    function intArgNumberReturn(arg1 : Long) : double
    {
        return(23.23)
    }

    function voidArgBooleanReturn() : Boolean
    {
        return(Boolean.TRUE)
    }
  }

    class ReturnArgValidationImpl4
    {
      function intArgIntReturn(arg1: Long) : Integer
      {
          return(23)
      }

      function intArgNumberReturn(arg1 : Long) : Double
      {
          return(23.23)
      }

     function voidArgBooleanReturn() : Boolean
     {
        return(Boolean.TRUE)
     }
    }

  class ReturnArgValidationImpl5
  {
    function intArgIntReturn(arg1: Long) : Byte
    {
        return(23)
    }

    function intArgNumberReturn(arg1 : Long) : Float
    {
        return(23.23)
    }

     function voidArgBooleanReturn() : boolean
     {
        return(Boolean.TRUE)
     }
  }

}