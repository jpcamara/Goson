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

function typeStuff(o : Object) {
  print("Name: " + (typeof o).Name)
  print("Display Name: " + (typeof o).DisplayName) 
  print("Relative Name: " + (typeof o).RelativeName)
  print("Namespace: " + (typeof o).Namespace)
  print("TypeLoader: " + (typeof o).TypeLoader)
  print("Supertype: " + (typeof o).Supertype)
  print("EnclosingType: " + (typeof o).EnclosingType)
  print("GenericType: " + (typeof o).GenericType)
  print("Interfaces: " + (typeof o).Interfaces)
  print("GenericTypeVariables: " + Arrays.toString((typeof o).GenericTypeVariables))
  print("TypeParameters: " + Arrays.toString((typeof o).TypeParameters))
  print("AllTypesInHierarchy: " + (typeof o).AllTypesInHierarchy)
  print("ArrayType: " + (typeof o).ArrayType)
  print("ComponentType: " + (typeof o).ComponentType)
  print("TypeInfo: " + (typeof o).TypeInfo)
  print("Modifiers: " + (typeof o).Modifiers)
  print("CompoundTypeComponents: " + (typeof o).CompoundTypeComponents)
}

function typeInfoStuff(o : Object) {
  print("Properties: " + (typeof o).TypeInfo.Properties)
  print("Methods: " + (typeof o).TypeInfo.Methods) 
  print("Constructors: " + (typeof o).TypeInfo.Constructors)
  print("Events: " + (typeof o).TypeInfo.Events)
  print("Annotations: " + (typeof o).TypeInfo.Annotations)
  print("DeclaredAnnotations: " + (typeof o).TypeInfo.DeclaredAnnotations)
  print("Container: " + (typeof o).TypeInfo.Container)
  print("OwnersType: " + (typeof o).TypeInfo.OwnersType)
  print("Name: " + (typeof o).TypeInfo.Name)
  print("DisplayName: " + (typeof o).TypeInfo.DisplayName)
  print("Description: " + (typeof o).TypeInfo.Description)
}

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
  :Mappy = { 1 -> "nice" }
}
print((typeof awe.SomeObject).TypeInfo.Properties.where(\ p -> p.Name == "Mappy").FeatureType)
print(Awesome.parse(awe.write()).write())
/*awe.SomeObject.SomeDeeperObject.EvenDeeper.each(\ ed -> print(ed.Nice))*/
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
/*print(typeof even)*/
/*print("Feature Type: " + java.util.Arrays.toString((typeof awe.SomeObject.SomeDeeperObject).TypeInfo.Properties.where(\ p -> p.Name == "EvenDeeper").FeatureType))*/
//Feature Type: [java.util.ArrayList<json.jpcamara.example.EvenDeeper>]
/*awe.SomeObject.SomeDeeperObject.EvenDeeper.each(\ p -> print(p.Nice))*/
/*for (ed in awe.SomeObject.SomeDeeperObject.EvenDeeper index i) {
  var a : EvenDeeper = awe.SomeObject.SomeDeeperObject.EvenDeeper[i]
  print(a.Nice)
  print(ed)
}*/
/*var edArr = new EvenDeeper[1]
edArr[0] = new EvenDeeper() { :Nice = "Sweet" }
(edArr).each(\ e -> print(e.Nice))*/
/*print(typeof edArr)*/
/*var edList : List<EvenDeeper> = {
  new EvenDeeper() { :Nice = "Sweet" }
}*/
/*({ new EvenDeeper() { :Nice = "Sweet" } }).each(\ e -> print("inside json list iteration: " + Arrays.toString((typeof e).TypeParameters)))*/
/*({ new EvenDeeper() { :Nice = "Sweet" } }).each(\ e -> print(e.Nice))*/
/*({ awe.SomeObject.SomeDeeperObject.EvenDeeper.first() }).each(\ e -> print("inside json list iteration: " + Arrays.toString((typeof e).TypeParameters)))*/

/*var x = new myapp.example.Person()
x.Age = { 32, 9298, 2323 }
x.Firstname = "Joe"
x.Dude = {
  new myapp.example.anonymous.elements.Person_Dude() {
    :Typey = "Male"
  }
}
var xsdThingy = { 
  new myapp.example.anonymous.elements.Person_Dude() {
    :Typey = "Male"
  }
}
print("")
print("")
print("")
print("")
print("")
x.Dude.each(\ dude -> typeInfoStuff(dude))
print("")
print("")
print("")
print("")
print("")
({ new EvenDeeper() { :Nice = "Sweet" } }).each(\ e -> typeInfoStuff(e))
print("")
print("")
print("")
print("")
print("")*/