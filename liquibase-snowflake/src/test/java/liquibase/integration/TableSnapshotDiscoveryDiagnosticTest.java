package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.Column;
import liquibase.structure.DatabaseObject;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple diagnostic test to isolate the exact failure point in table snapshot discovery.
 * This replicates the TableFullCycleIntegrationTest logic step-by-step with extensive debugging.
 */
@DisplayName("Table Snapshot Discovery Diagnostic Test")
public class TableSnapshotDiscoveryDiagnosticTest {

    private Database database;
    private Connection connection;
    private String testId = String.valueOf(System.currentTimeMillis());
    private String testSchema = "DIAG_SCHEMA_" + testId;
    
    @BeforeEach
    public void setUp() throws Exception {
        try {
            connection = TestDatabaseConfigUtil.getSnowflakeConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Create clean schema for testing
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE");
                stmt.execute("CREATE SCHEMA " + testSchema);
            }
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake or create schema: " + e.getMessage());
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE");
            } catch (Exception e) {
                System.err.println("Failed to cleanup schema: " + e.getMessage());
            }
            connection.close();
        }
    }
    
    @Test
    @DisplayName("Diagnose Table Snapshot Discovery Issue - Step by Step Analysis")
    public void diagnoseTableSnapshotDiscovery() throws Exception {
        
        System.out.println("🔍 DIAGNOSTIC TEST: Table Snapshot Discovery");
        System.out.println("==========================================");
        
        // STEP 1: Create a simple test table
        System.out.println("\n📝 STEP 1: Creating test table...");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE SCHEMA " + testSchema);
            stmt.execute("CREATE TABLE DIAGNOSTIC_TABLE (" +
                "ID NUMBER(10,0) PRIMARY KEY, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "CREATED_DATE DATE DEFAULT CURRENT_DATE()" +
            ")");
            System.out.println("✅ Table created successfully");
        }
        
        // STEP 2: Verify table exists with direct SQL
        System.out.println("\n🔍 STEP 2: Verifying table exists with direct SQL...");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE SCHEMA " + testSchema);
            ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'DIAGNOSTIC_%'");
            int count = 0;
            while (rs.next()) {
                count++;
                String tableName = rs.getString("name");
                String schema = rs.getString("schema_name");
                String database_name = rs.getString("database_name");
                System.out.println("  ✅ Found table: " + database_name + "." + schema + "." + tableName);
            }
            rs.close();
            
            if (count == 0) {
                System.out.println("  ❌ NO TABLES FOUND - Critical issue with table creation!");
                fail("Table creation failed - no tables found with SHOW TABLES");
            }
            System.out.println("  ✅ Direct SQL verification: " + count + " table(s) found");
        }
        
        // STEP 3: Check database connection and default schema
        System.out.println("\n⚙️ STEP 3: Analyzing database connection context...");
        System.out.println("  🎯 Database default catalog: " + database.getDefaultCatalogName());
        System.out.println("  🎯 Database default schema: " + database.getDefaultSchemaName());
        System.out.println("  🎯 Database class: " + database.getClass().getSimpleName());
        System.out.println("  🎯 Connection URL: " + connection.getMetaData().getURL());
        
        // STEP 4: Set database context to our test schema
        System.out.println("\n⚙️ STEP 4: Setting database context...");
        database.setDefaultSchemaName(testSchema);
        System.out.println("  ✅ Database default schema set to: " + database.getDefaultSchemaName());
        
        // STEP 5: Create SnapshotControl and analyze
        System.out.println("\n📊 STEP 5: Creating SnapshotControl...");
        SnapshotControl snapshotControl = new SnapshotControl(database, Table.class, Column.class);
        System.out.println("  ✅ SnapshotControl created with types: " + snapshotControl.getTypesToInclude());
        
        // STEP 6: Create Schema object and analyze
        System.out.println("\n🏗️ STEP 6: Creating Schema object...");
        Schema schemaObject = new Schema(database.getDefaultCatalogName(), testSchema);
        System.out.println("  ✅ Schema object created: " + schemaObject.toString());
        System.out.println("  🎯 Schema catalog: " + schemaObject.getCatalogName());
        System.out.println("  🎯 Schema name: " + schemaObject.getName());
        
        // STEP 7: Generate snapshot and analyze results
        System.out.println("\n📸 STEP 7: Generating database snapshot...");
        
        try {
            DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(new DatabaseObject[]{schemaObject}, database, snapshotControl);
            
            System.out.println("  ✅ Snapshot generated successfully");
            
            // Analyze snapshot contents
            System.out.println("  🔍 Snapshot analysis:");
            System.out.println("    📋 Snapshot database: " + (snapshot.getDatabase() != null ? snapshot.getDatabase().getClass().getSimpleName() : "null"));
            
            // Check for tables
            Set<Table> tables = snapshot.get(Table.class);
            if (tables == null) {
                System.out.println("    ❌ Tables set is NULL");
            } else if (tables.isEmpty()) {
                System.out.println("    ❌ Tables set is EMPTY (size=0)");
            } else {
                System.out.println("    ✅ Tables found: " + tables.size());
                for (Table table : tables) {
                    System.out.println("      📋 Table: " + table.getName() + 
                        " (Schema: " + (table.getSchema() != null ? table.getSchema().getName() : "null") + 
                        ", Catalog: " + (table.getSchema() != null && table.getSchema().getCatalogName() != null ? table.getSchema().getCatalogName() : "null") + ")");
                }
            }
            
            // Check for columns
            Set<Column> columns = snapshot.get(Column.class);
            if (columns != null && !columns.isEmpty()) {
                System.out.println("    ✅ Columns found: " + columns.size());
            } else {
                System.out.println("    ⚠️ No columns found");
            }
            
            // STEP 8: Test alternative snapshot approaches
            System.out.println("\n🔄 STEP 8: Testing alternative snapshot approaches...");
            
            // Try with empty schema array
            System.out.println("  🧪 Testing snapshot with empty schema array...");
            try {
                DatabaseSnapshot altSnapshot1 = SnapshotGeneratorFactory.getInstance()
                    .createSnapshot(new DatabaseObject[0], database, snapshotControl);
                
                Set<Table> altTables1 = altSnapshot1.get(Table.class);
                if (altTables1 != null && !altTables1.isEmpty()) {
                    System.out.println("    ✅ Alternative approach 1 found " + altTables1.size() + " tables");
                    for (Table table : altTables1) {
                        if (table.getName().startsWith("DIAGNOSTIC_")) {
                            System.out.println("      ✅ Found our diagnostic table: " + table.getName());
                        }
                    }
                } else {
                    System.out.println("    ❌ Alternative approach 1: No tables found");
                }
            } catch (Exception e) {
                System.out.println("    ❌ Alternative approach 1 failed: " + e.getMessage());
            }
            
            // Try with catalog object
            System.out.println("  🧪 Testing snapshot with catalog object...");
            try {
                Catalog catalogObj = new Catalog(database.getDefaultCatalogName());
                DatabaseSnapshot altSnapshot2 = SnapshotGeneratorFactory.getInstance()
                    .createSnapshot(new DatabaseObject[]{catalogObj}, database, snapshotControl);
                
                Set<Table> altTables2 = altSnapshot2.get(Table.class);
                if (altTables2 != null && !altTables2.isEmpty()) {
                    System.out.println("    ✅ Alternative approach 2 found " + altTables2.size() + " tables");
                    for (Table table : altTables2) {
                        if (table.getName().startsWith("DIAGNOSTIC_")) {
                            System.out.println("      ✅ Found our diagnostic table: " + table.getName());
                        }
                    }
                } else {
                    System.out.println("    ❌ Alternative approach 2: No tables found");
                }
            } catch (Exception e) {
                System.out.println("    ❌ Alternative approach 2 failed: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("  ❌ Snapshot generation failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            fail("Snapshot generation failed: " + e.getMessage());
        }
        
        System.out.println("\n🎯 DIAGNOSTIC COMPLETE");
        System.out.println("====================");
    }
    
    @Test
    @DisplayName("Test Schema Name Variations - Case Sensitivity and Context")
    public void testSchemaNameVariations() throws Exception {
        
        System.out.println("🔍 TESTING SCHEMA NAME VARIATIONS");
        System.out.println("=================================");
        
        // Create test table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE SCHEMA " + testSchema);
            stmt.execute("CREATE TABLE SCHEMA_TEST_TABLE (ID NUMBER PRIMARY KEY, NAME VARCHAR(50))");
        }
        
        database.setDefaultSchemaName(testSchema);
        
        // Test different schema name formats
        String[] schemaVariations = {
            testSchema,                              // Original
            testSchema.toUpperCase(),                // Upper case
            testSchema.toLowerCase(),                // Lower case  
            "\"" + testSchema + "\"",               // Quoted
            database.getDefaultCatalogName() + "." + testSchema  // Fully qualified
        };
        
        for (String schemaName : schemaVariations) {
            System.out.println("\n📝 Testing schema variation: " + schemaName);
            try {
                Schema schemaObj = new Schema(database.getDefaultCatalogName(), schemaName);
                SnapshotControl control = new SnapshotControl(database, Table.class);
                
                DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
                    .createSnapshot(new DatabaseObject[]{schemaObj}, database, control);
                
                Set<Table> tables = snapshot.get(Table.class);
                if (tables != null && !tables.isEmpty()) {
                    System.out.println("  ✅ Found " + tables.size() + " table(s)");
                    for (Table table : tables) {
                        System.out.println("    📋 " + table.getName());
                    }
                } else {
                    System.out.println("  ❌ No tables found");
                }
            } catch (Exception e) {
                System.out.println("  ❌ Error: " + e.getMessage());
            }
        }
    }
}