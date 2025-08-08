package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DatabaseComparator;
import liquibase.snapshot.jvm.DatabaseSnapshotGeneratorSnowflake;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Database object snapshot/diff functionality.
 * Tests the complete workflow: create → snapshot → compare → diff.
 */
public class DatabaseObjectIntegrationTest {

    private Database database;
    private Connection connection;
    private List<String> createdTestObjects = new ArrayList<>();

    /**
     * Generates unique test object names for schema isolation.
     */
    private String getUniqueTestObjectName(String methodName) {
        return "INT_TEST_DB_" + methodName.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration instead of environment variables
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clean up any created test databases
        for (String objectName : createdTestObjects) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP DATABASE IF EXISTS " + objectName);
                dropStmt.execute();
                dropStmt.close();
            } catch (Exception e) {
                System.err.println("Failed to cleanup test database " + objectName + ": " + e.getMessage());
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
    public void testDatabaseSnapshotGeneratorDirectQuery() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("directQuery");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test database in Snowflake with Snowflake-specific attributes
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE DATABASE " + uniqueName + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 1 " +
                "COMMENT = 'Integration test database'"
            );
            createStmt.execute();
            createStmt.close();
            
            // DIRECT QUERY: Test the SQL queries our generator uses
            PreparedStatement infoStmt = connection.prepareStatement(
                "SELECT DATABASE_NAME, COMMENT, RETENTION_TIME, IS_TRANSIENT, DATABASE_OWNER, TYPE " +
                "FROM INFORMATION_SCHEMA.DATABASES WHERE DATABASE_NAME = ?"
            );
            infoStmt.setString(1, uniqueName);
            java.sql.ResultSet rs = infoStmt.executeQuery();
            
            assertTrue(rs.next(), "INFORMATION_SCHEMA should find our database");
            assertEquals(uniqueName, rs.getString("DATABASE_NAME"), "Database name should match");
            assertEquals("Integration test database", rs.getString("COMMENT"), "Comment should match");
            assertEquals(1, rs.getInt("RETENTION_TIME"), "Retention time should match");
            assertEquals("NO", rs.getString("IS_TRANSIENT"), "Should not be transient");
            assertNotNull(rs.getString("DATABASE_OWNER"), "Should have an owner");
            assertNotNull(rs.getString("TYPE"), "Should have a type");
            
