package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses java.net.*

uses org.jschema.test.*

uses org.jschema.examples.fullexample.Example
uses org.jschema.examples.fullexample.Example.SomeType
uses org.jschema.examples.fullexample.Example.SomeType.TypeInArray
uses org.jschema.examples.fullexample.Example.SomeType.EnumEx
uses org.jschema.examples.fullexample.Example.SomeType.NestedType
uses org.jschema.examples.fullexample.Example.SomeType.NestedType.NestedTypeInArray

uses org.jschema.examples.twitter.status.StatusResponse
uses org.jschema.examples.twitter.status.StatusResponse.User

uses org.jschema.examples.google.geocode.GeocodeResponse
uses org.jschema.examples.google.geocode.GeocodeResponse.Results
uses org.jschema.examples.google.geocode.GeocodeResponse.Results.Types
uses org.jschema.examples.google.geocode.GeocodeResponse.Results.Geometry
uses org.jschema.examples.google.geocode.GeocodeResponse.Results.Geometry.Location

uses org.jschema.examples.PeopleId
uses org.jschema.examples.PeopleId.IdToPeople
uses org.jschema.examples.PeopleId.IdToPeople.EyeColor
uses org.jschema.examples.NameAndAge
uses org.jschema.examples.SelfTest
uses org.jschema.examples.URITest
uses org.jschema.examples.AutoCreateAndInsertTest
uses org.jschema.examples.cloning.*

uses org.jschema.examples.people1.Peeps
uses org.jschema.examples.people1.Peeps.People

class JSchemaTypesTest extends GosonTest {

  function testOne() {
    /* { "name" : "string", "age" : "integer" } */
    /* { "name" : "Joe", "age" : "42" } */
    var person = new NameAndAge() {
      :Name = "Joe",
      :Age = 42
    }
    print(NameAndAge.parse(person.write()).write())

    /*
    { "people" : [ { "name" : "string",
                     "age" : "integer"} ] }
    */
    /*
    { "people" : [
      { "name" : "Joe", "age" : "42" }
      { "name" : "Paul", "age" : "28" }
      { "name" : "Mack", "age" : "55" } ] }
    */
    var people = new Peeps() {
      :People = {
        new People() { :Name = "Joe", :Age = 42 },
        new People() { :Name = "Paul", :Age = 28 },
        new People() { :Name = "Mack", :Age = 55 }
      }
    }
    print(Peeps.parse(people.write()).write())

    /*
    { "people" : [ { "name" : "string",
                     "age" : "integer",
                     "eye_color" : {"enum" : ["brown",
                                              "blue",
                                              "green"]}} ] }
    */
    /*
    { "people" : [
      { "name" : "Joe", "age" : "42", "eye_color" : "brown" },
      { "name" : "Paul", "age" : "28", "eye_color" : "brown" },
      { "name" : "Mack", "age" : "55", "eye_color" : "blue" } ] }
    */
    /*var people2 = new jschema.people2.Peeps() {
      :People = {
        new jschema.people2.People() { :Name = "Joe", :Age = 42, :EyeColor = jschema.people2.EyeColor.BROWN },
        new jschema.people2.People() { :Name = "Paul", :Age = 28, :EyeColor = jschema.people2.EyeColor.BROWN },
        new jschema.people2.People() { :Name = "Mack", :Age = 55, :EyeColor = jschema.people2.EyeColor.BLUE }
      }
    }
    print(jschema.people2.Peeps.parse(people2.write()).write())*/

    /*
    { "id_to_people" : {
        "map_of" : { "name" : "string",
                    "age" : "integer",
                    "eye_color" : {"enum" : ["brown",
                                             "blue",
                                             "green"]}
        }
      }
    }
    */
    /*
    { "id_to_people" : {
        "1" : { "name" : "Joe", "age" : "42", "eye_color" : "brown" },
        "2" : { "name" : "Paul", "age" : "28", "eye_color" : "brown" },
        "3" : { "name" : "Mack", "age" : "55", "eye_color" : "blue" }
      }
    }
    */
    var peopleMap = new PeopleId() {
      :IdToPeople = {
        "1" -> new IdToPeople() { :Name = "Joe", :Age = 42, :EyeColor = EyeColor.BROWN },
        "2" -> new IdToPeople() { :Name = "Paul", :Age = 28, :EyeColor = EyeColor.BROWN },
        "3" -> new IdToPeople() { :Name = "Mack", :Age = 55, :EyeColor = EyeColor.BLUE }
      }
    }
    print(PeopleId.parse(peopleMap.write()).write())
    print(peopleMap.prettyPrint())
  }

