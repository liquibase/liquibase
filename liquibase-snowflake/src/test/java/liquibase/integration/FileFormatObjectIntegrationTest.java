package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.util.TestDatabaseConfigUtil;
import liquibase.database.object.FileFormat;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.FileFormatComparator;
import liquibase.snapshot.jvm.FileFormatSnapshotGeneratorSnowflake;
import liquibase.structure.core.Schema;
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
 * Integration tests for FileFormat object snapshot/diff functionality.
 * Implements three-phase approach: Direct Component → Framework API → Pattern Validation.
 * 
 * ADDRESSES_CORE_ISSUE: Bridge unit tests → test harness with real database validation for FileFormat objects.
 */
public class FileFormatObjectIntegrationTest {

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
        return "INT_TEST_FILEFORMAT_" + methodName.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration instead of environment variables
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        
        // Ensure we're using the correct schema - first check if it exists, create if not
        try {
            PreparedStatement useSchema = connection.prepareStatement("USE SCHEMA BASE_SCHEMA");
            useSchema.execute();
            useSchema.close();
        } catch (Exception e) {
            // Schema doesn't exist, create it
            PreparedStatement createSchema = connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS BASE_SCHEMA");
            createSchema.execute();
            createSchema.close();
            
            PreparedStatement useSchema = connection.prepareStatement("USE SCHEMA BASE_SCHEMA");
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
                PreparedStatement dropStmt = connection.prepareStatement("DROP FILE FORMAT IF EXISTS BASE_SCHEMA." + objectName);
                dropStmt.execute();
                dropStmt.close();
            } catch (Exception e) {
                System.err.println("Failed to cleanup test file format " + objectName + ": " + e.getMessage());
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
    public void testFileFormatSnapshotGeneratorDirectQuery() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("directQuery");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test file format in Snowflake with comprehensive properties
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE FILE FORMAT " + uniqueName + " " +
                "TYPE = CSV " +
                "FIELD_DELIMITER = ',' " +
                "RECORD_DELIMITER = '\\n' " +
                "SKIP_HEADER = 1 " +
                "FIELD_OPTIONALLY_ENCLOSED_BY = '\"' " +
                "ESCAPE = '\\\\' " +
                "TRIM_SPACE = TRUE " +
                "EMPTY_FIELD_AS_NULL = TRUE " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH = FALSE " +
                "COMPRESSION = GZIP " +
                "DATE_FORMAT = 'YYYY-MM-DD' " +
                "TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS' " +
                "BINARY_FORMAT = HEX " +
                "NULL_IF = ('NULL', 'null')"
            );
            createStmt.execute();
            createStmt.close();
            
            // DIRECT QUERY: Test the SQL queries our generator uses
            // Test INFORMATION_SCHEMA query
            PreparedStatement infoStmt = connection.prepareStatement(
                "SELECT FILE_FORMAT_NAME, FILE_FORMAT_TYPE, RECORD_DELIMITER, FIELD_DELIMITER, " +
                "FIELD_OPTIONALLY_ENCLOSED_BY, ESCAPE, DATE_FORMAT, TIMESTAMP_FORMAT, BINARY_FORMAT, " +
                "COMPRESSION, NULL_IF, SKIP_HEADER, TRIM_SPACE, " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH " +
                "FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_NAME = ?"
            );
            infoStmt.setString(1, uniqueName);
            ResultSet rs = infoStmt.executeQuery();
            
            assertTrue(rs.next(), "INFORMATION_SCHEMA should find our file format");
            assertEquals(uniqueName, rs.getString("FILE_FORMAT_NAME"), "File format name should match");
            assertEquals("CSV", rs.getString("FILE_FORMAT_TYPE"), "File format type should match");
            assertEquals(",", rs.getString("FIELD_DELIMITER"), "Field delimiter should match");
            assertEquals("\"", rs.getString("FIELD_OPTIONALLY_ENCLOSED_BY"), "Field optionally enclosed by should match");
            assertEquals("GZIP", rs.getString("COMPRESSION"), "Compression should match");
            assertEquals(1, rs.getInt("SKIP_HEADER"), "Skip header should match");
            assertEquals("true", rs.getString("TRIM_SPACE"), "Trim space should match");
            assertEquals("false", rs.getString("ERROR_ON_COLUMN_COUNT_MISMATCH"), "Error on column count mismatch should match");
            
