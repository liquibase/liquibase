package liquibase.database.object;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Snowflake Schema database object model.
 * Tests all properties, validation, and object behavior.
 * 
 * ADDRESSES_CORE_ISSUE: Complete TDD coverage for Schema object implementation.
 */
public class SchemaTest {

    @Test 
    public void testSchemaCreationWithMinimalProperties() {
        Schema schema = new Schema();
        schema.setName("TEST_SCHEMA");
        
        assertEquals("TEST_SCHEMA", schema.getName());
        assertEquals("schema", schema.getObjectTypeName());
        assertTrue(schema.snapshotByDefault());
    }

    @Test
    public void testSchemaCreationWithConstructor() {
        Schema schema = new Schema("CONSTRUCTOR_SCHEMA");
        
        assertEquals("CONSTRUCTOR_SCHEMA", schema.getName());
        assertEquals("schema", schema.getObjectTypeName());
    }

    @Test
    public void testConfigurationProperties() {
        Schema schema = new Schema("CONFIG_SCHEMA");
        
        // Test configuration properties (included in diffs)
        schema.setComment("Test schema comment");
        assertEquals("Test schema comment", schema.getComment());
        
        schema.setDataRetentionTimeInDays("7");
        assertEquals("7", schema.getDataRetentionTimeInDays());
        
        schema.setMaxDataExtensionTimeInDays("14");
        assertEquals("14", schema.getMaxDataExtensionTimeInDays());
        
        schema.setDefaultDdlCollation("utf8");
        assertEquals("utf8", schema.getDefaultDdlCollation());
        
        schema.setCloneFrom("SOURCE_SCHEMA");
        assertEquals("SOURCE_SCHEMA", schema.getCloneFrom());
        
        schema.setTransient(true);
        assertTrue(schema.getTransient());
        
        schema.setManagedAccess(true);
        assertTrue(schema.getManagedAccess());
        
        schema.setWithTag(true);
        assertTrue(schema.getWithTag());
    }

    @Test
    public void testStateProperties() {
        Schema schema = new Schema("STATE_SCHEMA");
        
        // Test state properties (excluded from diffs)
        schema.setCreatedOn("2024-01-01");
        assertEquals("2024-01-01", schema.getCreatedOn());
        
        schema.setOwner("SCHEMA_OWNER");
        assertEquals("SCHEMA_OWNER", schema.getOwner());
        
        schema.setOwnerRoleType("ROLE");
        assertEquals("ROLE", schema.getOwnerRoleType());
        
        schema.setDefault(true);
        assertTrue(schema.getDefault());
        
        schema.setCurrent(true);
        assertTrue(schema.getCurrent());
        
        schema.setRetentionTime("7");
        assertEquals("7", schema.getRetentionTime());
        
        schema.setDroppedOn(null);
        assertNull(schema.getDroppedOn());
        
        schema.setKind("STANDARD");
        assertEquals("STANDARD", schema.getKind());
        
        schema.setResourceMonitorName("MONITOR");
        assertEquals("MONITOR", schema.getResourceMonitorName());
        
        schema.setBudget("1000");
        assertEquals("1000", schema.getBudget());
        
        schema.setLastAltered("2024-01-02");
        assertEquals("2024-01-02", schema.getLastAltered());
        
        schema.setOrigin("ACCOUNT");
        assertEquals("ACCOUNT", schema.getOrigin());
        
        schema.setDatabaseName("TEST_DB");
        assertEquals("TEST_DB", schema.getDatabaseName());
    }

    @Test
    public void testEqualsAndHashCode() {
        Schema schema1 = new Schema("EQUAL_SCHEMA");
        Schema schema2 = new Schema("EQUAL_SCHEMA");
        Schema schema3 = new Schema("DIFFERENT_SCHEMA");
        
        // Test equals
        assertEquals(schema1, schema2);
        assertNotEquals(schema1, schema3);
        assertEquals(schema1, schema1); // reflexive
        
        // Test hashCode consistency
        assertEquals(schema1.hashCode(), schema2.hashCode());
        assertNotEquals(schema1.hashCode(), schema3.hashCode());
    }

    @Test
    public void testCompareTo() {
        Schema schemaA = new Schema("A_SCHEMA");
        Schema schemaB = new Schema("B_SCHEMA");
        Schema schemaC = new Schema("A_SCHEMA");
        
        assertTrue(schemaA.compareTo(schemaB) < 0); // A comes before B
        assertTrue(schemaB.compareTo(schemaA) > 0); // B comes after A
        assertEquals(0, schemaA.compareTo(schemaC)); // Same names
    }

    @Test
    public void testToString() {
        Schema schema = new Schema("STRING_SCHEMA");
        assertEquals("STRING_SCHEMA", schema.toString());
    }

    @Test 
    public void testNullNameHandling() {
        Schema schema1 = new Schema();
        Schema schema2 = new Schema();
        
        // Both null names should be equal
        assertEquals(schema1, schema2);
        assertEquals(schema1.hashCode(), schema2.hashCode());
        
        // Null name vs non-null name
        Schema schema3 = new Schema("NOT_NULL");
        assertNotEquals(schema1, schema3);
        assertNotEquals(schema1.hashCode(), schema3.hashCode());
    }

    @Test
    public void testSchemaObjectHierarchy() {
        Schema schema = new Schema("HIERARCHY_SCHEMA");
        
        // Schema doesn't have containing objects (it's a top-level object within a database)
        assertNull(schema.getContainingObjects());
        
        // Schema IS the schema object
        assertNull(schema.getSchema());
        
        // Serialization namespace
        assertEquals(Schema.STANDARD_SNAPSHOT_NAMESPACE, schema.getSerializedObjectNamespace());
    }

    @Test
    public void testBooleanPropertyDefaults() {
        Schema schema = new Schema("BOOLEAN_SCHEMA");
        
        // Boolean properties should default to null (not false)
        assertNull(schema.getTransient());
        assertNull(schema.getManagedAccess());
        assertNull(schema.getWithTag());
        assertNull(schema.getDefault());
        assertNull(schema.getCurrent());
    }

    @Test
    public void testPropertyChaining() {
        // Test that setters return the schema object for chaining
        Schema schema = new Schema("CHAIN_SCHEMA");
        
        Schema result = schema.setName("CHAINED_SCHEMA");
        assertSame(schema, result);
        assertEquals("CHAINED_SCHEMA", schema.getName());
    }
}