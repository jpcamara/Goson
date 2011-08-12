classpath "../build/dist,../src"
typeloader com.jpcamara.goson.JsonTypeLoader

uses java.lang.*
uses java.util.ArrayList
uses java.util.List
uses java.util.Arrays

/*uses json.simplejson.IdToPeople*/
uses simplejson.NameAndAge

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
var people = new simplejson.people1.Peeps() {
  :People = {
    new simplejson.people1.People() { :Name = "Joe", :Age = 42 },
    new simplejson.people1.People() { :Name = "Paul", :Age = 28 },
    new simplejson.people1.People() { :Name = "Mack", :Age = 55 }
  }
}
print(simplejson.people1.Peeps.parse(people.write()).write())
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
/*var people2 = json.simplejson.people2.Peeps() {
  
}*/

/*
{ "id_to_people" : {
    "map_of" : {
    "key" : "biginteger",
    "value" : { "name" : "string", 
                "age" : "integer",
                "eye_color" : {"enum" : ["brown", 
                                         "blue", 
                                         "green"]} }
    }
  }
}
*/
/*var peopleMap = new IdToPeople() {
  
}*/