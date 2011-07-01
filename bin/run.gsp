classpath "../src,../lib,../build/dist,../test"

uses java.lang.*
uses json.eventful.search.SearchResponse
uses json.jpcamara.example.Awesome
uses json.google.geocode.GeocodeResponse
uses json.twitter.status.StatusResponse
uses json.twitter.status.User

var geocode = new GeocodeResponse() {
	:Status = "Rickaroo"
}
var status = new StatusResponse() {
	:User = new User()
}
var awesome = new Awesome()
var search = new SearchResponse()

print(geocode.write())
print(status.write())
print(awesome.write())
print(search.write())

/*var twitter = new json.twitter.status.Response()
twitter.User = new User() {
	:Name = "Someone"
}
twitter.Source = "Web"
twitter.CreatedAt = "Thu Jul 15 23:26:44 +0000 2010"*/
/*print(twitter.write())*/

/*var getType : Response = 
	Response.parse("{'status':'ok', results:[{'formatted_address':'some addr'}]}")*/
/*print(getType.Results.Count)*/
/*

var resp = new Response()
resp.Status = "Success"
resp.Results = {
	new Results() {
		:FormattedAddress = "3984 Jibba Jabba Drive, Sandy CO 93840",
		:Types = { "street_address" },
		:AddressComponents = {
			new AddressComponents() { 
				:LongName = "75",
				:ShortName = "75",
				:Types = { "street_number" }
			}
		},
		:Geometry = new Geometry() {
			:Location = new Location() {
				:Lat = 34.23452,
				:Lng = -34.2352
			},
			:LocationType = "ROOFTOP",
			:Viewport = new Viewport() {
				:Southwest = new Southwest() {
					:Lat = 34.23452,
					:Lng = -34.2352
				},
				:Northeast = new Northeast() {
					:Lat = 34.23452,
					:Lng = -34.2352
				}
			}
		}
	}
}
print(typeof resp)

/*print(resp.write())*/