  function testTwo() {
    var example = new Example() {
      :SomeType = new SomeType() {
        :StringEx = "Example",
        :BooleanEx = true,
        :NumberEx = 20.1231,
        :IntEx = 1,
        :TypeInArray = {
          new TypeInArray() {
            :Content = "Example Content"
          }
        },
        :MapEx = { "1232" -> "OneTwoThreeTwo", "111" -> "OneOneOne" },
        :EnumEx = EnumEx.JSON,
        :NestedType = new NestedType() {
          :NestedStringEx = "Nested Example",
          :NestedTypeInArray = {
            new NestedTypeInArray() {
              :Value = "Super Nested",
              :ADate = new java.util.Date()
            }
          },
          :IntArrayEx = { 312 },
          :StringArrayEx = { "Oh Nice", "This", "Is", "An", "Array" },
          :NestedIntEx = 12312,
          :NestedNumberEx = 123.1239141
        }
      }
    }

    print(example.SomeType.NestedType.StringArrayEx.join(" "))
    print(Example.parse(example.write()).write())
    print("")

    var status = new StatusResponse() {
      :InReplyToStatusId = 1232,
      //TODO :User = new org.jschema.examples.twitter.status.StatusResponse.User() why won't this work?
      :User = new User() {
        :Name = "jpcamara",
        :CreatedAt = new java.util.Date(),
        :Url = "http://twitter.com/jpcamara",
        :Id = 12312312,
        :GeoEnabled = true
      }
    }
    print(status.write())
    print("")

    /*
    {
      "status": "string",
      "results": [ {
        "types": [{ "enum" : [ "street_address" ] }],
        "formatted_address": "string",
        "address_components": [{
          "long_name": "string",
          "short_name": "string",
          "types": [{ "enum" : [ "street_number" ] }    }
      } ]
    }
    */
    var geocode = new GeocodeResponse() {
      :Status = "Good",
      :Results = {
        new Results() {
          :Types = {
            Types.STREET_ADDRESS
          },
          :FormattedAddress = "123 Main St, Boulder CO",
          :Geometry = new Geometry() {
            :Location = new Location() {
              :Lat = -123.123123,
              :Lng = 12.12312
            }
          }
        }
      }
    }

    print(GeocodeResponse.parse(geocode.write()).write())

    print(GeocodeResponse.parse(new java.net.URL(
      "http://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=false"))
      .prettyPrint())
  }

  function testGetMethod() {
    print(GeocodeResponse.get( "http://maps.googleapis.com/maps/api/geocode/json",
                               { "address" -> "1600 Amphitheatre Parkway, Mountain View, CA",
                                 "sensor" -> false } )
                         .prettyPrint())
  }

  function testPostMethod() {
    print(GeocodeResponse.post( "http://maps.googleapis.com/maps/api/geocode/json",
                                { "address" -> "1600 Amphitheatre Parkway, Mountain View, CA",
                                  "sensor" -> false } )
                         .prettyPrint())
  }

  function testDescendentsFunction() {
    var peeps = new Peeps() {
      :People = {
        new People() { :Name = "Joe", :Age = 42 },
        new People() { :Name = "Paul", :Age = 28 },
        new People() { :Name = "Mack", :Age = 55 }
      }
    }

    print( peeps.prettyPrint( 2 ) )

    assertEquals(11, peeps.descendents().Count)
    assertEquals(3, peeps.descendents().whereTypeIs(String).Count)
    assertEquals(2, peeps.find(String).where( \ s -> s.length() > 3 ).Count)
    assertEquals(2, peeps.find(People).where( \ p -> p.Age > 30 ).Count )
    assertEquals(3, peeps.find(People).where( \ p -> p.parent().People.Count == 3 ).Count )
  }

  function testAutoCreateWithJSchemaTypes() {
    var x = new SelfTest()
    x.Reference.Reference.Reference.Name = "foo"
    assertEquals( "foo", x.Reference.Reference.Reference.Name )
    assertNull( x.Reference.Reference.Name )
    assertNull( x.Reference.Name )
  }

  function testAutoCreateWithMaps() {
  //TODO cgross - gosu bug, the map syntax doesn't play well with Autocreate
  /*
    assertNull( x.Map?["blah"] )
    x.Map["blah"].Name = "test"
    assertEquals( "test", x.Map["blah"].Name  )
   */
  }

