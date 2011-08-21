classpath "../build/dist,../test"

uses org.jschema.examples.PeopleId
uses org.jschema.examples.PeopleId.IdToPeople
uses org.jschema.examples.PeopleId.EyeColor
uses org.jschema.examples.NameAndAge

uses org.jschema.examples.people1.Peeps
uses org.jschema.examples.people1.Peeps.People

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