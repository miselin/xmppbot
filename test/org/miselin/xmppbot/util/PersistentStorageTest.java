
package org.miselin.xmppbot.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the persistent storage utility.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class PersistentStorageTest {

  /**
   * Test of get method, of class PersistentStorage.
   */
  @Test
  public void testGet() {
    PersistentStorage instance = new PersistentStorage();
    instance.initialize(false);

    assertEquals("get() for a key that doesn't exist returns null", instance.get("foo"), null);
    instance.set("foo", this);
    assertEquals("get() for an existing key returns the value", instance.get("foo"), this);
  }

  /**
   * Test of set method, of class PersistentStorage.
   */
  @Test
  public void testSet() {
    PersistentStorage instance = new PersistentStorage();
    instance.initialize(false);

    instance.set("foo", "bar");
    instance.set("foo", "baz");
    assertEquals(instance.get("foo"), "baz");
    instance.set("foo", null);
    assertFalse(instance.exists("foo"));
  }

  /**
   * Test of exists method, of class PersistentStorage.
   */
  @Test
  public void testExists() {
    PersistentStorage instance = new PersistentStorage();
    instance.initialize(false);

    assertFalse(instance.exists("foo"));
    instance.set("foo", "bar");
    assertTrue(instance.exists("foo"));
  }

}
