package liquibase.database.object;

import liquibase.structure.core.Catalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Tests for Snowflake Database object model
 * Testing all XSD configuration attributes and operational state attributes
 * Based on snowflake_database_snapshot_diff_requirements.md
 */
public class DatabaseTest {
    
    private Database database;
    
    @BeforeEach
    void setUp() {
        database = new Database();
    }
    
    // ===== IDENTITY AND CORE PROPERTIES TESTS =====
    
    @Test
    void testDatabaseCreationWithMinimalProperties() {
        database.setName("TEST_DB");
        
        assertEquals("TEST_DB", database.getName());
        assertEquals("TEST_DB", database.getSnapshotId());
    }
    
    @Test
    void testDatabaseNameValidation() {
        // Valid name
        assertDoesNotThrow(() -> database.setName("VALID_DB"));
        assertEquals("VALID_DB", database.getName());
        
        // Null name should throw exception
        assertThrows(IllegalArgumentException.class, () -> database.setName(null));
        
        // Empty name should throw exception
        assertThrows(IllegalArgumentException.class, () -> database.setName(""));
        
        // Whitespace only should throw exception
        assertThrows(IllegalArgumentException.class, () -> database.setName("   "));
    }
    
    // ===== XSD CONFIGURATION ATTRIBUTES TESTS =====
    
