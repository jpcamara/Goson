classpath "../lib,../build/dist"

uses java.io.*
uses java.util.*
uses com.jpcamara.gosu.json.Schema
uses com.google.gson.*

print(java.lang.System.getProperty('user.dir'))
/*Schema.parse(new java.io.File("../src/example.jsd"))*/
/*JsonElement root = new JsonParser().parse(new FileReader(json));*/

var root = new JsonParser().parse(new FileReader(new File('../src/example.jsd')))
if (!root.JsonObject) {
  print('ok')
  java.lang.System.exit(1)
}

var jsonObj = root.AsJsonObject

function addType(name : String, path : String, o : JsonElement) {
  
}

function searchAndAddTypes(name : String, path : String, o : JsonObject) {

}