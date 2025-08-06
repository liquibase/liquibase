package liquibase.integration;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.FileFormat;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * True integration test for FileFormatSnapshotGeneratorSnowflake using Liquibase's actual snapshot framework.
 * ADDRESSES CRITICAL TESTING GAPS: Tests the protected snapshotObject() and addTo() methods through the framework.
 * 
 * TARGET: Achieve 80%+ coverage on FileFormatSnapshotGeneratorSnowflake by testing core business logic.
 */
public class FileFormatSnapshotIntegrationTest {

    private Connection connection;
    private Database database;
    private List<String> createdTestObjects = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration for Snowflake connection
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        
        // Set up schema
        try {
            PreparedStatement useSchema = connection.prepareStatement("USE SCHEMA BASE_SCHEMA");
            useSchema.execute();
            useSchema.close();
        } catch (Exception e) {
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
        // Clean up all created test objects
        for (String objectName : createdTestObjects) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP FILE FORMAT IF EXISTS BASE_SCHEMA." + objectName);
                dropStmt.execute();
                dropStmt.close();
                System.out.println("Cleaned up test file format: " + objectName);
            } catch (Exception e) {
                System.err.println("Failed to cleanup test file format " + objectName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testSnapshotObjectMethod_ExistingFileFormat() throws Exception {
        System.out.println("=== TESTING snapshotObject() METHOD WITH EXISTING FILEFORMAT ===");
        
        String testFormatName = "SNAPSHOT_OBJECT_TEST_" + System.currentTimeMillis();
        createdTestObjects.add(testFormatName);
        
        // Step 1: Create a comprehensive FileFormat in Snowflake
        try (PreparedStatement createStmt = connection.prepareStatement(
            "CREATE FILE FORMAT " + testFormatName + " " +
            "TYPE = CSV " +
            "FIELD_DELIMITER = '|' " +
            "RECORD_DELIMITER = '\\n' " +
            "SKIP_HEADER = 3 " +
            "DATE_FORMAT = 'YYYY-MM-DD' " +
            "TIME_FORMAT = 'HH24:MI:SS' " +
            "TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS' " +
            "BINARY_FORMAT = BASE64 " +
            "COMPRESSION = GZIP " +
            "TRIM_SPACE = TRUE " +
            "ERROR_ON_COLUMN_COUNT_MISMATCH = FALSE " +
            "ESCAPE = '\\\\' " +
            "ESCAPE_UNENCLOSED_FIELD = '%' " +
            "FIELD_OPTIONALLY_ENCLOSED_BY = '\"' " +
            "NULL_IF = ('NULL', 'null', 'N/A') " +
            "COMMENT = 'Comprehensive snapshot test format'"
        )) {
            createStmt.execute();
            System.out.println("✅ Test FileFormat created in Snowflake");
        }
        
        // Step 2: Use Liquibase's actual snapshot framework to test snapshotObject()
        Catalog catalog = new Catalog("LB_DBEXT_INT_DB");
        Schema schema = new Schema(catalog, "BASE_SCHEMA");
        
        // Create a FileFormat example to trigger snapshotObject()
        FileFormat exampleFormat = new FileFormat(testFormatName);
        exampleFormat.setSchema(schema);
        
        // This calls FileFormatSnapshotGeneratorSnowflake.snapshotObject() internally
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            new CatalogAndSchema(catalog.getName(), schema.getName()), database, snapshotControl);
        
        // Step 3: Verify the snapshotObject() method populated all properties correctly
        FileFormat snapshotted = snapshot.get(exampleFormat);
        
        assertNotNull(snapshotted, "snapshotObject() should return a populated FileFormat");
        assertEquals(testFormatName, snapshotted.getName(), "Name should be correctly populated");
        assertEquals("CSV", snapshotted.getFormatType(), "FormatType should be correctly populated");
        assertEquals("|", snapshotted.getFieldDelimiter(), "FieldDelimiter should be correctly populated");
        assertEquals("\\n", snapshotted.getRecordDelimiter(), "RecordDelimiter should be correctly populated");
        assertEquals(Integer.valueOf(3), snapshotted.getSkipHeader(), "SkipHeader should be correctly populated");
        assertEquals("YYYY-MM-DD", snapshotted.getDateFormat(), "DateFormat should be correctly populated");
        assertEquals("HH24:MI:SS", snapshotted.getTimeFormat(), "TimeFormat should be correctly populated");  
        assertEquals("YYYY-MM-DD HH24:MI:SS", snapshotted.getTimestampFormat(), "TimestampFormat should be correctly populated");
        assertEquals("BASE64", snapshotted.getBinaryFormat(), "BinaryFormat should be correctly populated");
        assertEquals("GZIP", snapshotted.getCompression(), "Compression should be correctly populated");
        assertEquals(Boolean.TRUE, snapshotted.getTrimSpace(), "TrimSpace should be correctly populated");
        assertEquals(Boolean.FALSE, snapshotted.getErrorOnColumnCountMismatch(), "ErrorOnColumnCountMismatch should be correctly populated");
        assertEquals("\\", snapshotted.getEscape(), "Escape should be correctly populated");
        assertEquals("%", snapshotted.getEscapeUnenclosedField(), "EscapeUnenclosedField should be correctly populated");
        assertEquals("\"", snapshotted.getFieldOptionallyEnclosedBy(), "FieldOptionallyEnclosedBy should be correctly populated");
        assertNotNull(snapshotted.getNullIf(), "NullIf should be populated");
        assertTrue(snapshotted.getNullIf().contains("NULL"), "NullIf should contain 'NULL'");
        
        System.out.println("✅ SUCCESS: snapshotObject() method correctly populated ALL properties from Snowflake");
    }

    @Test
    public void testSnapshotObjectMethod_NonExistentFileFormat() throws Exception {
        System.out.println("=== TESTING snapshotObject() METHOD WITH NON-EXISTENT FILEFORMAT ===");
        
        String nonExistentFormatName = "NON_EXISTENT_" + System.currentTimeMillis();
        
        // Create a FileFormat example for a non-existent format
        Catalog catalog = new Catalog("LB_DBEXT_INT_DB");
        Schema schema = new Schema(catalog, "BASE_SCHEMA");
        FileFormat exampleFormat = new FileFormat(nonExistentFormatName);
        exampleFormat.setSchema(schema);
        
        // This should call snapshotObject() and return null for non-existent format
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            new CatalogAndSchema(catalog.getName(), schema.getName()), database, snapshotControl);
        
        FileFormat result = snapshot.get(exampleFormat);
        assertNull(result, "snapshotObject() should return null for non-existent FileFormat");
        
        System.out.println("✅ SUCCESS: snapshotObject() correctly handles non-existent FileFormat");
    }