    @Test
    void testCommentProperty() {
        // Null comment should be allowed
        database.setComment(null);
        assertNull(database.getComment());
        
        // Valid comment
        database.setComment("Test database comment");
        assertEquals("Test database comment", database.getComment());
        
        // Empty comment should be treated as null
        database.setComment("");
        assertNull(database.getComment());
        
        // Comment length validation (if any)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("A");
        }
        String longComment = sb.toString();
        assertDoesNotThrow(() -> database.setComment(longComment));
    }
    
    @Test
    void testDataRetentionTimeInDays() {
        // Valid retention time
        database.setDataRetentionTimeInDays(7);
        assertEquals(7, database.getDataRetentionTimeInDays());
        
        // Minimum value (0)
        database.setDataRetentionTimeInDays(0);
        assertEquals(0, database.getDataRetentionTimeInDays());
        
        // Maximum value (90)
        database.setDataRetentionTimeInDays(90);
        assertEquals(90, database.getDataRetentionTimeInDays());
        
        // Invalid values should throw exception
        assertThrows(IllegalArgumentException.class, () -> database.setDataRetentionTimeInDays(-1));
        assertThrows(IllegalArgumentException.class, () -> database.setDataRetentionTimeInDays(91));
        
        // Null should be allowed (inherits from account)
        database.setDataRetentionTimeInDays(null);
        assertNull(database.getDataRetentionTimeInDays());
    }
    
    @Test
    void testMaxDataExtensionTimeInDays() {
        // Valid extension time
        database.setMaxDataExtensionTimeInDays(14);
        assertEquals(14, database.getMaxDataExtensionTimeInDays());
        
        // Null should be allowed (default behavior)
        database.setMaxDataExtensionTimeInDays(null);
        assertNull(database.getMaxDataExtensionTimeInDays());
        
        // Should accept reasonable values
        database.setMaxDataExtensionTimeInDays(0);
        assertEquals(0, database.getMaxDataExtensionTimeInDays());
    }
    
    @Test
    void testTransientProperty() {
        // Default should be false
        assertFalse(database.getTransient());
        
        // Should accept boolean values
        database.setTransient(true);
        assertTrue(database.getTransient());
        
        database.setTransient(false);
        assertFalse(database.getTransient());
        
        // Null should default to false
        database.setTransient(null);
        assertFalse(database.getTransient());
    }
    
    @Test
    void testDefaultDdlCollation() {
        // Should accept valid collation values
        database.setDefaultDdlCollation("utf8");
        assertEquals("utf8", database.getDefaultDdlCollation());
        
        // Null should be allowed
        database.setDefaultDdlCollation(null);
        assertNull(database.getDefaultDdlCollation());
        
        // Empty string should be treated as null
        database.setDefaultDdlCollation("");
        assertNull(database.getDefaultDdlCollation());
    }
    
    @Test
    void testTagProperty() {
        // Should accept tag values
        database.setTag("environment=dev");
        assertEquals("environment=dev", database.getTag());
        
        // Null should be allowed
        database.setTag(null);
        assertNull(database.getTag());
    }
    
    // ===== ICEBERG DATABASE ATTRIBUTES TESTS =====
    
    @Test
    void testExternalVolume() {
        database.setExternalVolume("my_external_volume");
        assertEquals("my_external_volume", database.getExternalVolume());
        
        database.setExternalVolume(null);
        assertNull(database.getExternalVolume());
    }
    
    @Test
    void testCatalogProperty() {
        database.setCatalogString("polaris_catalog");
        assertEquals("polaris_catalog", database.getCatalogString());
        
        database.setCatalogString(null);
        assertNull(database.getCatalogString());
    }
    
    @Test
    void testStorageSerializationPolicy() {
        database.setStorageSerializationPolicy("COMPATIBLE");
        assertEquals("COMPATIBLE", database.getStorageSerializationPolicy());
        
        database.setStorageSerializationPolicy(null);
        assertNull(database.getStorageSerializationPolicy());
    }
    
    // ===== OPERATIONAL STATE ATTRIBUTES TESTS =====
    
    @Test
    void testOwnerProperty() {
        database.setOwner("SYSADMIN");
        assertEquals("SYSADMIN", database.getOwner());
        
        database.setOwner(null);
        assertNull(database.getOwner());
    }
    
    @Test
    void testDatabaseType() {
        database.setDatabaseType("STANDARD");
        assertEquals("STANDARD", database.getDatabaseType());
        
        // Should accept other valid types
        database.setDatabaseType("SHARED");
        assertEquals("SHARED", database.getDatabaseType());
        
        database.setDatabaseType("IMPORTED");
        assertEquals("IMPORTED", database.getDatabaseType());
    }
    
    @Test
    void testTimestamps() {
        Date now = new Date();
        
        database.setCreated(now);
        assertEquals(now, database.getCreated());
        
        database.setLastAltered(now);
        assertEquals(now, database.getLastAltered());
        
        // Null timestamps should be allowed
        database.setCreated(null);
        assertNull(database.getCreated());
        
        database.setLastAltered(null);
        assertNull(database.getLastAltered());
    }
    
    // ===== OBJECT RELATIONSHIP TESTS =====
    
    @Test
    void testContainingObjects() {
        // Database should have no containing objects (top-level)
        assertNotNull(database.getContainingObjects());
        assertEquals(0, database.getContainingObjects().length);
    }
    
    @Test
    void testCatalogRelationship() {
        Catalog catalog = new Catalog("TEST_CATALOG");
        database.setCatalog(catalog);
        assertEquals(catalog, database.getCatalog());
    }
    
    // ===== EQUALS AND HASHCODE TESTS =====
    
    @Test
    void testEqualsAndHashCode() {
        Database db1 = new Database();
        db1.setName("TEST_DB");
        
        Database db2 = new Database();
        db2.setName("TEST_DB");
        
        Database db3 = new Database();
        db3.setName("OTHER_DB");
        
        // Same name should be equal
        assertEquals(db1, db2);
        assertEquals(db1.hashCode(), db2.hashCode());
        
        // Different names should not be equal
        assertNotEquals(db1, db3);
        assertNotEquals(db1.hashCode(), db3.hashCode());
        
        // Null comparison
        assertNotEquals(db1, null);
        
        // Same object
        assertEquals(db1, db1);
    }
    
    // ===== PROPERTY CATEGORIZATION TESTS =====
    
    @Test
    void testConfigurationProperties() {
        // These properties should be included in structural comparison
        database.setName("TEST_DB");
        database.setComment("Test comment");
        database.setDataRetentionTimeInDays(7);
        database.setTransient(true);
        database.setDefaultDdlCollation("utf8");
        
        assertNotNull(database.getName());
        assertNotNull(database.getComment());
        assertNotNull(database.getDataRetentionTimeInDays());
        assertNotNull(database.getTransient());
        assertNotNull(database.getDefaultDdlCollation());
    }
    
    @Test
    void testStatePropertiesExcludedFromComparison() {
        // These properties should be excluded from structural comparison
        database.setOwner("ADMIN");
        database.setCreated(new Date());
        database.setLastAltered(new Date());
        database.setDatabaseType("STANDARD");
        
        // Properties should be settable but marked as state properties
        assertNotNull(database.getOwner());
        assertNotNull(database.getCreated());
        assertNotNull(database.getLastAltered());
        assertNotNull(database.getDatabaseType());
    }
    
    // ===== VALIDATION TESTS =====
    
    @Test
    void testSnapshotId() {
        database.setName("TEST_DATABASE");
        assertEquals("TEST_DATABASE", database.getSnapshotId());
        
        // Snapshot ID should be based on name only for databases
        database.setOwner("ADMIN");
        database.setComment("Test");
        assertEquals("TEST_DATABASE", database.getSnapshotId());
    }
    
    @Test
    void testToString() {
        database.setName("TEST_DB");
        database.setComment("Test database");
        database.setTransient(true);
        
        String toString = database.toString();
        assertTrue(toString.contains("TEST_DB"));
        assertTrue(toString.contains("Database"));
    }
    
    // ===== CREATION-ONLY ATTRIBUTES TESTS =====
    
    @Test
    void testOrReplaceFlag() {
        // orReplace should be settable but excluded from diff
        database.setOrReplace(true);
        assertTrue(database.getOrReplace());
        
        database.setOrReplace(false);
        assertFalse(database.getOrReplace());
    }
    
    @Test
    void testIfNotExistsFlag() {
        // ifNotExists should be settable but excluded from diff
        database.setIfNotExists(true);
        assertTrue(database.getIfNotExists());
        
        database.setIfNotExists(false);
        assertFalse(database.getIfNotExists());
    }
}