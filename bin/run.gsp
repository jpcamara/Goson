classpath "../src,../lib,../build/dist,../test"
typeloader com.jpcamara.gosu.json.JsonTypeLoader

uses java.lang.*
uses json.eventful.search.SearchResponse
uses json.jpcamara.example.Awesome
uses json.google.geocode.GeocodeResponse
uses json.google.geocode.Results
uses json.twitter.status.StatusResponse
uses json.twitter.status.User
uses java.util.ArrayList
uses java.util.List
uses gw.lang.reflect.java.IJavaType

var geocode = new GeocodeResponse() {
	:Status = "Rickaroo"
}
geocode.Results = new ArrayList<Results>() as List<Results> //have to cast for the in-memory types
geocode.Results.add(new Results() {
  :FormattedAddress = "3984 Jibba Jabba Drive, Sandy CO 93840",
	:Types = { "street_address" } //no casting for existing types...?
})
geocode.Results.add(new Results() {
  :FormattedAddress = "3984 Jibba Jabba Drive, Sandy CO 93840",
	:Types = { "street_address" } //no casting for existing types...?
})
geocode.Results.each(\ result -> print(typeof result))

var status = new StatusResponse() {
	:User = new User() {
	  :Name = "JP"
	},
	:Coordinates = 1,
  :Favorited = false,
  :CreatedAt = "Thu Jul 15 23:26:44 +0000 2010",
  :Truncated = false,
  :Text = "qu por qu kieres saver como poner pablito",
  :Contributors = "nullee",
  /*:Id = 1863948500*/
  :Geo = "12.232 -23.343",
  :InReplyToUserId = 12345
}

/*print(geocode.write())
print(awesome.write())
print(search.write())*/

print(GeocodeResponse.parse(geocode.write()).write())
print(typeof GeocodeResponse.parse(geocode.write()).Results)
gw.internal.gosu.parser.JavaType_Proxy
/*print(typeof gw.lang.reflect.IType.TypeInfo)*/
/*print(IJavaType.LIST.getParameterizedType(IJavaType.INTEGER))//.TypeInfo.getConstructor(null).Constructor.newInstance(null))*/
/*print(typeof geocode)
print(typeof status)
print(typeof awesome)
print(typeof search)

print(StatusResponse.parse(status.write()).write())

print(GeocodeResponse.parse(geocode.write()).write())*/