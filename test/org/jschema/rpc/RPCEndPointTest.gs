package org.jschema.rpc

uses java.util.*
uses java.lang.*
uses java.math.*
uses org.jschema.test.*
uses org.jschema.rpc.*
uses org.jschema.model.*
uses org.jschema.examples.rpc.ValidationBasis
uses org.jschema.examples.rpc.ReturnArgValidation
uses org.jschema.examples.rpc.ArgTypeValidation

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

 function testReturnTypesArePromiscuousWhores()
 {
    var endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl1(), "/returntype")
    endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl2(), "/returntype")
    endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl3(), "/returntype")
    endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl4(), "/returntype")
    endPoint = new RPCEndPoint(ReturnArgValidation, new ReturnArgValidationImpl5(), "/returntype")
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

    function voidArgArrayReturn() : List<Long>
    {
        return(null)
    }

    function voidArgMapReturn() : Map<String, Long>
    {
        return(null)
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

    function voidArgArrayReturn() : ArrayList<Long>
    {
        return(null)
    }

    function voidArgMapReturn() : Map<String, Long>
    {
        return(null)
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

    function voidArgArrayReturn() : List<byte>
    {
        return(null)
    }

    function voidArgMapReturn() : Map<String, byte>
    {
        return(null)
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

     function voidArgArrayReturn() : List<int>
     {
        return(null)
     }

    function voidArgMapReturn() : Map<String, int>
    {
        return(null)
    }

    }


  class RidiculousListDerivation extends ArrayList<int>
  {
  }

  class RidiculousMapDerivation extends HashMap<String, int>
  {
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

    function voidArgArrayReturn() : RidiculousListDerivation
    {
        return(null)
    }

    function voidArgMapReturn() : RidiculousMapDerivation
    {
        return(null)
    }
  }

 function testArguementTypesAreAsSluttyAsReturnTypes()
 {
    var endPoint = new RPCEndPoint(ArgTypeValidation, new ArgTypeValidationImpl1(), "/argtype")
    endPoint = new RPCEndPoint(ArgTypeValidation, new ArgTypeValidationImpl2(), "/argtype")
    endPoint = new RPCEndPoint(ArgTypeValidation, new ArgTypeValidationImpl3(), "/argtype")
    endPoint = new RPCEndPoint(ArgTypeValidation, new ArgTypeValidationImpl4(), "/argtype")
    endPoint = new RPCEndPoint(ArgTypeValidation, new ArgTypeValidationImpl5(), "/argtype")
 }

 class ArgTypeValidationImpl1{
    function intArgVoidReturn(arg1 : byte)
    {
        return;
    }

    function numberArgVoidReturn(arg1 : float)
    {
        return;
    }
 }


  class ArgTypeValidationImpl2{
     function intArgVoidReturn(arg1 : int)
     {
         return;
     }

     function numberArgVoidReturn(arg1 : double)
     {
         return;
     }
  }


  class ArgTypeValidationImpl3{
     function intArgVoidReturn(arg1 : long)
     {
         return;
     }

     function numberArgVoidReturn(arg1 : float)
     {
         return;
     }
  }


 class ArgTypeValidationImpl4{
    function intArgVoidReturn(arg1 : Byte)
    {
        return;
    }

    function numberArgVoidReturn(arg1 : Float)
    {
        return;
    }
 }

 class ArgTypeValidationImpl5{
    function intArgVoidReturn(arg1 : Integer)
    {
        return;
    }

    function numberArgVoidReturn(arg1 : Double)
    {
        return;
    }
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



}