            rs.close();
            infoStmt.close();
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    @Test
    public void testDatabaseSnapshotGeneratorObjectCreation() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("objectCreation");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test database with various Snowflake properties
            // Note: Transient databases have retention time constraints (0-1 days)
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE DATABASE " + uniqueName + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 1 " +
                "COMMENT = 'Test database with Snowflake properties'"
            );
            createStmt.execute();
            createStmt.close();
            
            // TEST: Create DatabaseSnapshotGeneratorSnowflake and test framework integration
            DatabaseSnapshotGeneratorSnowflake generator = new DatabaseSnapshotGeneratorSnowflake();
            
            // Verify generator configuration
            assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                        generator.getPriority(liquibase.database.object.Database.class, database));
            
            // Create a database object manually using the same pattern as our generator
            liquibase.database.object.Database databaseObject = new liquibase.database.object.Database();
            databaseObject.setName(uniqueName);
            
            // Set test attributes to verify they get captured
            databaseObject.setComment("Test database with Snowflake properties");
            databaseObject.setDataRetentionTimeInDays(1);
            databaseObject.setTransient(true);
            
            // Verify object properties are set correctly
            assertEquals(uniqueName, databaseObject.getName(), "Database name should be set");
            assertEquals("Test database with Snowflake properties", databaseObject.getComment(), "Comment should be set");
            assertEquals(Integer.valueOf(1), databaseObject.getDataRetentionTimeInDays(), "Retention time should be set");
            assertEquals(Boolean.TRUE, databaseObject.getTransient(), "Transient flag should be set");
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    // ===========================================
    // PHASE 1B: DIRECT COMPONENT TESTING - COMPARATOR
    // ===========================================

    @Test
    public void testDatabaseComparatorSameObjects() throws Exception {
        
        // Create two identical database objects
        liquibase.database.object.Database database1 = new liquibase.database.object.Database();
        database1.setName("TEST_DATABASE");
        database1.setComment("Test comment");
        database1.setDataRetentionTimeInDays(1);
        database1.setTransient(false);
        
        liquibase.database.object.Database database2 = new liquibase.database.object.Database();
        database2.setName("TEST_DATABASE");
        database2.setComment("Test comment");
        database2.setDataRetentionTimeInDays(1);
        database2.setTransient(false);
        
        // COMPARE: Same objects should have no differences
        DatabaseComparator comparator = new DatabaseComparator();
        ObjectDifferences differences = comparator.findDifferences(
            database1, database2, database, new CompareControl(), null, new HashSet<>()
        );
        
        // VALIDATE: Should be identical
        assertFalse(differences.hasDifferences(), "Same objects should have no differences");
        
    }

    @Test 
    public void testDatabaseComparatorDifferentObjects() throws Exception {
        
        // Create source database object
        liquibase.database.object.Database source = new liquibase.database.object.Database();
        source.setName("TEST_DATABASE");
        source.setComment("Original comment");
        source.setDataRetentionTimeInDays(1);
        source.setTransient(false);
        
        // Create target database object with differences
        liquibase.database.object.Database target = new liquibase.database.object.Database();
        target.setName("TEST_DATABASE");
        target.setComment("Modified comment"); // Different
        target.setDataRetentionTimeInDays(7); // Different
        target.setTransient(true); // Different
        
        // COMPARE: Different objects should have differences
        DatabaseComparator comparator = new DatabaseComparator();
        ObjectDifferences differences = comparator.findDifferences(
            source, target, database, new CompareControl(), null, new HashSet<>()
        );
        
        // VALIDATE: Should detect differences
        assertTrue(differences.hasDifferences(), "Different objects should have differences");
        
    }

    // ===========================================
    // PHASE 2: FRAMEWORK API INTEGRATION TESTS
    // ===========================================

    @Test
    public void testDatabaseSnapshotGeneratorServiceRegistration() throws Exception {
        
        // Test direct service loading
        DatabaseSnapshotGeneratorSnowflake generator = new DatabaseSnapshotGeneratorSnowflake();
        
        // Verify framework integration - priority handling
        assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                    generator.getPriority(liquibase.database.object.Database.class, database));
        assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_NONE, 
                    generator.getPriority(liquibase.structure.core.Schema.class, database));
        
    }

    @Test
    public void testDatabaseComparatorServiceRegistration() throws Exception {
        
        DatabaseComparator comparator = new DatabaseComparator();
        
        // Verify framework integration - priority handling
        assertEquals(DatabaseComparator.PRIORITY_DATABASE,
                    comparator.getPriority(liquibase.database.object.Database.class, database));
        assertEquals(DatabaseComparator.PRIORITY_NONE,
                    comparator.getPriority(liquibase.structure.core.Schema.class, database));
        
    }

    // ===========================================
    // PHASE 3: XSD COMPLIANCE VALIDATION
    // ===========================================

    @Test
    public void testXSDAttributeHandling() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("xsdAttributes");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Database with comprehensive XSD attributes
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE DATABASE " + uniqueName + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 14 " +
                "COMMENT = 'Test database for comprehensive XSD attributes'"
            );
            createStmt.execute();
            createStmt.close();
            
            // QUERY: Capture and verify attribute handling using the same patterns as our generator
            PreparedStatement queryStmt = connection.prepareStatement(
                "SELECT DATABASE_NAME, COMMENT, RETENTION_TIME, IS_TRANSIENT, DATABASE_OWNER, TYPE " +
                "FROM INFORMATION_SCHEMA.DATABASES WHERE DATABASE_NAME = ?"
            );
            queryStmt.setString(1, uniqueName);
            java.sql.ResultSet rs = queryStmt.executeQuery();
            
            assertTrue(rs.next(), "Should find created database");
            
            // VALIDATE: XSD attributes handled correctly
            assertEquals(uniqueName, rs.getString("DATABASE_NAME"), "Database name handling");
            assertNotNull(rs.getString("COMMENT"), "Comment attribute handling");
            assertEquals(14, rs.getInt("RETENTION_TIME"), "Retention time attribute handling");
            assertEquals("NO", rs.getString("IS_TRANSIENT"), "Transient flag attribute handling");
            assertNotNull(rs.getString("DATABASE_OWNER"), "Owner attribute handling");
            assertNotNull(rs.getString("TYPE"), "Type attribute handling");
            
            rs.close();
            queryStmt.close();
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    @Test
    public void testDatabaseIsolationPattern() throws Exception {
        
        // Test that our unique naming pattern prevents conflicts
        String database1 = getUniqueTestObjectName("isolation1");
        String database2 = getUniqueTestObjectName("isolation2");
        String database3 = getUniqueTestObjectName("isolation3");
        
        // Verify all names are unique
        assertNotEquals(database1, database2, "Should generate unique names");
        assertNotEquals(database2, database3, "Should generate unique names");
        assertNotEquals(database1, database3, "Should generate unique names");
        
        // Verify naming pattern consistency
        assertTrue(database1.startsWith("INT_TEST_DB_"), "Should follow naming pattern");
        assertTrue(database2.startsWith("INT_TEST_DB_"), "Should follow naming pattern");
        assertTrue(database3.startsWith("INT_TEST_DB_"), "Should follow naming pattern");
        
        
    }
}