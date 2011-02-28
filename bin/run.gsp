classpath "../src,../lib"
typeloader com.jpcamara.gosu.json.JsonTypeLoader

uses json.google.geocode.*
uses java.lang.*

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