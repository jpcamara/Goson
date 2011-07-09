classpath "../src,../lib,../build/dist,../test"
typeloader com.jpcamara.goson.JsonTypeLoader

uses java.lang.*
uses java.util.ArrayList
uses java.util.List
uses json.jpcamara.example.Awesome
uses json.jpcamara.example.SomeObject
uses json.jpcamara.example.SomeDeeperObject
uses json.jpcamara.example.EvenDeeper

var awe = new Awesome()
awe.SomeObject = new SomeObject() {
  :FirstField = 1,
  :SecondField = "nice",
  :Booly = true,
  :Doubly = 10.12081989872,
  :SomeDeeperObject = new SomeDeeperObject() {
    :ThirdField = "Ok",
    :Other = { 1 },
    :MostOtherest = { "nice" },
    :EvenDeeper = new ArrayList<EvenDeeper>() as List<EvenDeeper>
  }
}
awe.SomeObject.SomeDeeperObject.EvenDeeper.add(new EvenDeeper() {
  :Nice = "Rock",
  :Now = new java.util.Date()
})
awe.SomeObject.SomeDeeperObject.EvenDeeper.add(new EvenDeeper() {
  :Nice = "Roll",
  :Now = new java.util.Date()
})

/**
*  "first_field": "integer",
  "second_field": "string",
  "booly": "boolean",
  "doubly": "decimal",
  "some_deeper_object": {
    "third_field": "string",
    "even_deeper": [{
      "nice": "string",
      "now": "date"
    }],
    "a_map": { "map_of" : "integer" }, <-- looks like a JSON Object, but it's a map
    "types": { "enum" : ["json", "txt", "xml", "jsd", "wtf"] }, <-- looks like a json object, but it's an enum
    "other": ["integer"],
    "most_otherest": ["string"]
  }
}
*/