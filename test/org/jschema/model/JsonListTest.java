package org.jschema.model;

import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.*;

public class JsonListTest extends TestCase {

  public void testSanity()  {
    JsonList jsonList = makeList("a", "b", "c");
    assertEquals("a", jsonList.get(0));
    assertEquals("c", jsonList.get(2));

    jsonList.set(0, "b");
    assertEquals(Arrays.asList("b", "b", "c"), jsonList);

    jsonList.add("d");
    assertEquals(Arrays.asList("b", "b", "c", "d"), jsonList);

    jsonList.remove(2);
    assertEquals(Arrays.asList("b", "b", "d"), jsonList);

    assertEquals(0, jsonList.indexOf("b"));
    assertEquals(1, jsonList.lastIndexOf("b"));
    assertEquals(2, jsonList.indexOf("d"));
  }

  public void testAddAllManagesParentCorrectly() {
    JsonList parent = new JsonList();
    JsonList parent2 = new JsonList();
    JsonList child1 = new JsonList(null, Arrays.<Object>asList("a"));
    JsonList child2 = new JsonList(null, Arrays.<Object>asList("b"));
    JsonList child3 = new JsonList(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.addAll(0, Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.addAll(2, Arrays.asList(child1, child2, child3));

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.addAll(0, parent);

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent2, child2.getParent());
    assertEquals(parent2, child3.getParent());

    parent.addAll(2, Arrays.asList(child2));

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent2, child3.getParent());
  }

  public void testAddManagesParentCorrectly() {
    JsonList parent = new JsonList();
    JsonList parent2 = new JsonList();
    JsonList child1 = new JsonList(null, Arrays.<Object>asList("a"));
    JsonList child2 = new JsonList(null, Arrays.<Object>asList("b"));
    JsonList child3 = new JsonList(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(0, child1);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(1, child1);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(2, child2);
    parent.add(3, child3);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.add(0, child1);

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
  
  public void testSetManagesParentCorrectly() {
    JsonList parent = new JsonList();
    JsonList parent2 = new JsonList();
    JsonList child1 = new JsonList(null, Arrays.<Object>asList("a"));
    JsonList child2 = new JsonList(null, Arrays.<Object>asList("b"));
    JsonList child3 = new JsonList(null, Arrays.<Object>asList("c"));

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(0, child1);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.add(1, child1);

    assertNull(parent.getParent());
    assertEquals(parent, child1.getParent());
    assertNull(child2.getParent());
    assertNull(child3.getParent());

    parent.set(1, child2);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertNull(child3.getParent());

    parent2.add(0, child1);

    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertNull(child3.getParent());

    parent2.set(0, child3);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent2, child3.getParent());

  }

  public void testRemoveManagesParentCorrectly() {
    JsonList parent = new JsonList();
    JsonList parent2 = new JsonList();
    JsonList child1 = new JsonList(null, Arrays.<Object>asList("a"));
    JsonList child2 = new JsonList(null, Arrays.<Object>asList("b"));
    JsonList child3 = new JsonList(null, Arrays.<Object>asList("c"));

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

    parent.remove(0);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    parent.remove(0);

    assertNull(parent.getParent());
    assertNull(child1.getParent());
    assertNull(child2.getParent());
    assertEquals(parent, child3.getParent());

    parent2.add(child3);
    parent.remove(0);
    assertEquals(parent2, child3.getParent());
  }

  public void testListIteratorProperlyManagesParent() {
    JsonList parent = new JsonList();
    JsonList parent2 = new JsonList();
    JsonList child1 = new JsonList(null, Arrays.<Object>asList("a"));
    JsonList child2 = new JsonList(null, Arrays.<Object>asList("b"));
    JsonList child3 = new JsonList(null, Arrays.<Object>asList("c"));

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

    ListIterator<Object> liParent = parent.listIterator();
    ListIterator<Object> liParent2 = parent2.listIterator();

    liParent2.add(liParent.next());
    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertEquals(parent, child2.getParent());
    assertEquals(parent, child3.getParent());

    liParent.next();
    liParent.previous();

    liParent.remove();
    assertNull(parent.getParent());
    assertEquals(parent2, child1.getParent());
    assertNull(child2.getParent());
    assertEquals(parent, child3.getParent());
  }

//  public void testSubListProperlyManagesParent() {
//    JsonList parent = new JsonList();
//    JsonList parent2 = new JsonList();
//    JsonList child1 = new JsonList(null, Arrays.<Object>asList("a"));
//    JsonList child2 = new JsonList(null, Arrays.<Object>asList("b"));
//    JsonList child3 = new JsonList(null, Arrays.<Object>asList("c"));
//
//    assertNull(parent.getParent());
//    assertNull(child1.getParent());
//    assertNull(child2.getParent());
//    assertNull(child3.getParent());
//
//    parent.add(child1);
//    parent.add(child2);
//    parent.add(child3);
//
//    assertNull(parent.getParent());
//    assertEquals(parent, child1.getParent());
//    assertEquals(parent, child2.getParent());
//    assertEquals(parent, child3.getParent());
//
//    parent.subList(0, 3).remove(0);
//
//    assertNull(parent.getParent());
//    assertNull(child1.getParent());
//    assertEquals(parent, child2.getParent());
//    assertEquals(parent, child3.getParent());
//
//    parent.subList(0, 2).remove(0);
//
//    assertNull(parent.getParent());
//    assertNull(child1.getParent());
//    assertNull(child2.getParent());
//    assertEquals(parent, child3.getParent());
//
//    parent2.add(child3);
//    parent.subList(0, 1).remove(0);
//    assertEquals(parent2, child3.getParent());
//  }

  private JsonList makeList(Object... values) {
    JsonList lst = new JsonList();
    lst.addAll(Arrays.asList(values));
    return lst;
  }
}
