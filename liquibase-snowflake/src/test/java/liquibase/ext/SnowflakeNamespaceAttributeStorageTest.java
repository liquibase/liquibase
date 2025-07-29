package liquibase.ext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SnowflakeNamespaceAttributeStorage
 */
@DisplayName("SnowflakeNamespaceAttributeStorage")
public class SnowflakeNamespaceAttributeStorageTest {
    
    @BeforeEach
    void setUp() {
        // Clear storage before each test
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @AfterEach
    void tearDown() {
        // Clear storage after each test
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @Test
    @DisplayName("Should store and retrieve attributes for a table")
    void shouldStoreAndRetrieveAttributes() {
        // Given
        String tableName = "TEST_TABLE";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("transient", "true");
        attributes.put("clusterBy", "id,created_at");
        attributes.put("dataRetentionTimeInDays", "7");
        
        // When
        SnowflakeNamespaceAttributeStorage.storeAttributes(tableName, attributes);
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes(tableName);
        
        // Then
        assertNotNull(retrieved);
        assertEquals("true", retrieved.get("transient"));
        assertEquals("id,created_at", retrieved.get("clusterBy"));
        assertEquals("7", retrieved.get("dataRetentionTimeInDays"));
    }
    
    @Test
    @DisplayName("Should return null for non-existent table")
    void shouldReturnNullForNonExistentTable() {
        // When
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes("NON_EXISTENT");
        
        // Then
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("Should handle null object name")
    void shouldHandleNullObjectName() {
        // Given
        Map<String, String> attributes = new HashMap<>();
        attributes.put("transient", "true");
        
        // When
        SnowflakeNamespaceAttributeStorage.storeAttributes(null, attributes);
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes(null);
        
        // Then
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("Should handle null attributes")
    void shouldHandleNullAttributes() {
        // When
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", null);
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes("TEST_TABLE");
        
        // Then
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("Should handle empty attributes")
    void shouldHandleEmptyAttributes() {
        // When
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", new HashMap<>());
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes("TEST_TABLE");
        
        // Then
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("Should remove attributes")
    void shouldRemoveAttributes() {
        // Given
        String tableName = "TEST_TABLE";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("transient", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes(tableName, attributes);
        
        // When
        SnowflakeNamespaceAttributeStorage.removeAttributes(tableName);
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes(tableName);
        
        // Then
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("Should clear all stored attributes")
    void shouldClearAllStoredAttributes() {
        // Given
        Map<String, String> attributes1 = new HashMap<>();
        attributes1.put("transient", "true");
        Map<String, String> attributes2 = new HashMap<>();
        attributes2.put("temporary", "true");
        
        SnowflakeNamespaceAttributeStorage.storeAttributes("TABLE1", attributes1);
        SnowflakeNamespaceAttributeStorage.storeAttributes("TABLE2", attributes2);
        
        // When
        SnowflakeNamespaceAttributeStorage.clear();
        
        // Then
        assertNull(SnowflakeNamespaceAttributeStorage.getAttributes("TABLE1"));
        assertNull(SnowflakeNamespaceAttributeStorage.getAttributes("TABLE2"));
    }
    
    @Test
    @DisplayName("Should create defensive copy of attributes")
    void shouldCreateDefensiveCopy() {
        // Given
        String tableName = "TEST_TABLE";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("transient", "true");
        
        // When
        SnowflakeNamespaceAttributeStorage.storeAttributes(tableName, attributes);
        
        // Modify original map
        attributes.put("transient", "false");
        attributes.put("temporary", "true");
        
        // Then - stored values should not change
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes(tableName);
        assertEquals("true", retrieved.get("transient"));
        assertNull(retrieved.get("temporary"));
    }
    
    @Test
    @DisplayName("Should be thread-safe for concurrent access")
    void shouldBeThreadSafeForConcurrentAccess() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // When - Multiple threads storing and retrieving concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String tableName = "TABLE_" + threadId + "_" + j;
                        Map<String, String> attrs = new HashMap<>();
                        attrs.put("threadId", String.valueOf(threadId));
                        attrs.put("operation", String.valueOf(j));
                        
                        SnowflakeNamespaceAttributeStorage.storeAttributes(tableName, attrs);
                        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes(tableName);
                        
                        assertEquals(String.valueOf(threadId), retrieved.get("threadId"));
                        assertEquals(String.valueOf(j), retrieved.get("operation"));
                        
                        if (j % 2 == 0) {
                            SnowflakeNamespaceAttributeStorage.removeAttributes(tableName);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Then - All operations should complete without errors
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should overwrite existing attributes")
    void shouldOverwriteExistingAttributes() {
        // Given
        String tableName = "TEST_TABLE";
        Map<String, String> attributes1 = new HashMap<>();
        attributes1.put("transient", "true");
        attributes1.put("clusterBy", "id");
        
        Map<String, String> attributes2 = new HashMap<>();
        attributes2.put("temporary", "true");
        attributes2.put("clusterBy", "id,date");
        
        // When
        SnowflakeNamespaceAttributeStorage.storeAttributes(tableName, attributes1);
        SnowflakeNamespaceAttributeStorage.storeAttributes(tableName, attributes2);
        
        // Then - Should have only the second set of attributes
        Map<String, String> retrieved = SnowflakeNamespaceAttributeStorage.getAttributes(tableName);
        assertNull(retrieved.get("transient"));
        assertEquals("true", retrieved.get("temporary"));
        assertEquals("id,date", retrieved.get("clusterBy"));
    }
}