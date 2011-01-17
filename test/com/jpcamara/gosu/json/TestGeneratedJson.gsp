classpath ".."
//typeloader com.jpcamara.gosu.json.JsonTypeLoader

uses java.lang.Integer
uses json.eventful.search.*

var resp = new Response()
resp.Events = new Events()
resp.Events.Event = new Event[] {
  new Event() {
    :AllDay = "sure",
    :CountryName = "USA"
  }
}
resp.PageNumber = "1"