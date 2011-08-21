classpath "../build/dist,../src"
typeloader org.jschema.typeloader.JsonTypeLoader

uses java.lang.*
uses java.util.ArrayList
uses java.util.List
uses java.util.Arrays
uses jschema.fullexample.Example
uses jschema.fullexample.Example.SomeType
uses jschema.fullexample.Example.SomeType.TypeInArray
uses jschema.fullexample.Example.SomeType.EnumEx
uses jschema.fullexample.Example.SomeType.NestedType
uses jschema.fullexample.Example.SomeType.NestedType.NestedTypeInArray

uses jschema.twitter.status.StatusResponse
uses jschema.twitter.status.StatusResponse.User

uses jschema.google.geocode.GeocodeResponse
uses jschema.google.geocode.GeocodeResponse.Results
uses jschema.google.geocode.GeocodeResponse.Results.Types
uses jschema.google.geocode.GeocodeResponse.Results.Geometry
uses jschema.google.geocode.GeocodeResponse.Results.Geometry.Location

var example = new Example() {
  :SomeType = new SomeType() {
    :BigIntEx = 12312,
    :StringEx = "Example",
    :BooleanEx = true,
    :BigDecimalEx = 20.1231,
    :IntEx = 1,
    :DecimalEx = 1.2123,
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
      :BigIntArrayEx = { 312 },
      :StringArrayEx = { "Oh Nice", "This", "Is", "An", "Array" },
      :NestedBigIntEx = 12312,
      :NestedBigDecimalEx = 123.1239141
    }
  }
}

print(example.SomeType.NestedType.StringArrayEx.join(" "))
print(Example.parse(example.write()).write())
print("")

var status = new StatusResponse() {
  :InReplyToStatusId = 1232,
  //TODO :User = new jschema.twitter.status.StatusResponse.User() why won't this work?
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