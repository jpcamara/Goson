package com.jpcamara.gosu.json;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import java.io.*;

public class Schema {
  public static void parse(File json) throws Exception {
    JsonElement root = new JsonParser().parse(new FileReader(json));
  }
}

/*
{
  "some_object": {
    "first_field": "integer",
    "second_field": "string",
    "booly": "boolean",
    "doubly": "decimal",
    "some_deeper_object": {
      "third_field": "string",
      "even_deeper": [{
        "nice": "string",
        "now": "date"
      }],
      "a_map": { "map_of" : "integer" },
      "types": { "enum" : ["json", "txt", "xml", "jsd", "wtf"]}
      "other": ["integer"],
      "most_otherest": ["string"]
    }
  }
}

*/