
package org.miselin.xmppbot.util;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Tests for the persistent storage utility.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class PersistentStorageTest {

  @Before
  public void beforeTest() {
    PersistentStorage.shutdown();
  }

  /**
   * Test of get method, of class PersistentStorage.
   */
  @Test
  public void testGet() {
    PersistentStorage.initialize(false);

    assertEquals("get() for a key that doesn't exist returns null", PersistentStorage.get("foo"), null);
    PersistentStorage.set("foo", this);
    assertEquals("get() for an existing key returns the value", PersistentStorage.get("foo"), this);
  }

  /**
   * Test of set method, of class PersistentStorage.
   */
  @Test
  public void testSet() {
    PersistentStorage.initialize(false);

    PersistentStorage.set("foo", "bar");
    PersistentStorage.set("foo", "baz");
    assertEquals(PersistentStorage.get("foo"), "baz");
    PersistentStorage.set("foo", null);
    assertFalse(PersistentStorage.exists("foo"));
  }

  /**
   * Tests that value comparisons are used for keys, not references.
   */
  @Test
  public void testUsesValues() {
    PersistentStorage.initialize(false);

    String key1 = "foo";
    String key2 = new String("foo");

    PersistentStorage.set(key1, "bar");
    assertEquals(PersistentStorage.get(key1), "bar");
    assertEquals(PersistentStorage.get(key2), "bar");
  }

  /**
   * Tests on-disk persistence.
   */
  @Test
  public void testOnDisk() {
    PersistentStorage.initialize(true);

    PersistentStorage.set("foo", "bar");
    assertEquals(PersistentStorage.get("foo"), "bar");

    PersistentStorage.shutdown();

    PersistentStorage.initialize(true);
    assertEquals(PersistentStorage.get("foo"), "bar");
  }

  /**
   * Tests more complex types.
   */
  @Test
  public void testComplexTypes() {
    PersistentStorage.initialize(false);

    List<String> l = Arrays.asList("foo", "bar", "baz");

    PersistentStorage.set("foo", l);
    assertEquals(PersistentStorage.get("foo"), l);
  }

  /**
   * Test of exists method, of class PersistentStorage.
   */
  @Test
  public void testExists() {
    PersistentStorage.initialize(false);

    assertFalse(PersistentStorage.exists("foo"));
    PersistentStorage.set("foo", "bar");
    assertTrue(PersistentStorage.exists("foo"));
  }

}
