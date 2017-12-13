package org.miselin.xmppbot.util;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ConcurrentNavigableMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

/**
 * Provides utilities that wrap a lower-level database layer to allow trivial
 * storage of key/value pairs. Note that you may need to provide your own custom
 * namespaces in your keys, as nothing is provided to do so by the class.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class PersistentStorage {

    private static DB db_ = null;
    private static ConcurrentNavigableMap<String, Object> map_ = null;

    public static void initialize(boolean ondisk) {
        if (null != db_) {
            return;
        }

        if (ondisk) {
            db_ = DBMaker.fileDB(new File("local.db"))
                    .closeOnJvmShutdown()
                    .make();
        } else {
            db_ = DBMaker.memoryDB().make();
        }
        // TODO(miselin): could use these collections to support namespaces...
        map_ = db_.treeMap("xmppbot")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    /**
     * Shut down the database. It will be re-initialized on the next
     * get/set/exists operation.
     */
    public static void shutdown() {
        if (null != db_) {
            db_.commit();
            db_.close();
            db_ = null;
        }
    }

    /**
     * Get the value for the given key, or null if not found.
     *
     * @param key the key to look up
     * @return the value (cast to your desired type), or null if not found
     */
    public static Object get(String key) {
        initialize(true);
        return map_.get(key);
    }

    /**
     * Set the value for the given key
     *
     * @param <T> the type of the value to set
     * @param key the key to set a value for
     * @param obj the value to set
     */
    public static void set(String key, Object obj) {
        initialize(true);
        if (null == obj) {
            map_.remove(key);
        } else {
            map_.put(key, obj);
        }
        db_.commit();
    }

    /**
     * Identifies whether a value for the given key exists or not.
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    public static boolean exists(String key) {
        initialize(true);
        return map_.containsKey(key);
    }

}
