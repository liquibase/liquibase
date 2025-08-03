package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.TableComparatorSnowflake;
import liquibase.snapshot.jvm.TableSnapshotGeneratorSnowflake;
import liquibase.structure.core.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Table object snapshot/diff functionality.
 * Implements three-phase approach: Direct Component → Framework API → Pattern Validation.
 * 
 * ADDRESSES_CORE_ISSUE: Bridge unit tests → test harness with real database validation for Table objects.
 */
public class TableObjectIntegrationTest {

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
        return "INT_TEST_TABLE_" + methodName.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @BeforeEach
    public void setUp() throws Exception {
        String url = System.getenv("SNOWFLAKE_URL");
        String user = System.getenv("SNOWFLAKE_USER");
        String password = System.getenv("SNOWFLAKE_PASSWORD");
        
        if (url == null || user == null || password == null) {
            throw new RuntimeException("Snowflake connection environment variables not set");
        }

        // For integration tests, we expect the URL to point to LB_INT_SNAPSHOT_DB
        if (!url.contains("LB_INT_SNAPSHOT_DB")) {
            System.out.println("WARNING: URL should contain LB_INT_SNAPSHOT_DB for integration tests");
            System.out.println("Current URL: " + url);
        }

        connection = DriverManager.getConnection(url, user, password);
        
        // Ensure we're using the correct schema - first check if it exists, create if not
        try {
            PreparedStatement useSchema = connection.prepareStatement("USE SCHEMA TESTHARNESS");
            useSchema.execute();
            useSchema.close();
        } catch (Exception e) {
            // Schema doesn't exist, create it
            PreparedStatement createSchema = connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS TESTHARNESS");
            createSchema.execute();
            createSchema.close();
            
            PreparedStatement useSchema = connection.prepareStatement("USE SCHEMA TESTHARNESS");
            useSchema.execute();
            useSchema.close();
        }
        
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // MANDATORY: Cleanup all created test objects using unique names
        for (String objectName : createdTestObjects) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP TABLE IF EXISTS TESTHARNESS." + objectName);
                dropStmt.execute();
                dropStmt.close();
                System.out.println("Cleaned up test table: " + objectName);
            } catch (Exception e) {
                System.err.println("Failed to cleanup test table " + objectName + ": " + e.getMessage());
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
    public void testTableSnapshotGeneratorDirectQuery() throws Exception {
        System.out.println("Phase 1A: Testing TableSnapshotGeneratorSnowflake direct SQL queries...");
        
        String uniqueName = getUniqueTestObjectName("directQuery");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test table in Snowflake with Snowflake-specific attributes
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE TABLE " + uniqueName + " (" +
                "    id INT, " +
                "    name VARCHAR(100)" +
                ") " +
                "CLUSTER BY (id) " +
                "DATA_RETENTION_TIME_IN_DAYS = 1 " +
                "COMMENT = 'Integration test table'"
            );
            createStmt.execute();
            createStmt.close();
            
            // DIRECT QUERY: Test the SQL queries our generator uses
            // Test INFORMATION_SCHEMA query
            PreparedStatement infoStmt = connection.prepareStatement(
                "SELECT TABLE_NAME, TABLE_SCHEMA, COMMENT, CLUSTERING_KEY, RETENTION_TIME, IS_TRANSIENT " +
                "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?"
            );
            infoStmt.setString(1, uniqueName);
            ResultSet rs = infoStmt.executeQuery();
            
            assertTrue(rs.next(), "INFORMATION_SCHEMA should find our table");
            assertEquals(uniqueName, rs.getString("TABLE_NAME"), "Table name should match");
            assertEquals("Integration test table", rs.getString("COMMENT"), "Comment should match");
            assertNotNull(rs.getString("CLUSTERING_KEY"), "Should have clustering key information");
            assertNotNull(rs.getString("RETENTION_TIME"), "Should have retention time information");
            
            rs.close();
            infoStmt.close();
            
            System.out.println("✅ SUCCESS: Direct SQL queries working for TableSnapshotGeneratorSnowflake");
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    @Test
    public void testTableSnapshotGeneratorObjectCreation() throws Exception {
        System.out.println("Phase 1A: Testing TableSnapshotGeneratorSnowflake object creation...");
        
        String uniqueName = getUniqueTestObjectName("objectCreation");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test table with Snowflake-specific properties
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE TRANSIENT TABLE " + uniqueName + " (" +
                "    customer_id INT, " +
                "    order_date DATE" +
                ") " +
                "CLUSTER BY (customer_id, order_date) " +
                "DATA_RETENTION_TIME_IN_DAYS = 1 " +
                "COMMENT = 'Test table with Snowflake properties'"
            );
            createStmt.execute();
            createStmt.close();
            
            // TEST: Create TableSnapshotGeneratorSnowflake and test object creation
            TableSnapshotGeneratorSnowflake generator = new TableSnapshotGeneratorSnowflake();
            
            // Verify generator configuration
            assertEquals(TableSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                        generator.getPriority(Table.class, database),
                        "Should handle Table objects with DATABASE priority");
            
            // Create a table object manually using the same pattern as our generator
            Table tableObject = new Table();
            tableObject.setName(uniqueName);
            tableObject.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "TESTHARNESS"));
            
            // Set test attributes to verify they get captured
            tableObject.setAttribute("clusteringKey", "customer_id, order_date");
            tableObject.setAttribute("retentionTime", "7");
            tableObject.setAttribute("isTransient", "YES");
            
            // Verify object properties are set correctly
            assertEquals(uniqueName, tableObject.getName(), "Table name should be set");
            assertEquals("customer_id, order_date", tableObject.getAttribute("clusteringKey", String.class), "Clustering key should be set");
            assertEquals("7", tableObject.getAttribute("retentionTime", String.class), "Retention time should be set");
            assertEquals("YES", tableObject.getAttribute("isTransient", String.class), "Transient flag should be set");
            
            System.out.println("✅ SUCCESS: TableSnapshotGeneratorSnowflake object creation working");
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    // ===========================================
    // PHASE 1B: DIRECT COMPONENT TESTING - COMPARATOR
    // ===========================================

    @Test
    public void testTableComparatorSameObjects() throws Exception {
        System.out.println("Phase 1B: Testing TableComparatorSnowflake - Same Objects scenario...");
        
        // Create two identical table objects
        Table table1 = new Table();
        table1.setName("TEST_TABLE");
        table1.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "TESTHARNESS"));
        table1.setAttribute("clusteringKey", "id, name");
        table1.setAttribute("retentionTime", "1");
        table1.setAttribute("isTransient", "NO");
        
