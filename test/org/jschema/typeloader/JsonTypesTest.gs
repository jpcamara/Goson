package org.jschema.typeloader

uses java.util.*
uses java.lang.*
uses java.net.*

uses org.jschema.test.*

uses org.jschema.examples.flickr.GalleriesList
uses org.jschema.examples.flickr.GalleriesList.Galleries
uses org.jschema.examples.RegularJson

class JsonTypesTest extends GosonTest {

  function testFlickr() {
    var flickr = new GalleriesList() {
      :Galleries = {
        new Galleries() {
          :Id = "5704-72157622637971865",
          :Url = new URI("http://www.flickr.com/photos/george/galleries/72157622637971865"),
          :Owner = "34427469121@N01",
          :DateCreate = 1,
          :DateUpdate = 1,
          :PrimaryPhotoId = "",
          :PrimaryPhotoServer = 39,
          :PrimaryPhotoFarm = 1,
          :PrimaryPhotoSecret = "ffa",
          :CountPhotos = 16,
          :CountVideos = 2,
          :Title = "I like me some black & white",
          :Description = "black and whites"
        }
      },
      :Total = 9,
      :Page = 1,
      :Pages = 1,
      :PerPage = 100,
      :UserId = "34427469121@N01"
    }
      /*
      {
    "galleries" : [{
      "id" : "5704-72157622637971865",
      "url" : "http://www.flickr.com/photos/george/galleries/72157622637971865",
      "owner" : "34427469121@N01",
      "date_create" : 1257711422,
      "date_update" : 1260360756,
      "primary_photo_id" : "107391222",
      "primary_photo_server" : 39,
      "primary_photo_farm" : 1,
      "primary_photo_secret" : "ffa",
      "count_photos" : 16,
      "count_videos" : 2,
      "title" : "I like me some black & white",
      "description" : "black and whites"
    }],
    "total" : 9,
    "page" : 1,
    "pages" : 1,
    "per_page" : 100,
    "user_id" : "34427469121@N01"
}
    */
  }

  function testSimpleJson() {
    var regular = new RegularJson()/* {
      :BigIntEx = 123123,
      :StringEx = "Oh word",
      :BooleanEx = true
    }
    /*
      {
  "some_type": {
    "big_int_ex": 12312312,
    "string_ex": "content",
    "boolean_ex": "true",
    "type_in_array": [{
      "content": "some content"
    }],
    "nested_type": {
      "nested_string_ex": "nested",
      "nested_type_in_array": [{
        "value": "some value",
      }],
      "big_int_array_ex": [2313123],
      "string_array_ex": ["array content"],
      "nested_big_int_ex" : 123123,
      "nested_big_decimal_ex" : 1232.2321
    }
  }
}
    */
  }

}