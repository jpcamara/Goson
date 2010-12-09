package com.jpcamara.gosu.json;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.jpcamara.gosu.json.Json.JSONParserException;

public class TestJson {

	private Json json;
	private String jsonString;
	private String[] keys = { "page_size", "total_items", "events",
			"page_items", "first_item", "page_number", "search_time",
			"last_item", "page_count" };
	
	private static final String OBJ_KEY = "events";
	private static final String PAGE_EXPECT = "20";

	@Before
	public void setup() throws Exception {
		StringBuilder j = new StringBuilder();
		Scanner s = new Scanner(new File(
				"./src/com/jpcamara/gosu/json/eventful.search.Response"));
		while (s.hasNext()) {
			j.append(s.nextLine());
		}
		s.close();
		jsonString = j.toString();
		json = new Json(jsonString);
	}

	@Test
	public void defaultConstructor() throws Exception {
		json = new Json();
	}

	@Test
	public void stringConstructor() throws Exception {
		json = new Json(jsonString);
	}

	@Test
	public void jsonObjectConstructor() throws Exception {
		json = new Json(new JSONObject(jsonString));
	}

	@Test
	public void keysMatchJson() throws Exception {
		int expected = keys.length;
		int actual = 0;
		for (String key : json.keys()) {
			assertEquals(keys[actual], key);
			actual++;
		}
		assertEquals(expected, actual);
	}

	@Test
	public void getValue() throws Exception {
		assertTrue(json.get(OBJ_KEY) instanceof JSONObject);
		assertEquals(PAGE_EXPECT, json.get("page_size"));
	}
	
	@Test
	public void jsonFromKey() throws Exception {
		assertTrue(json.getJson(OBJ_KEY) instanceof Json);
		assertEquals("event", json.getJson(OBJ_KEY).keys().iterator().next());
	}
	
	@Test
	public void typeNames() throws Exception {
		Set<String> expected = new HashSet<String>();
		expected.add("events");
		expected.add("event");
		expected.add("going");
		expected.add("user");
		Set<String> actual = json.getAllTypeNames();
		
		assertEquals(expected.size(), actual.size());
		for (String expectedType : expected) {
			assertTrue(actual.contains(expectedType));
		}
	}
	
	@Test
	public void putValue() throws Exception {
		String expected = "here it is";
		json.put("some_value", expected);
		assertEquals(expected, json.get("some_value"));
	}
	
	@Test
	public void typeChecks() throws Exception {
		assertTrue(Json.isJSONObject(json.get("events")));
		assertTrue(Json.isJSONArray(json.getJson("events").get("event")));
		assertTrue(Json.isJSONNull(json.get("total_items")));
		
		assertFalse(Json.isJSONObject(json.getJson("events").get("event")));
		assertFalse(Json.isJSONArray(json.get("events")));
		assertFalse(Json.isJSONNull(json.get("last_item")));
	}
	
	@Test
	public void getItemByIndex() throws Exception {
		assertTrue(json.getJson("events").getWithIndex("event", 0) instanceof JSONObject);
	}
	
	@Test
	public void returnsNullWhenNoKeyIsAvailable() throws Exception {
		assertEquals(null, json.get("doesnt_exist"));
	}
	
	@Test
	public void toStringReturnsContents() throws Exception {
		assertEquals(
		"[page_size,total_items,events,page_items,first_item," +
		"page_number,search_time,last_item,page_count,]", json.toString());
	}
	
	@Test
	public void serializesToJsonString() throws Exception {
		assertEquals("{}", json.serialize());
	}
	
	@Test(expected = JSONParserException.class)
	public void failsConstructionOnBogusJson() throws Exception {
		new Json("dun dun dunnnnn");
	}
	
	@Test(expected = JSONParserException.class)
	public void failsConstructionOnNonJSONObject() throws Exception {
		new Json(new Json());
	}
	
	@Test(expected = JSONParserException.class)
	public void failsWhenAccessedByIndexOnNonArray() throws Exception {
		json.getWithIndex("events", 0);
	}
	
	@Test(expected = JSONParserException.class)
	public void failsWhenGettingJsonForNonJsonObject() throws Exception {
		json.getJson("last_item");
	}
	
	@Test(expected = JSONParserException.class)
	public void failsOnPutWithABadKey() throws Exception {
		json.put(null,"uh-oh");
	}
}