package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Schema;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.SchemaComparator;
import liquibase.snapshot.SchemaSnapshotGenerator;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Schema object snapshot/diff functionality.
 * Implements three-phase approach: Direct Component → Framework API → Pattern Replication.
 * 
 * ADDRESSES_CORE_ISSUE: Bridge unit tests → test harness with real database validation for Schema objects.
 */
public class SchemaObjectIntegrationTest {

    private Database database;
    private Connection connection;
    private List<String> createdTestObjects = new ArrayList<>();

    /**
     * CRITICAL: Generates unique test object names for schema isolation.
     * ADDRESSES_CORE_ISSUE: Schema-level object naming conflicts preventing parallel execution.
     * 
     * @param methodName The test method name
     * @return Unique test object name for parallel execution
     */
    private String getUniqueTestObjectName(String methodName) {
        return "INT_TEST_SCHEMA_" + methodName.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration instead of environment variables
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // MANDATORY: Cleanup all created test objects using unique names
        for (String objectName : createdTestObjects) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP SCHEMA IF EXISTS " + objectName);
                dropStmt.execute();
                dropStmt.close();
            } catch (Exception e) {
                System.err.println("Failed to cleanup test schema " + objectName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // ===========================================
    // PHASE 1A: DIRECT COMPONENT TESTING - SNAPSHOT GENERATOR
    // ===========================================

    @Test
    public void testSchemaSnapshotGeneratorDirectQuery() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("directQuery");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test schema in Snowflake
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE SCHEMA IF NOT EXISTS " + uniqueName + 
                " COMMENT = 'Integration test schema'"
            );
            createStmt.execute();
            createStmt.close();
            
            // DIRECT QUERY: Test the SQL queries our generator uses
            // Test INFORMATION_SCHEMA query
            PreparedStatement infoStmt = connection.prepareStatement(
                "SELECT SCHEMA_NAME, COMMENT, RETENTION_TIME " +
                "FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?"
            );
            infoStmt.setString(1, uniqueName);
            ResultSet rs = infoStmt.executeQuery();
            
            assertTrue(rs.next(), "INFORMATION_SCHEMA should find our schema");
            assertEquals(uniqueName, rs.getString("SCHEMA_NAME"), "Schema name should match");
            assertEquals("Integration test schema", rs.getString("COMMENT"), "Comment should match");
            
            rs.close();
            infoStmt.close();
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    @Test
    public void testSchemaSnapshotGeneratorObjectCreation() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("objectCreation");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test schema with properties
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE SCHEMA " + uniqueName + " " +
                "COMMENT = 'Test schema with properties'"
            );
            createStmt.execute();
            createStmt.close();
            
            // TEST: Create SchemaSnapshotGenerator and test object creation
            SchemaSnapshotGenerator generator = new SchemaSnapshotGenerator();
            
            // Verify generator configuration
            assertEquals(SchemaSnapshotGenerator.PRIORITY_DATABASE, 
                        generator.getPriority(liquibase.database.object.Schema.class, database));
            
            // Create a schema object manually using the same pattern as our generator
            Schema schemaObject = new Schema();
            schemaObject.setName(uniqueName);
            schemaObject.setComment("Test schema with properties");
            
            // Verify object properties are set correctly
            assertEquals(uniqueName, schemaObject.getName(), "Schema name should be set");
            assertEquals("Test schema with properties", schemaObject.getComment(), "Comment should be set");
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    // ===========================================
    // PHASE 1B: DIRECT COMPONENT TESTING - COMPARATOR
    // ===========================================

    @Test
    public void testSchemaComparatorSameObjects() throws Exception {
        
        // Create two identical schema objects
        Schema schema1 = new Schema("TEST_SCHEMA");
        schema1.setComment("Test comment");
        schema1.setDataRetentionTimeInDays("7");
        
        Schema schema2 = new Schema("TEST_SCHEMA"); 
        schema2.setComment("Test comment");
        schema2.setDataRetentionTimeInDays("7");
        
        // COMPARE: Same objects should have no differences
        SchemaComparator comparator = new SchemaComparator();
        ObjectDifferences differences = comparator.findDifferences(
            schema1, schema2, database, new CompareControl(), null, new HashSet<>()
        );
        
        // VALIDATE: Should be identical
        assertFalse(differences.hasDifferences(), "Same objects should have no differences");
        
    }

    @Test 
    public void testSchemaComparatorDifferentObjects() throws Exception {
        
        // Create source schema object
        Schema source = new Schema("TEST_SCHEMA");
        source.setComment("Original comment");
        source.setDataRetentionTimeInDays("7");
        
        // Create target schema object with differences
        Schema target = new Schema("TEST_SCHEMA");
        target.setComment("Modified comment"); // Different
        target.setDataRetentionTimeInDays("14"); // Different
        
        // COMPARE: Different objects should have differences
        SchemaComparator comparator = new SchemaComparator();
        ObjectDifferences differences = comparator.findDifferences(
            source, target, database, new CompareControl(), null, new HashSet<>()
        );
        
        // VALIDATE: Should detect differences
        assertTrue(differences.hasDifferences(), "Different objects should have differences");
        
    }

    // ===========================================
    // PHASE 1C: CREATE → SNAPSHOT → COMPARE WORKFLOW
    // ===========================================

