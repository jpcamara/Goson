classpath "../build/dist,../src"
typeloader com.jpcamara.goson.JsonTypeLoader

uses java.lang.*
uses java.util.ArrayList
uses java.util.List
uses java.util.Arrays
uses jschema.fullexample.Example
uses jschema.fullexample.SomeType
uses jschema.fullexample.TypeInArray
uses jschema.fullexample.EnumEx
uses jschema.fullexample.NestedType
uses jschema.fullexample.NestedTypeInArray

uses jschema.twitter.status.StatusResponse
uses jschema.google.geocode.GeocodeResponse

var example = new Example() {
  :SomeType = new SomeType() {
    :BigIntEx = 12314235134,
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
    :MapEx = { 1232 -> "OneTwoThreeTwo", 111 -> "OneOneOne" },
    :EnumEx = EnumEx.JSON,
    :NestedType = new NestedType() {
      :NestedStringEx = "Nested Example",
      :NestedTypeInArray = {
        new NestedTypeInArray() {
          :Value = "Super Nested",
          :ADate = new java.util.Date()
        }
      },
      :BigIntArrayEx = { 123112311 },
      :StringArrayEx = { "Oh Nice", "This", "Is", "An", "Array" },
      :NestedBigIntEx = 2312314,
      :NestedBigDecimalEx = 123.1239141
    }
  }
}

print(example.SomeType.NestedType.StringArrayEx.join(" "))
print(Example.parse(example.write()).write())

var status = new StatusResponse() {
  :InReplyToStatusId = 24134134,
  :User = new jschema.twitter.status.User() {
    :Name = "jpcamara",
    :CreatedAt = new java.util.Date(),
    :Url = "http://twitter.com/jpcamara",
    :Id = 12312312,
    :GeoEnabled = true
  }
}
print(status.write())

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
    new jschema.google.geocode.Results() {
      :Types = {
        jschema.google.geocode.Types.STREET_ADDRESS
      },
      :FormattedAddress = "123 Main St, Boulder CO",
      :Geometry = new jschema.google.geocode.Geometry() {
        :Location = new jschema.google.geocode.Location() {
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