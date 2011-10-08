# Introduction

Goson provides tools for working with JSON:

* Tools for parsing, validating and producing JSON
* A Gosu type loader For [JSchema](http://jschema.org) (`.jsc`), [JSON](http://json.org) (`.json`) and [JSchema-RPC](http://jschema.org/rpc.html) (`.jsc-rpc`) documents
* A servlet filter that can be used to publish JSchema-RPC end points
* A simple HTTP server that can be used to publish JSchema-RPC end points

As with other Gosu type loaders, you need only to add the goson jar to your project's classpath to start using the JSchema-based types.

# JSchema Support

Goson provides support for JSchema, a simple schema mechanism for JSON.  

Given the following JSchema file in your source directory at `src/jschema/Invoice.jsc`:

    {
      "typedefs@" : {
        "Address" : {
           "type" : { "enum" : ["business", "residential"] },
           "line1" : "string",
           "line2" : "string",
           "city" : "string",
           "state" : "string",
           "country" : "string",
           "zip" : "int"
         }
      },
      "id" : "int",
      "date" : "date",
      "billing_address" : "Address",
      "customers" : [ {
        "first_name" : "string",
        "last_name" : "string",
        "business_name" : "string",
        "address" : "Address"
       } ],
       "items" : [ {
         "name" : "string",
         "sku" : "number",
         "amount" : "number",
         "unit_cost" : "number",
         "total_cost" : "number"
       } ]
    }

The Goson type loader will create the following types:

* `jschema.Invoice`
* `jschema.Invoice.Address`
* `jschema.Invoice.Customers`
* `jschema.Invoice.Items`

These types can be used to work with JSON documents described by this JSchema

## Creating An Invoice

Creating an Invoice based on this schema in Gosu is quite simple:

    uses jschema.Invoice
  
    var invoice = new Invoice() {
      :Id = 42,
      :Date = Date.Today,
      :BillingAddress = new() {
        :Type = BUSINESS,
        :Line1 = "123 Main Street",
        :City = "Menlo Park",
        :State = "CA",
        :Country = "USA",
        :Zip = 12345
      },
      :Customers = {
        new() {
          :FirstName = "Ted",
          :LastName = "Smith",
          :Address = new() {
            :Type = RESIDENTIAL,
            :Line1 = "1122 G Street",
            :City = "Stockton",
            :State = "CA",
            :Country = "USA",
            :Zip = 12345
          }
        }
      },
      :Items = {
        new() { :Name = "Cornmeal", :Amount = 1, :TotalCost = 5.99 }
      }
    }

Note that in this code we are using the [Object Initializer](http://lazygosu.org/misc.html) syntax in Gosu to declare this invoice.  This is a general feature of Gosu and is not specific to JSchema.

As you can see properties are created for each member in the JSchema document, making it easy to create an mutate JSchema-based objects.  As an example, let's add a new item to the invoice:

    invoice.Items.add( new() { :Name = "Rice", :Quantity = 2, :TotalCost = 8.39 } )

And we can update update the invoice ID like so:

    invoice.Id = 43

And, of course, we can use the enhancement methods that Gosu provides on lists for data structure manipulation:

    invoice.Items.where( \ i -> i.TotalCost > 5.00 )
                 .each( \ i -> print( i.Name ) )

## Serializing To A JSON String

You can write the JSchema object to a JSON string by calling the `write()` method:

    print( invoice.write() )

Or, if you want nice whitespace in your JSON, use the `prettyPrint()` method, which has an optional indent argument:

    print( invoice.prettyPrint() )
    print( invoice.prettyPrint(4) )

## Parsing From JSON

To parse JSON content, there are a set of static `parse()` methods on JSchema types:

    var invoice = '{ "id" : 1 }' // A very boring invoice

    var invoice = Invoice.parse( str )

    print( "The ID of the Invoice is ${invoice.Id}" )

## Using HTTP

If you have a JSchema document that describes an http end point, you can easily do an HTTP `Get` or `Post` to retrieve a document from the URL:

    var invoiceViaGet = Invoice.get( "http://example.com/invoices", { "id" -> 42 } )

    var invoiceViaPost = Invoice.post( "http://example.com/invoices", { "id" -> 42 } )

Both methods allow you to pass arguments via a Map, as an optional second argument.

## Finding Things

JSchema does not have a JPath-like system, instead relying on programming languages to provide such functionality.

### Finding Everything

The `descendents()` method returns all nodes at or below the node it is invoked on.  This can be used to search the entire tree:

    invoice.descendents().whereTypeIs(Invoice.Address)
                         .where( \ addr -> addr.State == "CA" )
                         .each( \ addr -> print( "Found CA Address : ${addr}" ) )

### Finding Specific Things

Finding all nodes of a specific type is a common operation, so there is a `find()` method that takes a specific type and returns all descendents (again, inclusive of the root node) of that type.  Using this method, the code above can be simplified to:

    invoice.find(Invoice.Address)
           .where( \ addr -> addr.State == "CA" )
           .each( \ addr -> print( "Found CA Address : ${addr}" ) )

### Getting The Parent

Goson objects maintain pointer to their parent objects, which can be accessed via the `parent()` method:

    invoice.find(Invoice.Address)
           .each( \ addr -> print( "Address Parent: ${addr.parent()}" ) )

The `parent()` method will be strongly typed where possible, so, for example, the return type of `parent()` on the `jschema.Invoice.Items` type is `jschema.Invoice`, since it is an inline type, whereas for `jschema.Invoice.Address` is it `Object`, since an Address can belong to multiple parents (i.e. both `jschema.Invoice` and `jschema.Invoice.Customers`)

## Conversions

You can convert from one JSchema type to another using the `convertTo(Type)` method.  This will create a new object of the type passed in and fill in the properties of it based on the object that `convertTo()` was called on.

Let's say you have two different schemas, `Schema1.jsc`:

    {
      "name" : "string",
      "age" : "int"      
    }

And `Schema2.jsc`:

    {
      "name" : "string",
      "age" : "int",
      "title" : "string"
    }

You could convert an object of type `Schema2` to `Schema1` like so:

    var schema2obj = Schema2.get( "http://someserver.com" )
    
    var schema1obj = schema2obj.convertTo(Schema1)

Note that this only works if the type being converted to has a subset of the properties type being converted from.  Presently `convertTo()` will fail at runtime if this is not the case, but we intend to make it a compile time error.

# JSON Types

Unfortunately, most JSON content providers do not provide JSchema schemas for their content.  Instead they typically give example JSON documents.   Fortunately, it is simple to derived a JSchema from a sample JSON document, and Goson will do this on the fly for you if you put a JSON document in your source directory.

Asn an example, Twitter offers an sample document from their User Timeline API here: [https://dev.twitter.com/docs/api/1/get/statuses/user_timeline.](https://dev.twitter.com/docs/api/1/get/statuses/user_timeline.)

If you download this sample to `src/jschema/TwitterUserTimeline.json`, you can write the following code:

    var latestTweets = jschema.TwitterUserTimeline.get("http://api.twitter.com/1/statuses/user_timeline.json",
                                                       { "include_entities" -> true,
                                                         "include_rts" -> true,
                                                         "screen_name" -> "carson_gross",
                                                         "count"-> 5 } )
    for( tweet in latestTweets ) {
      print( tweet.Text )
    }

Some type information is lost (e.g. enums are simply strings), but it is still much more pleasant to work with this API than the untyped alternative.

# Raw JSON Objects

The Goson library includes support for working with raw JSON.  This functionality is outlined below.

## Parsing Raw JSON Objects

Goson ships with a general JSON parser that returns objects from the model found in the `org.jschema.model` package.  These model classes are based on the Java Collections interfaces, with `JsonList` extending `List` and `JsonMap` extending `Map`, but add the concept of a parent pointer, effectively modeling the JSON tree.  Note this is in contrast to the standard JSON library, which does not implement the java Collections interfaces.

The easiest way to parse raw JSON is the static method `org.jschema.util.JSchemaUtils.parseJsonObject()` which will return a `org.jschema.model.JsonMap`, which extends `Map<String, Object>`.  You can manipulate this object using the standard `java.util.Map` methods, such as `get()` and `put()`:

    var rawJson = JSchemaUtils.parseJsonObject( '{"foo" : 10, "bar" : "a string"}' )
    
    print( rawJson.get( "foo") ) // prints '10'
    
    print( rawJson.get( "bar") ) // prints 'a string'
    

In addition to the usual methods on `Map`, `JsonMap` also has:

* `getMap(String name)` - returns the value cast to a JsonMap.
* `getList(String name)` - returns the value cast to a JsonList.
* `getString(String name)` - returns the value cast to a String.
* `getNumber(String name)` - returns the value cast to a Number.
* `getDecimal(String name)` - returns the value cast to a BigDecimal.
* `getInt(String name)` - returns the value cast to a Long.
* `getBoolean(String name)` - returns the value cast to a Boolean.
* `write()` - returns a JSON serialized version of the object.
* `prettyPrint()` - returns a JSON serialized version of the object with nice formatting.
* `parent()` - returns the parent object of this object.
* `descendents()` - returns the descendents of this map.

`JsonList` has similar methods.

## Creating Raw JSON Objects

JSON objects can be created quite easily by using the data structure literal syntax of Gosu:

    var someJson = new JsonMap() {
      "foo" -> 10,
      "bar" -> {"this", "is", "a", "list"},
      "nested_obj" -> {
        "foo" -> 10,
        "bar" -> "A string"
      }
    }
    
    print( someJson.prettyPrint() )

will print this JSON string:

    {
      "foo" : 10, 
      "bar" : ["this", "is", "a", "list"], 
      "nested_obj" : {
        "foo" : 10, 
        "bar" : "A string"
      }
    }

## Getting Raw JSON From JSchema Objects

At runtime, Goson objects are actually simply `org.jschema.model.JsonMap`'s.  You can get at this underlying map for direct manipulation via the `asJSON` method:

    var map = invoice.asJSON()

    print( "Raw ID: ${map.get("id")}" )
    map.put( "foo", "bar" ) // stash a non-schema based value away

Note that the keys of the map will be raw string values, and *may not* necessarily correspond to the property names (in fact often they will be different).

It is obviously possible to subvert the type system in this manner, and store, say, a string where a boolean is expected, but we are all adults here, right?

# JSchema RPC

[JSchema RPC](http://jschema.org/rpc.html) builds on JSchema to allow for the easy specification of RPC end points using JSchema as a core data specification layer.

## JSchema-RPC Example

JSchema RPC types are defined in `.jsc-rpc` files.  Here is a simple example:

    { "url" : "http://myserver:8080/api/employees",
      "description" : "Methods for manipulating employees",
      "typedefs@" : {
        "Employee" : {
          "first_name" : "string",
          "last_name" : "string",
          "age" : "int",
          "id" : "int"
        }
      }
      "functions" : [
        { "name" : "getEmployee",
          "description" : "Returns the employee of the given id",
          "args" : [ {"id" : "int" } ],
          "return_type" : "Employee"
        },
        { "name" : "listEmployees",
          "description" : "Returns all employees",
          "args" : [],
          "return_type" : [ "Employee" ]
        },
        { "name" : "updateEmployee",
          "description" : "Updates the given employee",
          "args" : [ { "employee", "Employee" } ],
          "return_type" : "boolean"
        }
      ]
    }

If this content were in the `src/api/EmployeeService.jsc-rpc` file, then the following types would be created:

* `api.EmployeeService` - The RPC type that you can invoke methods on.
* `api.EmployeeService.CustomInstance` - A customized version of the endpoint (can be used to change the URL, for example).
* `api.EmployeeService.Employee` - A JSchema type for the typedef.

## Invoking Remote Methods

With the file above in your source directory you can invoke a JSchema RPC method like so:

    var myEmp = api.EmployeeController.getEmployee(42)

    myEmp.Age++

    if( api.EmployeeController.updateEmployee(myEmp) ) {
      print( "Updated the age of ${myEmp.FirstName}")
    }

For each method declaration found in the `jsc-rpc` file, there will be a corresponding static method available on the RPC type in Gosu.

Note that the `emp` variable is of type `rpc.EmployeeService.Employee`, a JSchema type with all the functionality mentioned above.

## Customizing RPC Instances

If you want to change the behavior of an RPC object you can use the `with()` method.  As an example, if you wanted to change the URL that an RPC invocation will be done against, you could write this:

    var customizedRPCService = api.EmployeeController.with( :url = "http://someotherserver/api/employees" )

    var myEmp = customizedRPCService.getEmployee(42)

    myEmp.Age++

    if( customizedRPCService.updateEmployee(myEmp) ) {
      print( "Updated the age of ${myEmp.FirstName}")
    }

The following customizations are available:

* `handler : org.jschema.rpc.RPCCallHandler` - Handles the HTTP invocation
* `url : String` - The root URL to invoke against
* `method : org.jschema.rpc.HttpMethod` - GET or POST
* `includeNulls : Boolean` - Include local null values when invoking remotely (rarely needed)
* `logger : org.jschema.rpc.RPCLoggerCallback` - A logger to be used while dispatching
* `wrapper : org.jschema.rpc.RPCInvocationWrapper` - An object that wraps the RPC call

## Global Defaults

You can set the Global Defaults for the RPC system using the `org.jschema.rpc.RPCDefaults` class.  This lets you customize the RPC layer wholesale, rather than doing it one service at a time like we did above.

As an example, we can set the default request hander to be based on Apache HTTPClient with this code:

    RPCDefaults.setDefaultHandler(new ApacheHTTPClientCallHandler())

`ApacheHTTPClientCallHandler` is based on the Apache HTTPClient and is more advanced than the default handler (for example, it supports `https`).

Note that the Apache HTTPClient jar does not ship with the Goson library and will need to be included separately for `ApacheHTTPClientCallHandler` to work.

## Publishing JSchema-RPC

There are many options for publishing JSchema-RPC in Gosu:

### The Built In RPC Server

You can easily publish simple JSchema RPC end points using the build in RPC server:


    var server = new RPCServer()

    server.addEndPoint( new RPCEndPoint( api.EmployeeService, new api.EmployeeServiceImpl(), "/employees" ) )

    server.start()
    
The `EmployeeServiceImpl` class is a Gosu class that implements all of the methods specified in the JSchema-RPC file.

#### A Slight Digression on RPC

Note that the methods are defined in terms of the JSchema RPC types, so both sides of the wire are using the *same JSchema types*.

Thus there is no mapping or object serialization layer: that responsibility falls on the API publisher.

That may seem a bit strange, but it is very much by design.  Our experience has been that a mapping layer between an RPC system and a custom class domain is rarely clean.  Sometimes you may want a property migrated across, sometimes you may want to transform it, sometimes you may want to make a property not available at all.  Sometimes an API object maps to a domain object, sometimes it maps to multiple domain objects, sometimes it doesn't map to any domain object at all.

It just depends.

Our take is that it is better to let the API designer make these decisions in plain-old debuggable code.

## Ronin

[Ronin](http://ronin-web.org) makes a great host environment for JSchema-RPC, by using the `org.jschema.rpc.RPCFilter` class in your `RoninConfig` constructor:

    construct(m : ApplicationMode, an : RoninServlet) {
      super(m, an)

      DefaultController = controller.MyDefaultController

      // create a new RPC filter with an end point at /api/employees
      var rpcFilter = new RPCFilter().withEndPoint( new RPCEndPoint( EmployeesApi, new EmployeesApiImpl(), "/api/employees" ) )

      // add it to the Ronin filter list
      Filters.add(rpcFilter)

      // tie the RPC logger to Ronin's logger
      RPCDefaults.setLogger( \ msg -> Ronin.log( :msg = msg, :component = "JSchema-RPC" ) )

      // tie the RPC into Ronin's trace support
      RPCDefaults.setHandlerWrapper( \ url, callback -> {
                              using( Ronin.CurrentTrace?.withMessage("[rpc] ${url}")  ) {
                                return callback.call() as String
                              }
                            } )

      if(m == DEVELOPMENT) {
        AdminConsole.start()
      }
    }

The code above publishes the `EmployeesApi` and integrated the RPC system into Ronin's excellent logging and tracing subsystems.  (How's that for dependency injection?)

## Arbitrary Servlets

Although Ronin is the easiest, you can use the `org.jschema.rpc.RPCFilter` to publish JSchema RPC Endpoints in any Servlet environment that has a properly set up Gosu TypeSystem.

`RPCFilter` takes a single config parameter, `config`, which should be the neame of a class that implements `org.jschema.rpc.RPCFilter.Config` and has a default constructor.  This config object supplies the list of endpoints to the filter.