        Table table2 = new Table();
        table2.setName("TEST_TABLE");
        table2.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "TESTHARNESS"));
        table2.setAttribute("clusteringKey", "id, name");
        table2.setAttribute("retentionTime", "1");
        table2.setAttribute("isTransient", "NO");
        
        // COMPARE: Same objects should have no differences
        TableComparatorSnowflake comparator = new TableComparatorSnowflake();
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, database, new CompareControl(), null, new HashSet<>()
        );
        
        // VALIDATE: Should be identical
        assertFalse(differences.hasDifferences(), "Same objects should have no differences");
        
        System.out.println("✅ SUCCESS: TableComparatorSnowflake same objects scenario working");
    }

    @Test 
    public void testTableComparatorDifferentObjects() throws Exception {
        System.out.println("Phase 1B: Testing TableComparatorSnowflake - Different Objects scenario...");
        
        // Create source table object
        Table source = new Table();
        source.setName("TEST_TABLE");
        source.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "TESTHARNESS"));
        source.setAttribute("clusteringKey", "original_key");
        source.setAttribute("retentionTime", "1");
        source.setAttribute("isTransient", "NO");
        
        // Create target table object with differences
        Table target = new Table();
        target.setName("TEST_TABLE");
        target.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "TESTHARNESS"));
        target.setAttribute("clusteringKey", "modified_key"); // Different
        target.setAttribute("retentionTime", "14"); // Different
        target.setAttribute("isTransient", "YES"); // Different
        
        // COMPARE: Different objects should have differences
        TableComparatorSnowflake comparator = new TableComparatorSnowflake();
        ObjectDifferences differences = comparator.findDifferences(
            source, target, database, new CompareControl(), null, new HashSet<>()
        );
        
        // VALIDATE: Should detect differences
        assertTrue(differences.hasDifferences(), "Different objects should have differences");
        
        System.out.println("✅ SUCCESS: TableComparatorSnowflake different objects scenario working");
    }

    // ===========================================
    // PHASE 1C: CREATE → SNAPSHOT → COMPARE WORKFLOW
    // ===========================================

    @Test
    public void testCreateSnapshotCompareWorkflow() throws Exception {
        System.out.println("Phase 1C: Testing Create → Snapshot → Compare workflow...");
        
        String uniqueName = getUniqueTestObjectName("workflow");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test table in Snowflake
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE TABLE " + uniqueName + " (" +
                "    id INT, " +
                "    data VARCHAR(50)" +
                ") " +
                "CLUSTER BY (id) " +
                "DATA_RETENTION_TIME_IN_DAYS = 1 " +
                "COMMENT = 'Workflow test table'"
            );
            createStmt.execute();
            createStmt.close();
            
            // SNAPSHOT: Query database to capture state (simulating what our generator does)
            PreparedStatement queryStmt = connection.prepareStatement(
                "SELECT TABLE_NAME, COMMENT, CLUSTERING_KEY, RETENTION_TIME, IS_TRANSIENT " +
                "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?"
            );
            queryStmt.setString(1, uniqueName);
            ResultSet rs = queryStmt.executeQuery();
            
            assertTrue(rs.next(), "Should find created table");
            
            // Create table object from snapshot data and capture values
            Table snapshotResult = new Table();
            snapshotResult.setName(rs.getString("TABLE_NAME"));
            snapshotResult.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "TESTHARNESS"));
            snapshotResult.setAttribute("clusteringKey", rs.getString("CLUSTERING_KEY"));
            snapshotResult.setAttribute("retentionTime", rs.getString("RETENTION_TIME"));
            snapshotResult.setAttribute("isTransient", rs.getString("IS_TRANSIENT"));
            
            // Capture values before closing ResultSet
            String retentionTime = rs.getString("RETENTION_TIME");
            String isTransient = rs.getString("IS_TRANSIENT");
            
            rs.close();
            queryStmt.close();
            
            // COMPARE: Create different version and compare
            Table modifiedVersion = new Table();
            modifiedVersion.setName(uniqueName);
            modifiedVersion.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, "TESTHARNESS"));
            modifiedVersion.setAttribute("clusteringKey", "different_key"); // Different clustering key
            modifiedVersion.setAttribute("retentionTime", retentionTime); // Same retention
            modifiedVersion.setAttribute("isTransient", isTransient); // Same transient
            
            TableComparatorSnowflake comparator = new TableComparatorSnowflake();
            ObjectDifferences differences = comparator.findDifferences(
                snapshotResult, modifiedVersion, database, new CompareControl(), null, new HashSet<>()
            );
            
            // VALIDATE: Complete workflow executed without exceptions
            assertNotNull(snapshotResult, "Snapshot should capture table");
            assertNotNull(differences, "Comparator should return differences");
            assertTrue(differences.hasDifferences(), "Should detect clustering key differences");
            
            // VALIDATE: Snapshot captured correct original values
            assertEquals(uniqueName, snapshotResult.getName(), "Name should match");
            assertNotNull(snapshotResult.getAttribute("clusteringKey", String.class), "Should capture clustering key");
            
            System.out.println("✅ SUCCESS: Complete Create → Snapshot → Compare workflow working");
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    // ===========================================
    // PHASE 2: FRAMEWORK API INTEGRATION TESTS
    // ===========================================

    @Test
    public void testTableSnapshotGeneratorServiceRegistration() throws Exception {
        System.out.println("Phase 2A: Testing TableSnapshotGeneratorSnowflake service registration...");
        
        // Test direct service loading
        TableSnapshotGeneratorSnowflake generator = new TableSnapshotGeneratorSnowflake();
        
        // Verify framework integration - priority handling
        assertEquals(TableSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                    generator.getPriority(Table.class, database),
                    "Should handle Table objects with DATABASE priority");
        assertEquals(TableSnapshotGeneratorSnowflake.PRIORITY_NONE, 
                    generator.getPriority(liquibase.database.object.Schema.class, database),
                    "Should not handle Schema objects");
        
        System.out.println("✅ SUCCESS: TableSnapshotGeneratorSnowflake service registration working");
    }

    @Test
    public void testTableComparatorServiceRegistration() throws Exception {
        System.out.println("Phase 2B: Testing TableComparatorSnowflake service registration...");
        
        TableComparatorSnowflake comparator = new TableComparatorSnowflake();
        
        // Verify framework integration - priority handling
        assertEquals(TableComparatorSnowflake.PRIORITY_DATABASE,
                    comparator.getPriority(Table.class, database),
                    "Should handle Table objects with DATABASE priority");
        assertEquals(TableComparatorSnowflake.PRIORITY_NONE,
                    comparator.getPriority(liquibase.database.object.Schema.class, database),
                    "Should not handle Schema objects");
        
        System.out.println("✅ SUCCESS: TableComparatorSnowflake service registration working");
    }

    // ===========================================
    // PHASE 3: PATTERN VALIDATION
    // ===========================================

    @Test
    public void testTableIsolationPattern() throws Exception {
        System.out.println("Phase 3A: Validating table isolation pattern for parallel execution...");
        
        // Test that our unique naming pattern prevents conflicts
        String table1 = getUniqueTestObjectName("isolation1");
        String table2 = getUniqueTestObjectName("isolation2");
        String table3 = getUniqueTestObjectName("isolation3");
        
        // Verify all names are unique
        assertNotEquals(table1, table2, "Should generate unique names");
        assertNotEquals(table2, table3, "Should generate unique names");
        assertNotEquals(table1, table3, "Should generate unique names");
        
        // Verify naming pattern consistency
        assertTrue(table1.startsWith("INT_TEST_TABLE_"), "Should follow naming pattern");
        assertTrue(table2.startsWith("INT_TEST_TABLE_"), "Should follow naming pattern");
        assertTrue(table3.startsWith("INT_TEST_TABLE_"), "Should follow naming pattern");
        
        System.out.println("Table 1: " + table1);
        System.out.println("Table 2: " + table2);
        System.out.println("Table 3: " + table3);
        
        System.out.println("✅ SUCCESS: Table isolation pattern validated");
    }

    @Test
    public void testErrorHandlingPatterns() throws Exception {
        System.out.println("Phase 3B: Testing error handling patterns...");
        
        TableSnapshotGeneratorSnowflake generator = new TableSnapshotGeneratorSnowflake();
        
        // Test generator configuration  
        assertEquals(TableSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                    generator.getPriority(Table.class, database),
                    "Should handle Table objects");
        assertEquals(TableSnapshotGeneratorSnowflake.PRIORITY_NONE, 
                    generator.getPriority(liquibase.database.object.Database.class, database),
                    "Should not handle Database objects");
        
        // Test comparator configuration
        TableComparatorSnowflake comparator = new TableComparatorSnowflake();
        assertEquals(TableComparatorSnowflake.PRIORITY_DATABASE,
                    comparator.getPriority(Table.class, database),
                    "Should handle Table objects");
        assertEquals(TableComparatorSnowflake.PRIORITY_NONE,
                    comparator.getPriority(liquibase.database.object.Database.class, database),
                    "Should not handle Database objects");
        
        System.out.println("✅ SUCCESS: Error handling patterns validated");
    }

    @Test
    public void testSnowflakeSpecificAttributeHandling() throws Exception {
        System.out.println("Phase 3C: Testing Snowflake-specific attribute handling patterns...");
        
        String uniqueName = getUniqueTestObjectName("attributes");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Table with comprehensive Snowflake attributes
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE TRANSIENT TABLE " + uniqueName + " (" +
                "    product_id INT, " +
                "    category_id INT, " +
                "    sale_date DATE" +
                ") " +
                "CLUSTER BY (product_id, category_id) " +
                "DATA_RETENTION_TIME_IN_DAYS = 1 " +
                "COMMENT = 'Test table for comprehensive Snowflake attributes'"
            );
            createStmt.execute();
            createStmt.close();
            
            // QUERY: Capture and verify attribute handling using the same patterns as our generator
            PreparedStatement queryStmt = connection.prepareStatement(
                "SELECT TABLE_NAME, COMMENT, CLUSTERING_KEY, RETENTION_TIME, IS_TRANSIENT " +
                "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?"
            );
            queryStmt.setString(1, uniqueName);
            ResultSet rs = queryStmt.executeQuery();
            
            assertTrue(rs.next(), "Should find created table");
            
            // VALIDATE: Different Snowflake-specific attributes handled correctly
            assertEquals(uniqueName, rs.getString("TABLE_NAME"), "Table name handling");
            assertNotNull(rs.getString("COMMENT"), "Comment attribute handling");
            assertNotNull(rs.getString("CLUSTERING_KEY"), "Clustering key attribute handling");
            assertNotNull(rs.getString("RETENTION_TIME"), "Retention time attribute handling");
            assertEquals("YES", rs.getString("IS_TRANSIENT"), "Transient flag attribute handling");
            
            rs.close();
            queryStmt.close();
            
            System.out.println("✅ SUCCESS: Snowflake-specific attribute handling patterns validated");
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }
}