    @Test  
    public void testSnapshotObjectMethod_NullName() throws Exception {
        System.out.println("=== TESTING snapshotObject() METHOD WITH NULL NAME ===");
        
        // Create a FileFormat with null name
        Catalog catalog = new Catalog("LB_DBEXT_INT_DB");
        Schema schema = new Schema(catalog, "BASE_SCHEMA");
        FileFormat exampleFormat = new FileFormat();
        exampleFormat.setName(null);  // Explicitly set to null
        exampleFormat.setSchema(schema);
        
        // This should call snapshotObject() and return null due to null name check
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            new CatalogAndSchema(catalog.getName(), schema.getName()), database, snapshotControl);
        
        FileFormat result = snapshot.get(exampleFormat);
        assertNull(result, "snapshotObject() should return null for FileFormat with null name");
        
        System.out.println("✅ SUCCESS: snapshotObject() correctly handles null name validation");
    }

    @Test
    public void testSnapshotObjectMethod_NullSchema() throws Exception {
        System.out.println("=== TESTING snapshotObject() METHOD WITH NULL SCHEMA ===");
        
        String testFormatName = "NULL_SCHEMA_TEST_" + System.currentTimeMillis();
        createdTestObjects.add(testFormatName);
        
        // Create a test format in the default schema
        try (PreparedStatement createStmt = connection.prepareStatement(
            "CREATE FILE FORMAT " + testFormatName + " " +
            "TYPE = JSON " +
            "COMPRESSION = AUTO " +
            "COMMENT = 'Null schema test'"
        )) {
            createStmt.execute();
            System.out.println("✅ Test FileFormat created for null schema test");
        }
        
        // Create a FileFormat with null schema (should default to database defaults)
        FileFormat exampleFormat = new FileFormat(testFormatName);
        exampleFormat.setSchema(null);  // Explicitly set to null
        
        // This should call snapshotObject() and handle null schema by defaulting  
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            new CatalogAndSchema("LB_DBEXT_INT_DB", "BASE_SCHEMA"), database, snapshotControl);
        
        FileFormat result = snapshot.get(exampleFormat);
        assertNotNull(result, "snapshotObject() should handle null schema by defaulting");
        assertEquals(testFormatName, result.getName(), "Name should be correctly populated");
        assertEquals("JSON", result.getFormatType(), "FormatType should be correctly populated");
        
        System.out.println("✅ SUCCESS: snapshotObject() correctly handles null schema with defaults");
    }

    @Test
    public void testAddToMethod_SchemaPopulation() throws Exception {
        System.out.println("=== TESTING addTo() METHOD - SCHEMA BULK POPULATION ===");
        
        // Create multiple FileFormats in the same schema
        String[] testFormatNames = {
            "ADDTO_CSV_" + System.currentTimeMillis(),
            "ADDTO_JSON_" + System.currentTimeMillis(),
            "ADDTO_PARQUET_" + System.currentTimeMillis()
        };
        
        for (String formatName : testFormatNames) {
            createdTestObjects.add(formatName);
        }
        
        // Create CSV format
        try (PreparedStatement createStmt = connection.prepareStatement(
            "CREATE FILE FORMAT " + testFormatNames[0] + " " +
            "TYPE = CSV " +
            "FIELD_DELIMITER = ',' " +
            "SKIP_HEADER = 1"
        )) {
            createStmt.execute();
        }
        
        // Create JSON format
        try (PreparedStatement createStmt = connection.prepareStatement(
            "CREATE FILE FORMAT " + testFormatNames[1] + " " +
            "TYPE = JSON " +
            "COMPRESSION = GZIP"
        )) {
            createStmt.execute();
        }
        
        // Create PARQUET format
        try (PreparedStatement createStmt = connection.prepareStatement(
            "CREATE FILE FORMAT " + testFormatNames[2] + " " +
            "TYPE = PARQUET " +
            "COMPRESSION = SNAPPY"
        )) {
            createStmt.execute();
        }
        
        System.out.println("✅ Created 3 test FileFormats for addTo() testing");
        
        // Use Liquibase's snapshot framework to snapshot the entire schema
        // This will call addTo() method to populate all FileFormats in the schema
        Catalog catalog = new Catalog("LB_DBEXT_INT_DB");
        Schema schema = new Schema(catalog, "BASE_SCHEMA");
        
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new CatalogAndSchema(catalog.getName(), schema.getName()), database, snapshotControl);
        
        // Verify that addTo() populated all FileFormats in the schema
        List<FileFormat> fileFormats = snapshot.get(schema).getDatabaseObjects(FileFormat.class);
        
        assertNotNull(fileFormats, "addTo() should populate FileFormats list");
        assertTrue(fileFormats.size() >= 3, "addTo() should find at least our 3 test formats (found: " + fileFormats.size() + ")");
        
        // Verify our specific test formats were found and populated correctly
        boolean foundCsv = false, foundJson = false, foundParquet = false;
        
        for (FileFormat format : fileFormats) {
            if (testFormatNames[0].equals(format.getName())) {
                foundCsv = true;
                assertEquals("CSV", format.getFormatType(), "CSV format type should be correct");
                assertEquals(",", format.getFieldDelimiter(), "CSV field delimiter should be correct");
                assertEquals(Integer.valueOf(1), format.getSkipHeader(), "CSV skip header should be correct");
            } else if (testFormatNames[1].equals(format.getName())) {
                foundJson = true;
                assertEquals("JSON", format.getFormatType(), "JSON format type should be correct");
                assertEquals("GZIP", format.getCompression(), "JSON compression should be correct");
            } else if (testFormatNames[2].equals(format.getName())) {
                foundParquet = true;
                assertEquals("PARQUET", format.getFormatType(), "PARQUET format type should be correct");
                assertEquals("SNAPPY", format.getCompression(), "PARQUET compression should be correct");
            }
        }
        
        assertTrue(foundCsv, "addTo() should have found and populated CSV format");
        assertTrue(foundJson, "addTo() should have found and populated JSON format");  
        assertTrue(foundParquet, "addTo() should have found and populated PARQUET format");
        
        System.out.println("✅ SUCCESS: addTo() method correctly populated ALL FileFormats in schema");
        System.out.println("   Found " + fileFormats.size() + " total FileFormats in schema");
    }

    @Test
    public void testAddToMethod_NonSchemaObject() throws Exception {
        System.out.println("=== TESTING addTo() METHOD WITH NON-SCHEMA OBJECT ===");
        
        // Create a non-Schema object (Catalog) to test the guard clause
        Catalog catalog = new Catalog("LB_DBEXT_INT_DB");
        
        // This should call addTo() with a non-Schema object and return early
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        
        // This should not throw an exception and should handle the non-Schema object gracefully
        assertDoesNotThrow(() -> {
            DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new CatalogAndSchema(catalog.getName(), null), database, snapshotControl);
            // The addTo method should return early for non-Schema objects
        }, "addTo() should handle non-Schema objects gracefully");
        
        System.out.println("✅ SUCCESS: addTo() correctly handles non-Schema objects");
    }

    @Test
    public void testBooleanAndIntegerFieldConversion() throws Exception {
        System.out.println("=== TESTING BOOLEAN AND INTEGER FIELD CONVERSION ===");
        
        String testFormatName = "FIELD_CONVERSION_TEST_" + System.currentTimeMillis();
        createdTestObjects.add(testFormatName);
        
        // Create a FileFormat with specific boolean and integer values
        try (PreparedStatement createStmt = connection.prepareStatement(
            "CREATE FILE FORMAT " + testFormatName + " " +
            "TYPE = CSV " +
            "SKIP_HEADER = 5 " +
            "TRIM_SPACE = FALSE " +
            "ERROR_ON_COLUMN_COUNT_MISMATCH = TRUE"
        )) {
            createStmt.execute();
            System.out.println("✅ Test FileFormat created with boolean/integer fields");
        }
        
        // Snapshot the format to test field conversion logic
        Catalog catalog = new Catalog("LB_DBEXT_INT_DB");
        Schema schema = new Schema(catalog, "BASE_SCHEMA");
        FileFormat exampleFormat = new FileFormat(testFormatName);
        exampleFormat.setSchema(schema);
        
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            new CatalogAndSchema(catalog.getName(), schema.getName()), database, snapshotControl);
        
        FileFormat result = snapshot.get(exampleFormat);
        assertNotNull(result, "Should successfully snapshot format with boolean/integer fields");
        
        // Test integer field conversion
        assertEquals(Integer.valueOf(5), result.getSkipHeader(), "Integer field SKIP_HEADER should be correctly converted");
        
        // Test boolean field conversions
        assertEquals(Boolean.FALSE, result.getTrimSpace(), "Boolean field TRIM_SPACE should be correctly converted to FALSE");
        assertEquals(Boolean.TRUE, result.getErrorOnColumnCountMismatch(), "Boolean field ERROR_ON_COLUMN_COUNT_MISMATCH should be correctly converted to TRUE");
        
        System.out.println("✅ SUCCESS: Boolean and integer field conversion working correctly");
    }
}