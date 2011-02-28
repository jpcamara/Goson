classpath "../src,../lib"
typeloader com.jpcamara.gosu.json.JsonTypeLoader

uses json.google.geocode.*
uses json.twitter.status.User
uses java.lang.*

var twitter = new json.twitter.status.Response()
twitter.User = new User() {
	:Name = "Someone"
}
twitter.Source = "Web"
twitter.CreatedAt = "Thu Jul 15 23:26:44 +0000 2010"
print(twitter.write())

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

print(resp.write())