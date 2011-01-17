package com.jpcamara.gosu.json;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestJsonName {
	@Test
	public void handleUnderscores() throws Exception {
		assertEquals("some_name_with_underscores", new JsonName("some_name_with_underscores").getJsonName());
		assertEquals("SomeNameWithUnderscores", new JsonName("some_name_with_underscores").getName());
	}
}