  function testAutoCreateWithLists() {
    var x = new AutoCreateAndInsertTest()
    x.Arr[0].Name = "foo"
    assertEquals( "foo", x.Arr[0].Name )

    var x2 = new AutoCreateAndInsertTest()
    x2.Arr[0].Arr[0].Arr[0].Arr[0].Name = "foo"
    assertEquals( "foo", x2.Arr[0].Arr[0].Arr[0].Arr[0].Name )
  }

  function testConvertToMethod() {
    var x1 =
      new AsExample1() {
        :Name = "foo",
        :Value = "bar",
        :Nested = new AsExample1.Nested() {
          :Name = "foo",
          :Value = "bar"
        },
        :Arr = { new AsExample1.Arr(){ :Name = "bar" } },
        :Map = {
          "foo" -> new AsExample1.Map(){ :Name = "bar" }
        }
      }

    var x2 = x1.convertTo(AsExample2)

    assertEquals("foo", x2.Name)
    assertEquals("bar", x2.Value)

    assertEquals("foo", x2.Nested.Name)
    assertEquals("bar", x2.Nested.Value)

    assertEquals("bar", x2.Arr[0].Name)

    assertEquals("bar", x2.Map["foo"].Name)

    x1.Value = null
    x1.Nested = null
    var x3 = x1.convertTo(AsExample3)
    assertEquals("foo", x3.Name)
    assertNull(x3.Value)
    assertNull(x3.Nested)
  }

  function testConvertToMethodThrowsOnBadMismatch() {
    var x1 =
      new AsExample1() {
        :Name = "foo",
        :Value = "bar",
        :Nested = new AsExample1.Nested() {
          :Name = "foo",
          :Value = "bar"
        },
        :Arr = { new AsExample1.Arr(){ :Name = "bar" } },
        :Map = {
          "foo" -> new AsExample1.Map(){ :Name = "bar" }
        }
      }

    try {
      var x2 = x1.convertTo(AsExample3)
    } catch( e ) {
      print(e.Message)
      assertTrue( e.Message.contains("Value"))
    }
  }

  function testConvertToMethodThrowsOnBadMismatchDeep() {
    var x1 =
      new AsExample1() {
        :Nested = new AsExample1.Nested() {
          :Value = "bar"
        }
      }
    try {
      var x2 = x1.convertTo(AsExample3)
    } catch( e ) {
      print(e.Message)
      assertTrue( e.Message.contains("Nested.Value"))
    }
  }

  function testConvertToMethodWithEnums() {
    var x1 = new AsExample1() { :Enum1 = VAL2 }
    var x2 = x1.convertTo(AsExample3)
    assertEquals(AsExample3.Enum1.VAL2, x2.Enum1)

    try {
      x1.Enum1 = VAL1
      x2 = x1.convertTo(AsExample3)
    } catch( e ) {
      print(e.Message)
      assertTrue( e.Message.contains("Didn't find Enum value 'VAL1' in Enum"))
    }

  }

  function testCircularLoopIsCopiedCorrectly() {
    var x1 = new SelfTest() { :Name = "Single" }
    x1.Reference = x1

    var x2 = x1.convertTo(SelfTest)

    assertEquals( x1, x1.Reference )

    assertEquals( x2, x2.Reference )

    assertFalse( x1 === x2 )

  }

  function testSelfProperties() {
    var slf = new SelfTest() {
      :Name = "Parent",
      :Children = {
        new SelfTest() { :Name = "Child1" },
        new SelfTest() { :Name = "Child2" },
        new SelfTest() { :Name = "Child3",
                         :Children = {
                            new SelfTest() { :Name = "Child31" },
                            new SelfTest() { :Name = "Child32" },
                            new SelfTest() { :Name = "Child33" }
                          }
        }
      }
    }
    assertEquals( "Parent", slf.Name )
    assertEquals( "Child1", slf.Children[0].Name )
    assertEquals( "Child2", slf.Children[1].Name )
    assertEquals( "Child3", slf.Children[2].Name )
    assertEquals( "Child31", slf.Children[2].Children[0].Name )
    assertEquals( "Child32", slf.Children[2].Children[1].Name )
    assertEquals( "Child33", slf.Children[2].Children[2].Name )
    assertNull( slf.Children[0].Children )
  }

  function testAsJsonMap() {
    var slf = new SelfTest() {
      :Name = "Parent",
      :Children = {}
    }
    var slfMap = slf.asJson()
    assertEquals( "Parent", slfMap["name"] )
    assertEquals( {}, slfMap["children"] )
    assertNull( slfMap["reference"] )
  }

  function testURISupport() {
    var test = URITest.parse( '{ "link" : "http://example.com" }' )
    assertEquals(URI, statictypeof test.Link )
    assertEquals(new URI("http://example.com"), test.Link )
  }

}