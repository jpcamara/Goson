classpath "../build/dist,../src"
typeloader com.jpcamara.goson.JsonTypeLoader

uses java.lang.*
uses java.util.ArrayList
uses java.util.List
uses java.util.Arrays
uses json.jpcamara.example.Example
uses json.jpcamara.example.SomeType
uses json.jpcamara.example.TypeInArray
uses json.jpcamara.example.EnumEx
uses json.jpcamara.example.NestedType
uses json.jpcamara.example.NestedTypeInArray
uses com.EnumExample

uses json.twitter.status.StatusResponse
uses json.google.geocode.GeocodeResponse

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
  :User = new json.twitter.status.User() {
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
    new json.google.geocode.Results() {
      :Types = {
        json.google.geocode.Types.STREET_ADDRESS
      },
      :FormattedAddress = "123 Main St, Boulder CO",
      :Geometry = new json.google.geocode.Geometry() {
        :Location = new json.google.geocode.Location() {
          :Lat = -123.123123,
          :Lng = 12.12312
        }
      }
    }
  }
}
print(GeocodeResponse.parse(geocode.write()).write())