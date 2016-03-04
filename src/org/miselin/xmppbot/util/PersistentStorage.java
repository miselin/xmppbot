
package org.miselin.xmppbot.util;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Provides utilities that wrap a lower-level database layer to allow trivial storage of key/value
 * pairs. Note that you may need to provide your own custom namespaces in your keys, as nothing is
 * provided to do so by the class.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class PersistentStorage {

  private static DB db_ = null;
  private static BTreeMap<String, Object> map_ = null;

  @Override
  public void finalize() throws Throwable {
    if (null != db_) {
      db_.commit();
      db_.close();
    }
    super.finalize();
  }

  public void initialize(boolean ondisk) {
    if (null != db_) {
      return;
    }

    if (ondisk) {
      db_ = DBMaker.fileDB("local.db").make();
    } else {
      db_ = DBMaker.heapDB().make();
    }
    // TODO(miselin): could use these collections to support namespaces...
    map_ = (BTreeMap<String, Object>) db_.treeMap("xmppbot").create();
  }

  /**
   * Get the value for the given key, or null if not found.
   *
   * @param key the key to look up
   * @return the value (cast to your desired type), or null if not found
   */
  public Object get(String key) {
    initialize(true);
    return map_.get(key);
  }

  /**
   * Set the value for the given key
   *
   * @param key the key to set a value for
   * @param obj the value to set
   */
  public void set(String key, Object obj) {
    initialize(true);
    if (null == obj) {
      map_.remove(key);
    } else {
      map_.put(key, obj);
    }
  }

  /**
   * Identifies whether a value for the given key exists or not.
   *
   * @param key the key to check
   * @return true if the key exists, false otherwise
   */
  public boolean exists(String key) {
    initialize(true);
    return map_.containsKey(key);
  }

}
