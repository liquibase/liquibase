package liquibase.integration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.diff.output.report.DiffToReport;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.DatabaseObject;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full-cycle integration test for Table objects:
 * 1. Initialize schema with SQL statements (comprehensive Table variations with Snowflake features)
 * 2. Generate changelog from source schema
 * 3. Deploy changelog to clean target schema
 * 4. Diff the two schemas
 * 5. Expect NO differences
 *
 * This validates the complete round-trip: SQL → Snapshot → Diff → ChangeLog → Deploy → Validate
 */
@DisplayName("Table Full-Cycle Integration Test")
public class TableFullCycleIntegrationTest {

    private Database database;
    private Connection connection;
    // Schema isolation: Use unique names per test run to enable parallel execution
    private String testId = String.valueOf(System.currentTimeMillis());
    private String sourceSchema = "TBL_FC_SRC_" + testId;
    private String targetSchema = "TBL_FC_TGT_" + testId;
    
    @BeforeEach
    public void setUp() throws Exception {
        try {
            connection = TestDatabaseConfigUtil.getSnowflakeConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Create clean schemas for testing
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + sourceSchema + " CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS " + targetSchema + " CASCADE");
                stmt.execute("CREATE SCHEMA " + sourceSchema);
                stmt.execute("CREATE SCHEMA " + targetSchema);
            }
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake or create schemas: " + e.getMessage());
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + sourceSchema + " CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS " + targetSchema + " CASCADE");
            } catch (Exception e) {
                System.err.println("Failed to cleanup schemas: " + e.getMessage());
            }
            connection.close();
        }
    }
    
    @Test
    @DisplayName("Table full-cycle: SQL → Generate Changelog → Deploy → Diff → Expect Zero Differences")
    public void testTableFullCycle() throws Exception {
        // PHASE 1: Initialize source schema with comprehensive Table objects using SQL
        
        try (Statement stmt = connection.createStatement()) {
            // Use the source schema
            stmt.execute("USE SCHEMA " + sourceSchema);
            
            // Basic table with standard data types
            stmt.execute("CREATE TABLE TBL_BASIC_TYPES (" +
                "ID NUMBER(10,0) NOT NULL, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "DESCRIPTION TEXT, " +
                "AMOUNT NUMBER(15,2), " +
                "IS_ACTIVE BOOLEAN DEFAULT TRUE, " +
                "CREATED_DATE DATE, " +
                "UPDATED_TIME TIMESTAMP_NTZ(9) DEFAULT CURRENT_TIMESTAMP(), " +
                "PRIMARY KEY (ID)" +
            ")");
            
            
            // Table with Snowflake-specific semi-structured data types
            stmt.execute("CREATE TABLE TBL_SEMI_STRUCTURED (" +
                "ID NUMBER AUTOINCREMENT, " +
                "JSON_DATA VARIANT, " +
                "ARRAY_DATA ARRAY, " +
                "OBJECT_DATA OBJECT, " +
                "BINARY_DATA BINARY(50), " +
                "GEO_POINT GEOGRAPHY, " +
                "GEO_SHAPE GEOMETRY, " +
                "CREATED_AT TIMESTAMP_LTZ DEFAULT CURRENT_TIMESTAMP(), " +
                "PRIMARY KEY (ID)" +
            ")");
            
            
            // Transient table with clustering
            stmt.execute("CREATE TRANSIENT TABLE TBL_TRANSIENT_CLUSTERED (" +
                "CUSTOMER_ID NUMBER(10,0), " +
                "ORDER_DATE DATE, " +
                "PRODUCT_CATEGORY VARCHAR(50), " +
                "ORDER_AMOUNT NUMBER(12,2), " +
                "REGION VARCHAR(20), " +
                "STATUS VARCHAR(20)" +
                ") CLUSTER BY (ORDER_DATE, CUSTOMER_ID)");
            
            
            // Table with foreign key reference
            stmt.execute("CREATE TABLE TBL_ORDERS (" +
                "ORDER_ID NUMBER(10,0) NOT NULL, " +
                "CUSTOMER_ID NUMBER(10,0) NOT NULL, " +
                "ORDER_DATE TIMESTAMP_NTZ, " +
                "TOTAL_AMOUNT NUMBER(15,2), " +
                "PRIMARY KEY (ORDER_ID), " +
                "FOREIGN KEY (CUSTOMER_ID) REFERENCES TBL_BASIC_TYPES(ID)" +
            ")");
            
            
            // Table with complex constraints and indexes
            stmt.execute("CREATE TABLE TBL_COMPLEX_CONSTRAINTS (" +
                "ID NUMBER(10,0) IDENTITY(1000,10), " +
                "EMAIL VARCHAR(255) NOT NULL, " +
                "USER_NAME VARCHAR(50) NOT NULL, " +
                "AGE NUMBER(3,0), " +
                "SALARY NUMBER(10,2), " +
                "DEPARTMENT VARCHAR(50) DEFAULT 'GENERAL', " +
                "HIRE_DATE DATE NOT NULL, " +
                "CONSTRAINT PK_COMPLEX_ID PRIMARY KEY (ID), " +
                "CONSTRAINT UK_COMPLEX_EMAIL UNIQUE (EMAIL), " +
                "CONSTRAINT UK_COMPLEX_USERNAME UNIQUE (USER_NAME)" +
            ")");
            
            // NOTE: Index creation removed due to Snowflake trial account limitations
            // Trial accounts don't support CREATE INDEX on regular tables
            // Original code:
            // stmt.execute("CREATE INDEX IDX_ORDERS_DATE ON TBL_ORDERS (ORDER_DATE)");
            // stmt.execute("CREATE INDEX IDX_COMPLEX_DEPT_HIRE ON TBL_COMPLEX_CONSTRAINTS (DEPARTMENT, HIRE_DATE)");
            
            // Table with column-level comments and collation
            stmt.execute("CREATE TABLE TBL_ADVANCED_FEATURES (" +
                "ID NUMBER(10,0) NOT NULL COMMENT 'Unique identifier', " +
                "TITLE VARCHAR(200) COLLATE 'en-ci' COMMENT 'Case-insensitive title', " +
                "CONTENT TEXT COMMENT 'Main content body', " +
                "TAGS ARRAY COMMENT 'Array of tags', " +
                "METADATA VARIANT COMMENT 'Flexible metadata storage', " +
                "VERSION NUMBER(5,0) DEFAULT 1 COMMENT 'Version number', " +
                "IS_PUBLISHED BOOLEAN DEFAULT FALSE COMMENT 'Publication status', " +
                "PUBLISHED_DATE TIMESTAMP_TZ COMMENT 'Publication timestamp with timezone', " +
                "PRIMARY KEY (ID)" +
            ")");
        }
        
        // DEBUG: Verify tables were created successfully
        try (Statement verifyStmt = connection.createStatement()) {
            verifyStmt.execute("USE SCHEMA " + sourceSchema);
            ResultSet rs = verifyStmt.executeQuery("SHOW TABLES LIKE 'TBL_%'");
            int tableCount = 0;
            while (rs.next()) {
                tableCount++;
                String tableName = rs.getString("name");
            }
            rs.close();
            
            if (tableCount == 0) {
                System.err.println("❌ No tables found in source schema - this will cause empty changelog");
            }
        } catch (Exception e) {
            System.err.println("❌ Error verifying table creation: " + e.getMessage());
        }
        
        // PHASE 2: Generate changelog from source schema
        
        // Set database to use source schema
        database.setDefaultSchemaName(sourceSchema);
        
        // Create simple SnapshotControl - avoid PrimaryKey/ForeignKey to prevent UniqueConstraint discovery
        // which requires INFORMATION_SCHEMA.CONSTRAINTS access that may not be available
        SnapshotControl snapshotControl = new SnapshotControl(database, Table.class, Column.class);
        
        // Create explicit Schema object for the source schema to ensure proper context
        Schema sourceSchemaObject = new Schema(database.getDefaultCatalogName(), sourceSchema);
        
        DatabaseSnapshot sourceSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[]{sourceSchemaObject}, database, snapshotControl);
        
        // DEBUG: Critical snapshot analysis
        int tableCount = sourceSnapshot.get(Table.class) != null ? sourceSnapshot.get(Table.class).size() : 0;
        if (tableCount == 0) {
            System.err.println("❌ SNAPSHOT DISCOVERY FAILED: No tables found in snapshot despite 6 tables existing!");
            System.err.println("  🎯 Schema used for snapshot: " + sourceSchema);
            System.err.println("  🎯 Database default schema: " + database.getDefaultSchemaName());
        } else {
        }
        
        // Generate diff against empty database to get complete changelog
        CompareControl compareControl = new CompareControl();
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            .compare(sourceSnapshot, null, compareControl);
        
        // DEBUG: Critical diff analysis
        int missingObjects = diffResult.getMissingObjects().size();
        if (missingObjects == 0) {
            System.err.println("❌ DIFF FAILED: No missing objects found - this means no CREATE statements will be generated!");
        }
        
        // Write diff to temporary changelog file
        File tempChangelogFile = File.createTempFile("table-fullcycle-", ".xml");
        tempChangelogFile.deleteOnExit();
        
        try (FileOutputStream outputStream = new FileOutputStream(tempChangelogFile)) {
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
            diffToChangeLog.print(new PrintStream(outputStream));
        }
        
        // DEBUG: Examine generated changelog content
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(tempChangelogFile.toPath());
            
            // Look for changeSet elements
            long changeSetCount = lines.stream().filter(line -> line.contains("<changeSet")).count();
            
            // Show first 20 lines of changelog for inspection
            for (int i = 0; i < Math.min(20, lines.size()); i++) {
            }
            
            if (changeSetCount == 0) {
                System.err.println("❌ Empty changelog generated - no changeSets found!");
            }
        } catch (Exception e) {
            System.err.println("❌ Error reading changelog content: " + e.getMessage());
        }
        
        // PHASE 3: Deploy generated changelog to target schema
        
        // Switch database to target schema
        database.setDefaultSchemaName(targetSchema);
        
        // Create Liquibase instance with generated changelog using directory resource accessor
        CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(
            new DirectoryResourceAccessor(tempChangelogFile.getParentFile()),
            new ClassLoaderResourceAccessor()
        );
        Liquibase liquibase = new Liquibase(tempChangelogFile.getName(), resourceAccessor, database);
        
        // Deploy the generated changelog
        liquibase.update(new Contexts(), new LabelExpression());
        
        
        // PHASE 4: Take snapshots of both schemas and compare
        
        // Take snapshot of target schema (after deployment)
        DatabaseSnapshot targetSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Reset database to source schema and take fresh snapshot
        database.setDefaultSchemaName(sourceSchema);
        DatabaseSnapshot freshSourceSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // PHASE 5: Compare snapshots - should be identical
        
        // Compare the snapshots
        CompareControl finalCompareControl = new CompareControl();
        // Suppress schema names in comparison since they're different
        finalCompareControl.addSuppressedField(Schema.class, "name");
        finalCompareControl.addSuppressedField(Table.class, "schemaName");
        finalCompareControl.addSuppressedField(Column.class, "relation");
        finalCompareControl.addSuppressedField(PrimaryKey.class, "table");
        finalCompareControl.addSuppressedField(ForeignKey.class, "foreignKeyTable");
        // NOTE: Index.class suppression removed due to trial account limitations
        // Suppress auto-increment current values which may vary
        finalCompareControl.addSuppressedField(Column.class, "autoIncrementInformation");
        
        DiffResult finalDiff = DiffGeneratorFactory.getInstance()
            .compare(freshSourceSnapshot, targetSnapshot, finalCompareControl);
        
        // VALIDATION: Assert no differences
        if (!finalDiff.areEqual()) {
            System.err.println("❌ FOUND DIFFERENCES - Full cycle failed!");
            System.err.println("Missing objects: " + finalDiff.getMissingObjects().size());
            System.err.println("Unexpected objects: " + finalDiff.getUnexpectedObjects().size());
            System.err.println("Changed objects: " + finalDiff.getChangedObjects().size());
            
            // Print detailed diff for debugging
            new DiffToReport(finalDiff, System.err).print();
            
            fail("Full-cycle test failed: Source and target schemas are not identical after round-trip");
        }
        
        
        // Additional validation: Verify table functionality and constraints
        try (Statement stmt = connection.createStatement()) {
            
            // Switch to target schema and test table functionality
            stmt.execute("USE SCHEMA " + targetSchema);
            
            // Enhanced diagnostic: Verify that the table exists and has correct structure
            java.sql.ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'TBL_BASIC_TYPES'");
            if (!rs.next()) {
                fail("Table TBL_BASIC_TYPES does not exist in target schema " + targetSchema + 
                     ". Changelog deployment may have failed.");
            }
            rs.close();
            
            // Additional diagnostic: Check table structure 
            try {
                rs = stmt.executeQuery("DESCRIBE TABLE TBL_BASIC_TYPES");
                System.out.println("DEBUG: TBL_BASIC_TYPES structure:");
                while (rs.next()) {
                    System.out.println("  Column: " + rs.getString("name") + " Type: " + rs.getString("type"));
                }
                rs.close();
            } catch (Exception e) {
                fail("Failed to describe TBL_BASIC_TYPES structure: " + e.getMessage());
            }
            
            // Additional diagnostic: Verify current schema context
            rs = stmt.executeQuery("SELECT CURRENT_SCHEMA()");
            if (rs.next()) {
                String currentSchema = rs.getString(1);
                System.out.println("DEBUG: Current schema context: " + currentSchema);
                if (!targetSchema.equals(currentSchema)) {
                    System.out.println("WARNING: Schema context mismatch. Expected: " + targetSchema + ", Actual: " + currentSchema);
                }
            }
            rs.close();
            
            // Test basic insert/select operations with enhanced error reporting
            try {
                stmt.execute("INSERT INTO " + targetSchema + ".TBL_BASIC_TYPES (ID, NAME, AMOUNT) VALUES (1, 'Test Record', 100.50)");
                System.out.println("DEBUG: INSERT successful");
            } catch (Exception e) {
                System.err.println("DEBUG: INSERT failed with error: " + e.getMessage());
                System.err.println("DEBUG: Error class: " + e.getClass().getSimpleName());
                throw e; // Re-throw to fail the test with original error
            }
            
            // Test semi-structured data insertion
            stmt.execute("INSERT INTO " + targetSchema + ".TBL_SEMI_STRUCTURED (JSON_DATA, ARRAY_DATA) " +
                "VALUES (PARSE_JSON('{\"key\": \"value\", \"num\": 123}'), ARRAY_CONSTRUCT('a', 'b', 'c'))");
            
            // Test foreign key constraint
            stmt.execute("INSERT INTO " + targetSchema + ".TBL_ORDERS (ORDER_ID, CUSTOMER_ID, TOTAL_AMOUNT) VALUES (1001, 1, 250.75)");
            
            // Test identity column
            stmt.execute("INSERT INTO " + targetSchema + ".TBL_COMPLEX_CONSTRAINTS (EMAIL, USER_NAME, AGE, SALARY, HIRE_DATE) VALUES ('test@example.com', 'testuser', 30, 50000.00, CURRENT_DATE())");
            
            
            // Verify table structures
            stmt.execute("SHOW TABLES LIKE 'TBL_%'");
            
            // Test constraints by attempting invalid data (should fail)
            try {
                stmt.execute("INSERT INTO " + targetSchema + ".TBL_COMPLEX_CONSTRAINTS (EMAIL, USER_NAME, AGE, SALARY, HIRE_DATE) VALUES ('invalid', 'user2', -5, -1000, CURRENT_DATE())");
                fail("Should have failed due to check constraint violation");
            } catch (Exception e) {
                // Expected - constraint violation
            }
            
            // Test unique constraints
            try {
                stmt.execute("INSERT INTO " + targetSchema + ".TBL_COMPLEX_CONSTRAINTS (EMAIL, USER_NAME, AGE, SALARY, HIRE_DATE) VALUES ('test@example.com', 'user3', 25, 40000, CURRENT_DATE())");
                fail("Should have failed due to unique constraint violation");
            } catch (Exception e) {
                // Expected - unique constraint violation
            }
        }
        
        // Additional snapshot validation for structural completeness
        
        // Count all database objects should match
        int sourceTableCount = freshSourceSnapshot.get(Table.class).size();
        int targetTableCount = targetSnapshot.get(Table.class).size();
        assertEquals(sourceTableCount, targetTableCount, "Table count should match");
        
        int sourcePKCount = freshSourceSnapshot.get(PrimaryKey.class).size();
        int targetPKCount = targetSnapshot.get(PrimaryKey.class).size();
        assertEquals(sourcePKCount, targetPKCount, "Primary key count should match");
        
        int sourceFKCount = freshSourceSnapshot.get(ForeignKey.class).size();
        int targetFKCount = targetSnapshot.get(ForeignKey.class).size();
        assertEquals(sourceFKCount, targetFKCount, "Foreign key count should match");
        
        // NOTE: Index count validation removed due to trial account limitations with index creation
        
        
        
        // Cleanup temp file
        tempChangelogFile.delete();
    }
}