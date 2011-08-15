classpath "../build/dist,../src"
typeloader com.jpcamara.goson.JsonTypeLoader

uses jschema.PeopleId
uses jschema.NameAndAge

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
var people = new jschema.people1.Peeps() {
  :People = {
    new jschema.people1.People() { :Name = "Joe", :Age = 42 },
    new jschema.people1.People() { :Name = "Paul", :Age = 28 },
    new jschema.people1.People() { :Name = "Mack", :Age = 55 }
  }
}
print(jschema.people1.Peeps.parse(people.write()).write())

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
var people2 = new jschema.people2.Peeps() {
  :People = {
    new jschema.people2.People() { :Name = "Joe", :Age = 42, :EyeColor = jschema.people2.EyeColor.BROWN },
    new jschema.people2.People() { :Name = "Paul", :Age = 28, :EyeColor = jschema.people2.EyeColor.BROWN },
    new jschema.people2.People() { :Name = "Mack", :Age = 55, :EyeColor = jschema.people2.EyeColor.BLUE }
  }
}
print(jschema.people2.Peeps.parse(people2.write()).write())

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
    1 -> new jschema.IdToPeople() { :Name = "Joe", :Age = 42, :EyeColor = jschema.EyeColor.BROWN },
    2 -> new jschema.IdToPeople() { :Name = "Paul", :Age = 28, :EyeColor = jschema.EyeColor.BROWN },
    3 -> new jschema.IdToPeople() { :Name = "Mack", :Age = 55, :EyeColor = jschema.EyeColor.BLUE }
  }
}
print(PeopleId.parse(peopleMap.write()).write())
print(peopleMap.prettyPrint())