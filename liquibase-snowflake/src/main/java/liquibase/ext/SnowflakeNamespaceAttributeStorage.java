package liquibase.ext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import liquibase.Scope;
import liquibase.logging.Logger;

/**
 * Thread-safe storage for Snowflake namespace attributes.
 * This allows us to capture namespace-prefixed attributes during XML parsing
 * and retrieve them during SQL generation.
 */
public class SnowflakeNamespaceAttributeStorage {
    
    private static final Logger logger = Scope.getCurrentScope().getLog(SnowflakeNamespaceAttributeStorage.class);
    
    private static final ConcurrentHashMap<String, Map<String, String>> storage = 
        new ConcurrentHashMap<>();
    
    /**
     * Store namespace attributes for a database object.
     * 
     * @param objectName The name of the database object (e.g., table name)
     * @param attributes Map of attribute names to values
     */
    public static void storeAttributes(String objectName, Map<String, String> attributes) {
        if (objectName != null && attributes != null && !attributes.isEmpty()) {
            storage.put(objectName, new ConcurrentHashMap<>(attributes));
            logger.fine("DEBUG: SnowflakeNamespaceAttributeStorage.storeAttributes - stored for '" + objectName + "': " + attributes);
            logger.fine("DEBUG: Storage now contains: " + storage);
        }
    }
    
    /**
     * Retrieve namespace attributes for a database object.
     * 
     * @param objectName The name of the database object
     * @return Map of attributes or null if none exist
     */
    public static Map<String, String> getAttributes(String objectName) {
        if (objectName == null) {
            return null;
        }
        Map<String, String> result = storage.get(objectName);
        logger.fine("DEBUG: SnowflakeNamespaceAttributeStorage.getAttributes - requested for '" + objectName + "', found: " + result);
        logger.fine("DEBUG: Storage currently contains: " + storage);
        return result;
    }
    
    /**
     * Remove namespace attributes for a database object.
     * Called after attributes have been used to prevent memory leaks.
     * 
     * @param objectName The name of the database object
     */
    public static void removeAttributes(String objectName) {
        if (objectName != null) {
            storage.remove(objectName);
        }
    }
    
    /**
     * Clear all stored attributes.
     * Useful for testing and cleanup.
     */
    public static void clear() {
        storage.clear();
    }
}