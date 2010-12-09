package com.jpcamara.gosu.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gw.lang.reflect.DefaultArrayType;
import gw.lang.reflect.java.IJavaType;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestJsonType {
	
	private JsonType type;
	private JsonTypeLoader loader = new JsonTypeLoader();
	
	@Before
	public void setup() throws Exception {
		type = new JsonType("SomeName", 
				"jpcamara.test", loader, new Json());
	}

	@Test
	public void constructor() throws Exception {
		type = new JsonType("some_namer", 
				"jpcamara.sweet", new JsonTypeLoader(), new Json());
	}
	
	@Test
	public void name() throws Exception {
		assertEquals("jpcamara.test.SomeName", type.getName());
	}
	
	@Test
	public void namespace() throws Exception {
		assertEquals("jpcamara.test", type.getNamespace());
	}
	
	@Test
	public void relativeName() throws Exception {
		assertEquals("SomeName", type.getRelativeName());
	}
	
	@Test
	public void jsonRelativeName() throws Exception {
		assertEquals("some_name", type.getJsonRelativeName());
	}
	
	@Test
	public void typeInfo() throws Exception {
		assertTrue(type.getTypeInfo() instanceof JsonTypeInfo);
	}
	
	@Test
	public void typeloader() throws Exception {
		assertEquals(loader, type.getTypeLoader());
	}
	
	@Test
	public void interfaces() throws Exception {
		assertEquals(Collections.emptyList(), type.getInterfaces());
	}
	
	@Test
	public void supertype() throws Exception {
		assertEquals(IJavaType.OBJECT, type.getSupertype());
	}
	
	@Test
	public void arrayType() throws Exception {
		assertTrue(type.getArrayType() instanceof DefaultArrayType);
	}
	
	@Test
	public void makeArrayInstance() throws Exception {
		Assert.assertArrayEquals((Json[])type.makeArrayInstance(1), new Json[1]);
	}
	
	@Test
	public void tostring() throws Exception {
		assertEquals("jpcamara.test.SomeName []", type.toString());
	}
}
