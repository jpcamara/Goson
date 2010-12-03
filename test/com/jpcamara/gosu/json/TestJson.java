package com.jpcamara.gosu.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

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
}