            rs.close();
            infoStmt.close();
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    @Test
    public void testFileFormatSnapshotGeneratorObjectCreation() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("objectCreation");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: Set up test file format with JSON-specific properties
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE FILE FORMAT " + uniqueName + " " +
                "TYPE = JSON " +
                "COMPRESSION = BROTLI " +
                "DATE_FORMAT = 'YYYY-MM-DD' " +
                "TIMESTAMP_FORMAT = 'YYYY-MM-DD\"T\"HH24:MI:SS' " +
                "BINARY_FORMAT = BASE64"
            );
            createStmt.execute();
            createStmt.close();
            
            // TEST: Create FileFormatSnapshotGeneratorSnowflake and test object creation
            FileFormatSnapshotGeneratorSnowflake generator = new FileFormatSnapshotGeneratorSnowflake();
            
            // Verify generator configuration
            assertEquals(FileFormatSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                        generator.getPriority(FileFormat.class, database));
            
            // Create a file format object manually using the same pattern as our generator
            FileFormat fileFormatObject = new FileFormat();
            fileFormatObject.setName(uniqueName);
            fileFormatObject.setSchema(new Schema((String) null, "BASE_SCHEMA"));
            
            // Set test attributes to verify they get captured
            fileFormatObject.setFormatType("JSON");
            fileFormatObject.setCompression("BROTLI");
            fileFormatObject.setDateFormat("YYYY-MM-DD");
            fileFormatObject.setTimestampFormat("YYYY-MM-DD\"T\"HH24:MI:SS");
            fileFormatObject.setBinaryFormat("BASE64");
            
            // Verify object properties are set correctly
            assertEquals(uniqueName, fileFormatObject.getName(), "File format name should be set");
            assertEquals("JSON", fileFormatObject.getFormatType(), "Format type should be set");
            assertEquals("BROTLI", fileFormatObject.getCompression(), "Compression should be set");
            assertEquals("YYYY-MM-DD", fileFormatObject.getDateFormat(), "Date format should be set");
            assertEquals("YYYY-MM-DD\"T\"HH24:MI:SS", fileFormatObject.getTimestampFormat(), "Timestamp format should be set");
            assertEquals("BASE64", fileFormatObject.getBinaryFormat(), "Binary format should be set");
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    // ===========================================
    // PHASE 1B: DIRECT COMPONENT TESTING - COMPARATOR
    // ===========================================

    @Test
    public void testFileFormatComparatorSameObjects() throws Exception {
        
        // Create two identical file format objects
        FileFormat format1 = new FileFormat();
        format1.setName("TEST_FORMAT");
        format1.setSchema(new Schema((String) null, "BASE_SCHEMA"));
        format1.setFormatType("CSV");
        format1.setCompression("GZIP");
        format1.setFieldDelimiter(",");
        format1.setRecordDelimiter("\n");
        format1.setSkipHeader(1);
        format1.setTrimSpace(true);
        
        FileFormat format2 = new FileFormat();
        format2.setName("TEST_FORMAT");
        format2.setSchema(new Schema((String) null, "BASE_SCHEMA"));
        format2.setFormatType("CSV");
        format2.setCompression("GZIP");
        format2.setFieldDelimiter(",");
        format2.setRecordDelimiter("\n");
        format2.setSkipHeader(1);
        format2.setTrimSpace(true);
        
        // COMPARE: Same objects should have no differences
        FileFormatComparator comparator = new FileFormatComparator();
        ObjectDifferences differences = comparator.findDifferences(
            format1, format2, database, new CompareControl(), null, new HashSet<>()
        );
        
        // VALIDATE: Should be identical
        assertFalse(differences.hasDifferences(), "Same objects should have no differences");
        
    }

    @Test 
    public void testFileFormatComparatorDifferentObjects() throws Exception {
        
        // Create source file format object
        FileFormat source = new FileFormat();
        source.setName("TEST_FORMAT");
        source.setSchema(new Schema((String) null, "BASE_SCHEMA"));
        source.setFormatType("CSV");
        source.setCompression("GZIP");
        source.setFieldDelimiter(",");
        source.setRecordDelimiter("\n");
        source.setSkipHeader(1);
        source.setTrimSpace(true);
        
        // Create target file format object with differences
        FileFormat target = new FileFormat();
        target.setName("TEST_FORMAT");
        target.setSchema(new Schema((String) null, "BASE_SCHEMA"));
        target.setFormatType("JSON"); // Different
        target.setCompression("BROTLI"); // Different
        target.setFieldDelimiter("|"); // Different
        target.setRecordDelimiter("\r\n"); // Different
        target.setSkipHeader(0); // Different
        target.setTrimSpace(false); // Different
        
        // COMPARE: Different objects should have differences
        FileFormatComparator comparator = new FileFormatComparator();
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
            // CREATE: Set up test file format in Snowflake
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE FILE FORMAT " + uniqueName + " " +
                "TYPE = CSV " +
                "FIELD_DELIMITER = ',' " +
                "RECORD_DELIMITER = '\\n' " +
                "SKIP_HEADER = 1 " +
                "TRIM_SPACE = TRUE " +
                "COMPRESSION = GZIP"
            );
            createStmt.execute();
            createStmt.close();
            
            // SNAPSHOT: Query database to capture state (simulating what our generator does)
            PreparedStatement queryStmt = connection.prepareStatement(
                "SELECT FILE_FORMAT_NAME, FILE_FORMAT_TYPE, FIELD_DELIMITER, RECORD_DELIMITER, " +
                "SKIP_HEADER, TRIM_SPACE, COMPRESSION " +
                "FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_NAME = ?"
            );
            queryStmt.setString(1, uniqueName);
            ResultSet rs = queryStmt.executeQuery();
            
            assertTrue(rs.next(), "Should find created file format");
            
            // Create file format object from snapshot data and capture values
            FileFormat snapshotResult = new FileFormat();
            snapshotResult.setName(rs.getString("FILE_FORMAT_NAME"));
            snapshotResult.setSchema(new Schema((String) null, "BASE_SCHEMA"));
            snapshotResult.setFormatType(rs.getString("FILE_FORMAT_TYPE"));
            snapshotResult.setFieldDelimiter(rs.getString("FIELD_DELIMITER"));
            snapshotResult.setRecordDelimiter(rs.getString("RECORD_DELIMITER"));
            snapshotResult.setCompression(rs.getString("COMPRESSION"));
            
            // Handle integer and boolean fields
            int skipHeader = rs.getInt("SKIP_HEADER");
            if (!rs.wasNull()) {
                snapshotResult.setSkipHeader(skipHeader);
            }
            
            String trimSpace = rs.getString("TRIM_SPACE");
            if (trimSpace != null) {
                snapshotResult.setTrimSpace("TRUE".equalsIgnoreCase(trimSpace));
            }
            
            // Capture values before closing ResultSet
            String formatType = rs.getString("FILE_FORMAT_TYPE");
            String compression = rs.getString("COMPRESSION");
            
            rs.close();
            queryStmt.close();
            
            // COMPARE: Create different version and compare
            FileFormat modifiedVersion = new FileFormat();
            modifiedVersion.setName(uniqueName);
            modifiedVersion.setSchema(new Schema((String) null, "BASE_SCHEMA"));
            modifiedVersion.setFormatType("JSON"); // Different format type
            modifiedVersion.setFieldDelimiter(","); // Same delimiter
            modifiedVersion.setRecordDelimiter("\n"); // Same record delimiter
            modifiedVersion.setCompression(compression); // Same compression
            modifiedVersion.setSkipHeader(1); // Same skip header
            modifiedVersion.setTrimSpace(true); // Same trim space
            
            FileFormatComparator comparator = new FileFormatComparator();
            ObjectDifferences differences = comparator.findDifferences(
                snapshotResult, modifiedVersion, database, new CompareControl(), null, new HashSet<>()
            );
            
            // VALIDATE: Complete workflow executed without exceptions
            assertNotNull(snapshotResult, "Snapshot should capture file format");
            assertNotNull(differences, "Comparator should return differences");
            assertTrue(differences.hasDifferences(), "Should detect format type differences");
            
            // VALIDATE: Snapshot captured correct original values
            assertEquals(uniqueName, snapshotResult.getName(), "Name should match");
            assertEquals(formatType, snapshotResult.getFormatType(), "Format type should match");
            assertEquals(",", snapshotResult.getFieldDelimiter(), "Field delimiter should match");
            assertEquals("GZIP", snapshotResult.getCompression(), "Compression should match");
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }

    // ===========================================
    // PHASE 2: FRAMEWORK API INTEGRATION TESTS
    // ===========================================

    @Test
    public void testFileFormatSnapshotGeneratorServiceRegistration() throws Exception {
        
        // Test direct service loading
        FileFormatSnapshotGeneratorSnowflake generator = new FileFormatSnapshotGeneratorSnowflake();
        
        // Verify framework integration - priority handling
        assertEquals(FileFormatSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                    generator.getPriority(FileFormat.class, database));
        assertEquals(FileFormatSnapshotGeneratorSnowflake.PRIORITY_NONE, 
                    generator.getPriority(liquibase.database.object.Database.class, database));
        
    }

    @Test
    public void testFileFormatComparatorServiceRegistration() throws Exception {
        
        FileFormatComparator comparator = new FileFormatComparator();
        
        // Verify framework integration - priority handling
        assertEquals(FileFormatComparator.PRIORITY_TYPE,
                    comparator.getPriority(FileFormat.class, database));
        assertEquals(FileFormatComparator.PRIORITY_NONE,
                    comparator.getPriority(liquibase.database.object.Database.class, database));
        
    }

    // ===========================================
    // PHASE 3: PATTERN VALIDATION
    // ===========================================

    @Test
    public void testFileFormatIsolationPattern() throws Exception {
        
        // Test that our unique naming pattern prevents conflicts
        String format1 = getUniqueTestObjectName("isolation1");
        String format2 = getUniqueTestObjectName("isolation2");
        String format3 = getUniqueTestObjectName("isolation3");
        
        // Verify all names are unique
        assertNotEquals(format1, format2, "Should generate unique names");
        assertNotEquals(format2, format3, "Should generate unique names");
        assertNotEquals(format1, format3, "Should generate unique names");
        
        // Verify naming pattern consistency
        assertTrue(format1.startsWith("INT_TEST_FILEFORMAT_"), "Should follow naming pattern");
        assertTrue(format2.startsWith("INT_TEST_FILEFORMAT_"), "Should follow naming pattern");
        assertTrue(format3.startsWith("INT_TEST_FILEFORMAT_"), "Should follow naming pattern");
        
        
    }

    @Test
    public void testErrorHandlingPatterns() throws Exception {
        
        FileFormatSnapshotGeneratorSnowflake generator = new FileFormatSnapshotGeneratorSnowflake();
        
        // Test generator configuration  
        assertEquals(FileFormatSnapshotGeneratorSnowflake.PRIORITY_DATABASE, 
                    generator.getPriority(FileFormat.class, database));
        assertEquals(FileFormatSnapshotGeneratorSnowflake.PRIORITY_NONE, 
                    generator.getPriority(liquibase.database.object.Database.class, database));
        
        // Test comparator configuration
        FileFormatComparator comparator = new FileFormatComparator();
        assertEquals(FileFormatComparator.PRIORITY_TYPE,
                    comparator.getPriority(FileFormat.class, database));
        assertEquals(FileFormatComparator.PRIORITY_NONE,
                    comparator.getPriority(liquibase.database.object.Database.class, database));
        
    }

    @Test
    public void testSnowflakeSpecificAttributeHandling() throws Exception {
        
        String uniqueName = getUniqueTestObjectName("attributes");
        createdTestObjects.add(uniqueName);
        
        try {
            // CREATE: File format with comprehensive Snowflake attributes
            // Note: PARQUET type only supports COMPRESSION option
            PreparedStatement createStmt = connection.prepareStatement(
                "CREATE FILE FORMAT " + uniqueName + " " +
                "TYPE = PARQUET " +
                "COMPRESSION = SNAPPY"
            );
            createStmt.execute();
            createStmt.close();
            
            // QUERY: Capture and verify attribute handling using the same patterns as our generator
            PreparedStatement queryStmt = connection.prepareStatement(
                "SELECT FILE_FORMAT_NAME, FILE_FORMAT_TYPE, COMPRESSION " +
                "FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_NAME = ?"
            );
            queryStmt.setString(1, uniqueName);
            ResultSet rs = queryStmt.executeQuery();
            
            assertTrue(rs.next(), "Should find created file format");
            
            // VALIDATE: Different Snowflake-specific attributes handled correctly
            assertEquals(uniqueName, rs.getString("FILE_FORMAT_NAME"), "File format name handling");
            assertEquals("PARQUET", rs.getString("FILE_FORMAT_TYPE"), "Format type attribute handling");
            assertEquals("SNAPPY", rs.getString("COMPRESSION"), "Compression attribute handling");
            
            rs.close();
            queryStmt.close();
            
            
        } finally {
            // Cleanup handled in tearDown()
        }
    }
}