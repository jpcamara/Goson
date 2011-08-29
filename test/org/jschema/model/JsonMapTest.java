package org.jschema.model;

import junit.framework.TestCase;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;

public class JsonMapTest extends TestCase {
  
  public void testSanity() {
    JsonMap map = new JsonMap();

    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
    assertFalse(map.containsKey("foo"));
    assertFalse(map.containsValue("bar"));
    assertNull(map.get("foo"));

    map.put("foo", "bar");

    assertEquals(1, map.size());
    assertFalse(map.isEmpty());
    assertTrue(map.containsKey("foo"));
    assertTrue(map.containsValue("bar"));
    assertEquals("bar", map.get("foo"));

    map.clear();

    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
    assertFalse(map.containsKey("foo"));
    assertFalse(map.containsValue("bar"));
    assertNull(map.get("foo"));

    map.put("foo", "bar");

    assertEquals(1, map.size());
    assertFalse(map.isEmpty());
    assertTrue(map.containsKey("foo"));
    assertTrue(map.containsValue("bar"));
    assertEquals("bar", map.get("foo"));

    map.remove("foo");

    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
    assertFalse(map.containsKey("foo"));
    assertFalse(map.containsValue("bar"));
    assertNull(map.get("foo"));

  }
  
  public void testPutUpdatesParentProperly() {
    JsonMap parent = new JsonMap();

    JsonMap child1 = new JsonMap();
    JsonMap child2 = new JsonMap();
    JsonMap child3 = new JsonMap();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.put("foo", child1);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.put("foo", child1);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.put("foo", child2);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertNull(child3.getParent());

    parent.put("bar", child3);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.put("baz", parent);

    assertEquals(parent, parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());
  }

  public void testRemoveUpdatesParentProperly() {
    JsonMap parent = new JsonMap();

    JsonMap child1 = new JsonMap();
    JsonMap child2 = new JsonMap();
    JsonMap child3 = new JsonMap();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.put("foo", child1);
    parent.put("bar", child2);
    parent.put("baz", child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove("falalalala");

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove("foo");

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove("foo");

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove(null);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());
  }

  public void testPutAllUpdatesParentProperly() {

    JsonMap parent = new JsonMap();

    JsonMap parent2 = new JsonMap();

    JsonMap child1 = new JsonMap();
    JsonMap child2 = new JsonMap();
    JsonMap child3 = new JsonMap();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    HashMap<String, Object> otherMap = new HashMap<String, Object>();
    otherMap.put("foo", child1);
    otherMap.put("bar", child2);
    otherMap.put("baz", child3);

    parent.putAll(otherMap);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.putAll(parent);

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent2, child2.getParent());
    assertEquals(parent2, child3.getParent());

    assertNotNull(parent.remove("foo"));

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent2, child2.getParent());
    assertEquals(parent2, child3.getParent());
  }

  public void testClearManagesParentPointersCorrectly() {

    JsonMap parent = new JsonMap();

    JsonMap parent2 = new JsonMap();

    JsonMap child1 = new JsonMap();
    JsonMap child2 = new JsonMap();
    JsonMap child3 = new JsonMap();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.put("foo", child1);
    parent.put("bar", child2);
    parent.put("baz", child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.clear();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.put("foo", child1);
    parent.put("bar", child2);
    parent.put("baz", child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.put("foo", child1);
    parent2.put("bar", child2);
    parent2.put("baz", child3);

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent2, child2.getParent());
    assertEquals(parent2, child3.getParent());

    assertNotNull(parent.get("foo"));
    assertNotNull(parent.get("bar"));
    assertNotNull(parent.get("baz"));

    parent.clear();

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent2, child2.getParent());
    assertEquals(parent2, child3.getParent());
  }

  public void testEqualsWorks() {

    JsonMap parent = new JsonMap();

    JsonMap parent2 = new JsonMap();

    JsonMap child1 = new JsonMap();
    JsonMap child2 = new JsonMap();
    JsonMap child3 = new JsonMap();

    parent.put("foo", child1);
    parent.put("bar", child2);
    parent.put("baz", child3);

    parent2.put("foo", child1);
    parent2.put("bar", child2);
    parent2.put("baz", child3);

    assertEquals(parent,  parent2);
    parent.clear();
    assertFalse(parent.equals(parent2));
    parent2.clear();
    assertEquals(parent,  parent2);
  }

  public void testKeySetManagesParentCorrectly() {

    JsonMap parent = new JsonMap();

    JsonMap child1 = new JsonMap();
    JsonMap child2 = new JsonMap();
    JsonMap child3 = new JsonMap();

    parent.put("foo", child1);
    parent.put("bar", child2);
    parent.put("baz", child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    assertTrue(parent.keySet().remove("foo"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    Iterator<String> iterator = parent.keySet().iterator();
    assertTrue(iterator.hasNext());
    iterator.next();
    iterator.remove();
    assertTrue(iterator.hasNext());
    iterator.next();
    iterator.remove();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());
  }

  public void testValuesManagesParentCorrectly() {

    JsonMap parent = new JsonMap();

    JsonMap child1 = new JsonMap();
    JsonMap child2 = new JsonMap();
    JsonMap child3 = new JsonMap();

    parent.put("foo", child1);
    parent.put("bar", child2);
    parent.put("baz", child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    assertTrue(parent.values().remove(child1));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    Iterator<Object> iterator = parent.values().iterator();
    assertTrue(iterator.hasNext());
    iterator.next();
    iterator.remove();
    assertTrue(iterator.hasNext());
    iterator.next();
    iterator.remove();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());
  }

  public void testEntrySetManagesParentCorrectly() {

    JsonMap parent = new JsonMap();

    JsonMap child1 = new JsonMap();
    JsonMap child2 = new JsonMap();
    JsonMap child3 = new JsonMap();

    parent.put("foo", child1);
    parent.put("bar", child2);
    parent.put("baz", child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    assertTrue(parent.entrySet().remove(new AbstractMap.SimpleEntry<String, Object>("foo", child1)));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    Iterator iterator = parent.entrySet().iterator();
    assertTrue(iterator.hasNext());
    iterator.next();
    iterator.remove();
    assertTrue(iterator.hasNext());
    iterator.next();
    iterator.remove();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());
  }

}
