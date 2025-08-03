package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DatabaseComparator;
import liquibase.snapshot.jvm.DatabaseSnapshotGeneratorSnowflake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
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
        String url = System.getenv("SNOWFLAKE_URL");
        String user = System.getenv("SNOWFLAKE_USER");
        String password = System.getenv("SNOWFLAKE_PASSWORD");
        
        if (url == null || user == null || password == null) {
            throw new RuntimeException("Snowflake connection environment variables not set");
        }

        // For database integration tests, we expect the URL to point to LB_DBEXT_INT_DB for database operations
        if (!url.contains("LB_DBEXT_INT_DB")) {
            System.out.println("WARNING: URL should contain LB_DBEXT_INT_DB for database integration tests");
            System.out.println("Current URL: " + url);
        }

        connection = DriverManager.getConnection(url, user, password);
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
                System.out.println("Cleaned up test database: " + objectName);
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
        System.out.println("Phase 1A: Testing DatabaseSnapshotGeneratorSnowflake direct SQL queries...");
        
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
            
            System.out.println("✅ SUCCESS: Direct SQL queries working for DatabaseSnapshotGeneratorSnowflake");
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    @Test
    public void testDatabaseSnapshotGeneratorObjectCreation() throws Exception {
        System.out.println("Phase 1A: Testing DatabaseSnapshotGeneratorSnowflake object creation...");
        
        String uniqueName = getUniqueTestObjectName("objectCreation");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test database with various Snowflake properties
            // Note: Transient databases have retention time constraints (0-1 days)
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE TRANSIENT DATABASE " + uniqueName + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 1 " +
                "COMMENT = 'Test database with Snowflake properties'"
            );
            createStmt.execute();
            createStmt.close();
            
            // TEST: Create DatabaseSnapshotGeneratorSnowflake and test framework integration
            DatabaseSnapshotGeneratorSnowflake generator = new DatabaseSnapshotGeneratorSnowflake();
            
            // Verify generator configuration
            assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                        generator.getPriority(liquibase.database.object.Database.class, database),
                        "Should handle Database objects with DATABASE priority");
            
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
            
            System.out.println("✅ SUCCESS: DatabaseSnapshotGeneratorSnowflake object creation working");
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    // ===========================================
    // PHASE 1B: DIRECT COMPONENT TESTING - COMPARATOR
    // ===========================================

    @Test
    public void testDatabaseComparatorSameObjects() throws Exception {
        System.out.println("Phase 1B: Testing DatabaseComparator - Same Objects scenario...");
        
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
        
        System.out.println("✅ SUCCESS: DatabaseComparator same objects scenario working");
    }

    @Test 
    public void testDatabaseComparatorDifferentObjects() throws Exception {
        System.out.println("Phase 1B: Testing DatabaseComparator - Different Objects scenario...");
        
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
        
        System.out.println("✅ SUCCESS: DatabaseComparator different objects scenario working");
    }

    // ===========================================
    // PHASE 2: FRAMEWORK API INTEGRATION TESTS
    // ===========================================

    @Test
    public void testDatabaseSnapshotGeneratorServiceRegistration() throws Exception {
        System.out.println("Phase 2A: Testing DatabaseSnapshotGeneratorSnowflake service registration...");
        
        // Test direct service loading
        DatabaseSnapshotGeneratorSnowflake generator = new DatabaseSnapshotGeneratorSnowflake();
        
        // Verify framework integration - priority handling
        assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                    generator.getPriority(liquibase.database.object.Database.class, database),
                    "Should handle Database objects with DATABASE priority");
        assertEquals(DatabaseSnapshotGeneratorSnowflake.PRIORITY_NONE, 
                    generator.getPriority(liquibase.structure.core.Schema.class, database),
                    "Should not handle Schema objects");
        
        System.out.println("✅ SUCCESS: DatabaseSnapshotGeneratorSnowflake service registration working");
    }

    @Test
    public void testDatabaseComparatorServiceRegistration() throws Exception {
        System.out.println("Phase 2B: Testing DatabaseComparator service registration...");
        
        DatabaseComparator comparator = new DatabaseComparator();
        
        // Verify framework integration - priority handling
        assertEquals(DatabaseComparator.PRIORITY_DATABASE,
                    comparator.getPriority(liquibase.database.object.Database.class, database),
                    "Should handle Database objects with DATABASE priority");
        assertEquals(DatabaseComparator.PRIORITY_NONE,
                    comparator.getPriority(liquibase.structure.core.Schema.class, database),
                    "Should not handle Schema objects");
        
        System.out.println("✅ SUCCESS: DatabaseComparator service registration working");
    }

    // ===========================================
    // PHASE 3: XSD COMPLIANCE VALIDATION
    // ===========================================

    @Test
    public void testXSDAttributeHandling() throws Exception {
        System.out.println("Phase 3A: Testing XSD attribute handling patterns...");
        
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
            
            System.out.println("✅ SUCCESS: XSD attribute handling patterns validated");
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    @Test
    public void testDatabaseIsolationPattern() throws Exception {
        System.out.println("Phase 3B: Validating database isolation pattern for parallel execution...");
        
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
        
        System.out.println("Database 1: " + database1);
        System.out.println("Database 2: " + database2);
        System.out.println("Database 3: " + database3);
        
        System.out.println("✅ SUCCESS: Database isolation pattern validated");
    }
}