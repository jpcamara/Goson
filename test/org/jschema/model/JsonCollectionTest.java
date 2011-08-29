package org.jschema.model;

import junit.framework.TestCase;

import java.util.*;

public class JsonCollectionTest extends TestCase {

  public void testSanity() {
    JsonCollection col = makeCol("foo", "bar", "baz");

    assertEquals(3, col.size());
    assertFalse(col.isEmpty());
    assertTrue(col.contains("foo"));
    assertFalse(col.contains("falalalala"));
    assertEquals(new ArrayList(col), Arrays.asList(col.toArray()));
    assertEquals(new ArrayList(col), Arrays.asList(col.toArray(new String[0])));

    assertTrue(col.add("blah"));
    assertEquals(4, col.size());

    assertTrue(col.remove("blah"));
    assertEquals(3, col.size());

    assertTrue(col.containsAll(Arrays.asList("foo", "bar")));
    assertFalse(col.containsAll(Arrays.asList("foo", "bar", "blah")));

    assertTrue(col.addAll(Arrays.asList("go", "bears")));
    assertEquals(5, col.size());

    assertTrue(col.removeAll(Arrays.asList("go", "bears")));
    assertEquals(3, col.size());

    assertTrue(col.retainAll(Arrays.asList("foo", "bears")));
    assertEquals(1, col.size());

    col.clear();
    assertEquals(0, col.size());
  }

  public void testIteratorManagesParentCorrectly() {
    JsonCollection parent = new JsonCollection();
    JsonCollection child1 = new JsonCollection(null, Arrays.<Object>asList("a"));
    JsonCollection child2 = new JsonCollection(null, Arrays.<Object>asList("b"));
    JsonCollection child3 = new JsonCollection(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.addAll(Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    Iterator<Object> iterator = parent.iterator();
    while (iterator.hasNext()) {
      if (child2 != iterator.next()) {
        iterator.remove();
      }
    }

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertNull(child3.getParent());
  }

  public void testAddManagesParentCorrectly() {
    JsonCollection parent = new JsonCollection();
    JsonCollection parent2 = new JsonCollection();
    JsonCollection child1 = new JsonCollection(null, Arrays.<Object>asList("a"));
    JsonCollection child2 = new JsonCollection(null, Arrays.<Object>asList("b"));
    JsonCollection child3 = new JsonCollection(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(child1);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(child1);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(child2);
    parent.add(child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.add(child1);

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove(child1);

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());
  }

  public void testRemoveManagesParentCorrectly() {
    JsonCollection parent = new JsonCollection();
    JsonCollection parent2 = new JsonCollection();
    JsonCollection child1 = new JsonCollection(null, Arrays.<Object>asList("a"));
    JsonCollection child2 = new JsonCollection(null, Arrays.<Object>asList("b"));
    JsonCollection child3 = new JsonCollection(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(child1);
    parent.add(child2);
    parent.add(child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove(child1);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove(child1);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove(child2);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.add(child3);
    parent.remove(child3);
    assertEquals(parent2, child3.getParent());
  }

  public void testAddAllManagesParentCorrectly() {
    JsonCollection parent = new JsonCollection();
    JsonCollection parent2 = new JsonCollection();
    JsonCollection child1 = new JsonCollection(null, Arrays.<Object>asList("a"));
    JsonCollection child2 = new JsonCollection(null, Arrays.<Object>asList("b"));
    JsonCollection child3 = new JsonCollection(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.addAll(Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.addAll(parent);

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent2, child2.getParent());
    assertEquals(parent2, child3.getParent());

    parent.addAll(Arrays.asList(child2));

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent2, child3.getParent());


  }

  public void testRemoveAllManagesParentCorrectly() {
    JsonCollection parent = new JsonCollection();
    JsonCollection parent2 = new JsonCollection();
    JsonCollection child1 = new JsonCollection(null, Arrays.<Object>asList("a"));
    JsonCollection child2 = new JsonCollection(null, Arrays.<Object>asList("b"));
    JsonCollection child3 = new JsonCollection(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.addAll(Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.removeAll(parent);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.addAll(Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.add(child3);
    parent.removeAll(parent);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertEquals(parent2, child3.getParent());
  }

  public void testRetainAllManagesParentCorrectly() {
    JsonCollection parent = new JsonCollection();
    JsonCollection parent2 = new JsonCollection();
    JsonCollection child1 = new JsonCollection(null, Arrays.<Object>asList("a"));
    JsonCollection child2 = new JsonCollection(null, Arrays.<Object>asList("b"));
    JsonCollection child3 = new JsonCollection(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.addAll(Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.retainAll(parent);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.retainAll(Arrays.asList(child1, child2));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertNull(child3.getParent());

    parent2.add(child2);
    parent.retainAll(Collections.<Object>emptyList());
    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent2, child2.getParent());
    assertNull(child3.getParent());
  }

  public void testClearManagesParentCorrectly() {
    JsonCollection parent = new JsonCollection();
    JsonCollection parent2 = new JsonCollection();
    JsonCollection child1 = new JsonCollection(null, Arrays.<Object>asList("a"));
    JsonCollection child2 = new JsonCollection(null, Arrays.<Object>asList("b"));
    JsonCollection child3 = new JsonCollection(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.addAll(Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.clear();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.addAll(Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.add(child2);
    parent.clear();

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent2, child2.getParent());
    assertNull(child3.getParent());
  }

  private JsonCollection makeCol(String... values) {
    JsonCollection jsonCollection = new JsonCollection();
    jsonCollection.addAll(Arrays.asList(values));
    return jsonCollection;
  }
}
