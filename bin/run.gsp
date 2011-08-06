classpath "../build/dist,../src"
typeloader com.jpcamara.goson.JsonTypeLoader

uses java.lang.*
uses java.util.ArrayList
uses java.util.List
uses java.util.Arrays
uses json.jpcamara.example.Awesome
uses json.jpcamara.example.SomeObject
uses json.jpcamara.example.SomeDeeperObject
uses json.jpcamara.example.EvenDeeper
uses json.jpcamara.example.Yo

var awe = new Awesome()
var even = new ArrayList<EvenDeeper>()
even.add(new EvenDeeper() {
  :Nice = "Rock",
  :Now = new java.util.Date()
})
even.add(new EvenDeeper() {
  :Nice = "Roll",
  :Now = new java.util.Date()
})
awe.SomeObject = new SomeObject() {
  :FirstField = 1,
  :SecondField = "nice",
  :Booly = true,
  :Decimal = 10.12081989872,
  :Inty = 2,
  :Doubly = 2.1,
  :Yo = {
    new Yo() {
      :Word = "nice"
    }
  },
  :SomeDeeperObject = new SomeDeeperObject() {
    :ThirdField = "Ok",
    :Other = { 1 },
    :MostOtherest = { "nice" },
    :EvenDeeper = even
  },
  :Mappy = { 1 -> "nice", 12412 -> "AWESOME" }
}
print((typeof awe.SomeObject).TypeInfo.Properties.where(\ p -> p.Name == "Mappy").FeatureType)
print(Awesome.parse(awe.write()).write())
/*print(awe.write())*/
awe.SomeObject.SomeDeeperObject.EvenDeeper.each(\ ed -> print(ed.Nice))
/*'{"some_object": {' +
'  "inty": 2,' +
'  "booly": true,' +
'  "second_field": "nice",' +
'  "first_field": 1,' +
'  "some_deeper_object": {' +
'    "other": [1],' +
'    "third_field": "Ok",' +
'    "most_otherest": ["nice"],' +
'    "even_deeper": [' +
'      {' +
'        "now": "Thu Jul 21 23:14:13 EDT 2011",' +
'        "nice": "Rock"' +
'      },' +
'      {' +
'        "now": "Thu Jul 21 23:14:13 EDT 2011",' +
'        "nice": "Roll"' +
'      }' +
'    ]' +
'  },' +
'  "yo": [{"word": "nice"}],' +
'  "doubly": 2.1,' +
'  "decimal": 10.12081989872' +
'}}'*/