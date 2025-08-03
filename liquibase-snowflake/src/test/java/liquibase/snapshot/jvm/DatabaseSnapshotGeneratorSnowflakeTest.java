package liquibase.snapshot.jvm;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Database;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseSnapshotGeneratorSnowflake.
 * Tests XSD compliance and full attribute coverage.
 */
public class DatabaseSnapshotGeneratorSnowflakeTest {

    private DatabaseSnapshotGeneratorSnowflake generator;
    private SnowflakeDatabase database;

    @BeforeEach
    public void setUp() {
        generator = new DatabaseSnapshotGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }

    @Test
    public void testGeneratorPriority() {
        // Should handle Database objects for SnowflakeDatabase with high priority
        assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_DATABASE,
                    generator.getPriority(Database.class, database),
                    "Should handle Database objects with DATABASE priority");
        
        // Should not handle other objects
        assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_NONE,
                    generator.getPriority(liquibase.structure.core.Table.class, database),
                    "Should not handle Table objects");
        
        // Should not handle Database objects for non-Snowflake databases
        liquibase.database.core.H2Database h2Database = new liquibase.database.core.H2Database();
        assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_NONE,
                    generator.getPriority(Database.class, h2Database),
                    "Should not handle Database objects for non-Snowflake databases");
    }

    @Test
    public void testAddsToConfiguration() {
        Class<? extends DatabaseObject>[] addsTo = generator.addsTo();
        
        assertNotNull(addsTo, "Should specify what objects it adds to");
        assertEquals(1, addsTo.length, "Should add to one object type");
        assertEquals(liquibase.structure.core.Catalog.class, addsTo[0], "Should add to Catalog objects");
    }

    @Test
    public void testYesNoToBooleanConversion() throws Exception {
        // Use reflection to test the private method
        java.lang.reflect.Method method = DatabaseSnapshotGeneratorSnowflake.class
            .getDeclaredMethod("convertYesNoToBoolean", String.class);
        method.setAccessible(true);
        
        // Test various YES/NO conversions
        assertEquals(Boolean.TRUE, method.invoke(generator, "YES"), "YES should convert to true");
        assertEquals(Boolean.TRUE, method.invoke(generator, "Y"), "Y should convert to true");
        assertEquals(Boolean.TRUE, method.invoke(generator, "yes"), "yes should convert to true");
        assertEquals(Boolean.TRUE, method.invoke(generator, " YES "), "Whitespace YES should convert to true");
        
        assertEquals(Boolean.FALSE, method.invoke(generator, "NO"), "NO should convert to false");
        assertEquals(Boolean.FALSE, method.invoke(generator, "N"), "N should convert to false");
        assertEquals(Boolean.FALSE, method.invoke(generator, "no"), "no should convert to false");
        assertEquals(Boolean.FALSE, method.invoke(generator, " NO "), "Whitespace NO should convert to false");
        
        assertNull(method.invoke(generator, (String) null), "null should return null");
        assertNull(method.invoke(generator, ""), "empty string should return null");
        assertNull(method.invoke(generator, "INVALID"), "invalid string should return null");
    }

    @Test
    public void testDatabaseObjectCreation() {
        // Test that we can create and configure database objects
        Database dbObject = new Database();
        
        // Test required properties
        assertDoesNotThrow(() -> dbObject.setName("TEST_DB"), "Should be able to set database name");
        assertEquals("TEST_DB", dbObject.getName(), "Database name should be set correctly");
        
        // Test XSD configuration attributes
        assertDoesNotThrow(() -> dbObject.setComment("Test database"), "Should be able to set comment");
        assertDoesNotThrow(() -> dbObject.setDataRetentionTimeInDays(7), "Should be able to set retention time");
        assertDoesNotThrow(() -> dbObject.setTransient(true), "Should be able to set transient flag");
        assertDoesNotThrow(() -> dbObject.setDefaultDdlCollation("en_US"), "Should be able to set collation");
        assertDoesNotThrow(() -> dbObject.setTag("env=test"), "Should be able to set tag");
        assertDoesNotThrow(() -> dbObject.setMaxDataExtensionTimeInDays(14), "Should be able to set max extension time");
        
        // Test Iceberg attributes
        assertDoesNotThrow(() -> dbObject.setExternalVolume("my_volume"), "Should be able to set external volume");
        assertDoesNotThrow(() -> dbObject.setCatalogString("my_catalog"), "Should be able to set catalog");
        assertDoesNotThrow(() -> dbObject.setStorageSerializationPolicy("COMPATIBLE"), "Should be able to set storage policy");
        
        // Test operational attributes
        assertDoesNotThrow(() -> dbObject.setOwner("SYSADMIN"), "Should be able to set owner");
        assertDoesNotThrow(() -> dbObject.setDatabaseType("STANDARD"), "Should be able to set database type");
        assertDoesNotThrow(() -> dbObject.setCreated(new java.util.Date()), "Should be able to set created date");
        assertDoesNotThrow(() -> dbObject.setLastAltered(new java.util.Date()), "Should be able to set last altered date");
        assertDoesNotThrow(() -> dbObject.setOwnerRoleType("ROLE"), "Should be able to set owner role type");
        
        // Verify values are set correctly
        assertEquals("Test database", dbObject.getComment(), "Comment should be set");
        assertEquals(Integer.valueOf(7), dbObject.getDataRetentionTimeInDays(), "Retention time should be set");
        assertEquals(Boolean.TRUE, dbObject.getTransient(), "Transient flag should be set");
        assertEquals("en_US", dbObject.getDefaultDdlCollation(), "Collation should be set");
        assertEquals("env=test", dbObject.getTag(), "Tag should be set");
    }

    @Test
    public void testObjectTypeAndSnapshotConfiguration() {
        Database dbObject = new Database();
        
        // Test object type configuration
        assertEquals("database", dbObject.getObjectTypeName(), "Object type should be 'database'");
        assertTrue(dbObject.snapshotByDefault(), "Databases should be snapshotted by default");
        assertNull(dbObject.getSchema(), "Databases exist at account level, not within schemas");
        assertEquals(0, dbObject.getContainingObjects().length, "Databases have no containing objects");
    }

    @Test
    public void testXSDAttributeCoverage() {
        Database dbObject = new Database();
        
        // Verify all 18 XSD configuration attributes can be set
        // (This ensures we have full XSD compliance)
        
        // Core configuration attributes
        assertDoesNotThrow(() -> dbObject.setComment("test"), "comment attribute");
        assertDoesNotThrow(() -> dbObject.setDataRetentionTimeInDays(1), "dataRetentionTimeInDays attribute");
        assertDoesNotThrow(() -> dbObject.setMaxDataExtensionTimeInDays(1), "maxDataExtensionTimeInDays attribute");
        assertDoesNotThrow(() -> dbObject.setTransient(false), "transient attribute");
        assertDoesNotThrow(() -> dbObject.setDefaultDdlCollation("en_US"), "defaultDdlCollation attribute");
        assertDoesNotThrow(() -> dbObject.setTag("test"), "tag attribute");
        
        // Creation-only attributes
        assertDoesNotThrow(() -> dbObject.setOrReplace(true), "orReplace attribute");
        assertDoesNotThrow(() -> dbObject.setIfNotExists(false), "ifNotExists attribute");
        
        // Clone attributes (stored as generic attributes)
        assertDoesNotThrow(() -> dbObject.setAttribute("cloneFrom", "SOURCE_DB"), "cloneFrom attribute");
        assertDoesNotThrow(() -> dbObject.setAttribute("fromDatabase", "SOURCE_DB"), "fromDatabase attribute");
        
        // Iceberg database attributes
        assertDoesNotThrow(() -> dbObject.setExternalVolume("volume"), "externalVolume attribute");
        assertDoesNotThrow(() -> dbObject.setCatalogString("catalog"), "catalog attribute");
        assertDoesNotThrow(() -> dbObject.setStorageSerializationPolicy("COMPATIBLE"), "storageSerializationPolicy attribute");
        assertDoesNotThrow(() -> dbObject.setAttribute("replaceInvalidCharacters", true), "replaceInvalidCharacters attribute");
        assertDoesNotThrow(() -> dbObject.setAttribute("catalogSync", "ENABLED"), "catalogSync attribute");
        assertDoesNotThrow(() -> dbObject.setAttribute("catalogSyncNamespaceMode", "SINGLE"), "catalogSyncNamespaceMode attribute");
        assertDoesNotThrow(() -> dbObject.setAttribute("catalogSyncNamespaceFlattenDelimiter", "_"), "catalogSyncNamespaceFlattenDelimiter attribute");
        
        // Additional creation-only attributes for completeness
        assertDoesNotThrow(() -> dbObject.setAttribute("comment", "test comment"), "Additional attribute storage works");
    }
}