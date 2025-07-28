package liquibase.ext.snowflake;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe storage for Snowflake namespace attributes.
 * This stores attributes between parsing and SQL generation.
 */
public class SnowflakeNamespaceAttributeStorage {
    // Storage keyed by object type and name (e.g., "sequence:TEST_SEQ")
    private static final ConcurrentHashMap<String, Map<String, String>> storage = 
        new ConcurrentHashMap<>();
    
    public static void storeAttributes(String objectType, String objectName, Map<String, String> attributes) {
        if (objectType != null && objectName != null && attributes != null && !attributes.isEmpty()) {
            String key = objectType + ":" + objectName;
            storage.put(key, new ConcurrentHashMap<>(attributes));
            System.out.println("[SnowflakeNamespaceAttributeStorage] Stored attributes for " + key + ": " + attributes);
        }
    }
    
    public static Map<String, String> getAttributes(String objectType, String objectName) {
        String key = objectType + ":" + objectName;
        return storage.get(key);
    }
    
    public static void removeAttributes(String objectType, String objectName) {
        String key = objectType + ":" + objectName;
        storage.remove(key);
    }
    
    public static void clear() {
        storage.clear();
    }
    
    // For debugging
    public static void printStorage() {
        System.out.println("[SnowflakeNamespaceAttributeStorage] Current storage: " + storage);
    }
}