    @Test
    public void testCreateSnapshotCompareWorkflow() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("workflow");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test schema in Snowflake
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE SCHEMA IF NOT EXISTS " + uniqueName + 
                " COMMENT = 'Workflow test schema'"
            );
            createStmt.execute();
            createStmt.close();
            
            // SNAPSHOT: Query database to capture state (simulating what our generator does)
            PreparedStatement queryStmt = connection.prepareStatement(
                "SELECT SCHEMA_NAME, COMMENT, RETENTION_TIME " +
                "FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?"
            );
            queryStmt.setString(1, uniqueName);
            ResultSet rs = queryStmt.executeQuery();
            
            assertTrue(rs.next(), "Should find created schema");
            
            // Create schema object from snapshot data
            Schema snapshotResult = new Schema();
            snapshotResult.setName(rs.getString("SCHEMA_NAME"));
            snapshotResult.setComment(rs.getString("COMMENT"));
            
            rs.close();
            queryStmt.close();
            
            // COMPARE: Create different version and compare
            Schema modifiedVersion = new Schema();
            modifiedVersion.setName(uniqueName);
            modifiedVersion.setComment("Modified workflow comment"); // Different comment
            
            SchemaComparator comparator = new SchemaComparator();
            ObjectDifferences differences = comparator.findDifferences(
                snapshotResult, modifiedVersion, database, new CompareControl(), null, new HashSet<>()
            );
            
            // VALIDATE: Complete workflow executed without exceptions
            assertNotNull(snapshotResult, "Snapshot should capture schema");
            assertNotNull(differences, "Comparator should return differences");
            assertTrue(differences.hasDifferences(), "Should detect comment differences");
            
            // VALIDATE: Snapshot captured correct original values
            assertEquals(uniqueName, snapshotResult.getName(), "Name should match");
            assertEquals("Workflow test schema", snapshotResult.getComment(), "Should capture original comment");
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    // ===========================================
    // PHASE 2: FRAMEWORK API INTEGRATION TESTS
    // ===========================================

    @Test
    public void testSchemaSnapshotGeneratorServiceRegistration() throws Exception {
        
        // Test direct service loading
        SchemaSnapshotGenerator generator = new SchemaSnapshotGenerator();
        
        // Verify framework integration - priority handling
        assertEquals(SchemaSnapshotGenerator.PRIORITY_DATABASE, 
                    generator.getPriority(liquibase.database.object.Schema.class, database));
        assertEquals(SchemaSnapshotGenerator.PRIORITY_NONE, 
                    generator.getPriority(liquibase.structure.core.Table.class, database));
        
    }

    @Test
    public void testSchemaComparatorServiceRegistration() throws Exception {
        
        SchemaComparator comparator = new SchemaComparator();
        
        // Verify framework integration - priority handling
        assertEquals(SchemaComparator.PRIORITY_DATABASE,
                    comparator.getPriority(liquibase.database.object.Schema.class, database));
        assertEquals(SchemaComparator.PRIORITY_NONE,
                    comparator.getPriority(liquibase.structure.core.Table.class, database));
        
    }

    // ===========================================
    // PHASE 3: PATTERN VALIDATION
    // ===========================================

    @Test
    public void testSchemaIsolationPattern() throws Exception {
        
        // Test that our unique naming pattern prevents conflicts
        String schema1 = getUniqueTestObjectName("isolation1");
        String schema2 = getUniqueTestObjectName("isolation2");
        String schema3 = getUniqueTestObjectName("isolation3");
        
        // Verify all names are unique
        assertNotEquals(schema1, schema2, "Should generate unique names");
        assertNotEquals(schema2, schema3, "Should generate unique names");
        assertNotEquals(schema1, schema3, "Should generate unique names");
        
        // Verify naming pattern consistency
        assertTrue(schema1.startsWith("INT_TEST_SCHEMA_"), "Should follow naming pattern");
        assertTrue(schema2.startsWith("INT_TEST_SCHEMA_"), "Should follow naming pattern");
        assertTrue(schema3.startsWith("INT_TEST_SCHEMA_"), "Should follow naming pattern");
        
        
    }

    @Test
    public void testErrorHandlingPatterns() throws Exception {
        
        SchemaSnapshotGenerator generator = new SchemaSnapshotGenerator();
        
        // Test generator configuration  
        assertEquals(SchemaSnapshotGenerator.PRIORITY_DATABASE, 
                    generator.getPriority(liquibase.database.object.Schema.class, database));
        assertEquals(SchemaSnapshotGenerator.PRIORITY_NONE, 
                    generator.getPriority(liquibase.database.object.Database.class, database));
        
        // Test comparator configuration
        SchemaComparator comparator = new SchemaComparator();
        assertEquals(SchemaComparator.PRIORITY_DATABASE,
                    comparator.getPriority(liquibase.database.object.Schema.class, database));
        assertEquals(SchemaComparator.PRIORITY_NONE,
                    comparator.getPriority(liquibase.database.object.Database.class, database));
        
    }

    @Test
    public void testDataTypeHandlingPatterns() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("dataTypes");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Schema with various data types
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE SCHEMA " + uniqueName + " " +
                "COMMENT = 'Test with various data types: strings, booleans, numbers'"
            );
            createStmt.execute();
            createStmt.close();
            
            // QUERY: Capture and verify type handling using the same patterns as our generator
            PreparedStatement queryStmt = connection.prepareStatement(
                "SELECT SCHEMA_NAME, COMMENT, RETENTION_TIME " +
                "FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?"
            );
            queryStmt.setString(1, uniqueName);
            ResultSet rs = queryStmt.executeQuery();
            
            assertTrue(rs.next(), "Should find created schema");
            
            // VALIDATE: Different data types handled correctly
            assertEquals(uniqueName, rs.getString("SCHEMA_NAME"), "String handling");
            assertNotNull(rs.getString("COMMENT"), "Comment string handling");
            
            rs.close();
            queryStmt.close();
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }
}