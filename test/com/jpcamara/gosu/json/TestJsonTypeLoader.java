//package com.jpcamara.gosu.json;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import gw.lang.reflect.IType;
//import gw.util.GosuStringUtil;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.junit.Before;
//import org.junit.Test;
//
//public class TestJsonTypeLoader {
//	
//	private JsonTypeLoader typeLoader;
//	private Set<String> expectedNs = new HashSet<String>();
//	{
//		expectedNs.add("json");
//		expectedNs.add("json.eventful");
//		expectedNs.add("json.eventful.search");
//		expectedNs.add("json.jpcamara");
//		expectedNs.add("json.jpcamara.example");
//		expectedNs.add("json.twitter");
//		expectedNs.add("json.twitter.status");
//	}
//	
//	@Before
//	public void setup() throws Exception {
//		typeLoader = new JsonTypeLoader( new MockModule());
//	}
//
//	@Test
//	public void constructors() throws Exception {
//		new JsonTypeLoader();
//		new JsonTypeLoader(new MockModule());
//	}
//	
//	@Test
//	public void checkNamespaces() throws Exception {		
//		Set<String> actualNs = typeLoader.getAllNamespaces();
//		System.out.println(actualNs);
//		assertEquals(expectedNs.size(), actualNs.size());
//		for (String ns : actualNs) {
//			assertTrue(expectedNs.contains(ns));
//		}
//	}
//	
//	@Test
//	public void checkHandledPrefixes() throws Exception {
//		List<String> actualPrefix = typeLoader.getHandledPrefixes();
//		assertEquals(expectedNs.size(), actualPrefix.size());
//		for (String ns : actualPrefix) {
//			assertTrue(expectedNs.contains(ns));
//		}
//	}
//	
//	@Test
//	public void typesFound() throws Exception {
//		Set<String> typeNames = new HashSet<String>();
//		typeNames.add("json.eventful.search.Going");
//		typeNames.add("json.eventful.search.Response");
//		typeNames.add("json.jpcamara.example.SomeDeeperObject"); 
//		typeNames.add("json.jpcamara.example.SomeObject");
//		typeNames.add("json.eventful.search.Event");
//		typeNames.add("json.twitter.status.Response"); 
//		typeNames.add("json.jpcamara.example.EvenDeeper");
//		typeNames.add("json.eventful.search.Events");
//		typeNames.add("json.twitter.status.User");
//		typeNames.add("json.jpcamara.example.Awesome");
//		typeNames.add("json.eventful.search.User");
//		System.out.println(typeLoader.getAllTypeNames());
//		assertEquals(typeNames.size(), typeLoader.getAllTypeNames().size());
//		for (String typeName : typeNames) {
//			assertTrue(typeLoader.getAllTypeNames().contains(typeName));
//		}
//	}
//	
//	@Test
//	public void getType() throws Exception {
//		assertTrue(typeLoader.getType("json.eventful.search.User") instanceof IType);
//	}
//}
