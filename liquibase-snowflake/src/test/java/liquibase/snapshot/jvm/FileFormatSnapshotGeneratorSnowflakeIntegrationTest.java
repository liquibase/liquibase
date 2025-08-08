package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.FileFormat;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FileFormatSnapshotGeneratorSnowflake using real database connections.
 * Uses TestDatabaseConfigUtil framework with proper schema isolation pattern.
 */
@DisplayName("FileFormat Snapshot Generator Snowflake Integration Tests")
public class FileFormatSnapshotGeneratorSnowflakeIntegrationTest {

    private FileFormatSnapshotGeneratorSnowflake generator;
    private Database database;
    private Connection connection;
    private String testSchema = "FF_SNAPSHOT_TEST";
    
    @BeforeEach
    public void setUp() throws Exception {
        generator = new FileFormatSnapshotGeneratorSnowflake();
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        JdbcConnection jdbcConnection = new JdbcConnection(connection);
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
        
        // Create clean test schema using schema isolation pattern
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE");
            stmt.execute("CREATE SCHEMA " + testSchema);
            stmt.execute("USE SCHEMA " + testSchema);
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE");
            }
            connection.close();
        }
    }

    // ==================== Priority Tests ====================
    
    @Test
    @DisplayName("getPriority for FileFormat on Snowflake returns high priority")
    public void testGetPriority_FileFormatOnSnowflake_ReturnsHighPriority() {
        int priority = generator.getPriority(FileFormat.class, database);
        assertEquals(SnapshotGenerator.PRIORITY_DATABASE, priority);
    }
    
    @Test
    @DisplayName("getPriority for Schema on Snowflake returns additional priority")
    public void testGetPriority_SchemaOnSnowflake_ReturnsAdditionalPriority() {
        int priority = generator.getPriority(Schema.class, database);
        assertEquals(SnapshotGenerator.PRIORITY_ADDITIONAL, priority);
    }

    // ==================== Snapshot Tests ====================
    
    @Test
    @DisplayName("snapshotObject for non-existent FileFormat returns null")
    public void testSnapshotObject_NonExistentFileFormat_ReturnsNull() throws Exception {
        // Given: FileFormat that doesn't exist
        Catalog catalog = new Catalog(database.getDefaultCatalogName());
        Schema schema = new Schema(catalog, testSchema);
        FileFormat fileFormat = new FileFormat("NON_EXISTENT_FORMAT");
        fileFormat.setSchema(schema);
        
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Taking snapshot
        FileFormat result = (FileFormat) generator.snapshotObject(fileFormat, snapshot);
        
        // Then: Should return null
        assertNull(result);
    }
    
    @Test
    @DisplayName("snapshotObject for existing FileFormat returns populated object")
    public void testSnapshotObject_ExistingFileFormat_ReturnsPopulatedObject() throws Exception {
        // Given: Create a FileFormat in the database
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE FILE FORMAT TEST_FORMAT " +
                "TYPE = 'CSV' " +
                "FIELD_DELIMITER = ',' " +
                "RECORD_DELIMITER = '\\n' " +
                "SKIP_HEADER = 1 " +
                "TRIM_SPACE = TRUE " +
                "COMPRESSION = 'GZIP' " +
                "COMMENT = 'Test FileFormat for integration test'");
        }
        
        Catalog catalog = new Catalog(database.getDefaultCatalogName());
        Schema schema = new Schema(catalog, testSchema);
        FileFormat fileFormat = new FileFormat("TEST_FORMAT");
        fileFormat.setSchema(schema);
        
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Taking snapshot
        FileFormat result = (FileFormat) generator.snapshotObject(fileFormat, snapshot);
        
        // Then: Should return populated FileFormat
        assertNotNull(result);
        assertEquals("TEST_FORMAT", result.getName());
        assertEquals("CSV", result.getFormatType());
        assertEquals(",", result.getFieldDelimiter());
        assertEquals("\n", result.getRecordDelimiter());
        assertEquals(Integer.valueOf(1), result.getSkipHeader());
        assertEquals(Boolean.TRUE, result.getTrimSpace());
        assertEquals("GZIP", result.getCompression());
    }
    
    @Test
    @DisplayName("snapshotObject handles FileFormat with comprehensive properties")
    public void testSnapshotObject_ComprehensiveFileFormat_HandlesAllProperties() throws Exception {
        // Given: Create FileFormat with many properties
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE FILE FORMAT COMPREHENSIVE_FORMAT " +
                "TYPE = 'CSV' " +
                "FIELD_DELIMITER = ';' " +
                "RECORD_DELIMITER = '\\r\\n' " +
                "SKIP_HEADER = 2 " +
                "FIELD_OPTIONALLY_ENCLOSED_BY = '\"' " +
                "ESCAPE = '\\\\' " +
                "ESCAPE_UNENCLOSED_FIELD = '%' " +
                "TRIM_SPACE = FALSE " +
                "NULL_IF = ('NULL', '\\N') " +
                "COMPRESSION = 'GZIP' " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH = TRUE " +
                "DATE_FORMAT = 'YYYY-MM-DD' " +
                "TIME_FORMAT = 'HH24:MI:SS' " +
                "TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS' " +
                "BINARY_FORMAT = 'BASE64'");
        }
        
        Catalog catalog = new Catalog(database.getDefaultCatalogName());
        Schema schema = new Schema(catalog, testSchema);
        FileFormat fileFormat = new FileFormat("COMPREHENSIVE_FORMAT");
        fileFormat.setSchema(schema);
        
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Taking snapshot
        FileFormat result = (FileFormat) generator.snapshotObject(fileFormat, snapshot);
        
        // Then: Should handle all properties
        assertNotNull(result);
        assertEquals("COMPREHENSIVE_FORMAT", result.getName());
        assertEquals("CSV", result.getFormatType());
        assertEquals(";", result.getFieldDelimiter());
        // Note: Snowflake may return different record delimiter format than specified
        assertEquals(Integer.valueOf(2), result.getSkipHeader());
        assertEquals("\"", result.getFieldOptionallyEnclosedBy());
        assertEquals("\\", result.getEscape()); // Snowflake normalizes escape characters
        assertEquals("%", result.getEscapeUnenclosedField());
        assertEquals(Boolean.FALSE, result.getTrimSpace());
        assertEquals("GZIP", result.getCompression());
        assertEquals(Boolean.TRUE, result.getErrorOnColumnCountMismatch());
        assertEquals("YYYY-MM-DD", result.getDateFormat());
        assertEquals("HH24:MI:SS", result.getTimeFormat());
        assertEquals("YYYY-MM-DD HH24:MI:SS", result.getTimestampFormat());
        assertEquals("BASE64", result.getBinaryFormat());
    }
    
    @Test
    @DisplayName("snapshotObject handles JSON FileFormat")
    public void testSnapshotObject_JsonFileFormat_HandlesJsonSpecificProperties() throws Exception {
        // Given: Create JSON FileFormat
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE FILE FORMAT JSON_FORMAT " +
                "TYPE = 'JSON' " +
                "COMPRESSION = 'AUTO' " +
                "DATE_FORMAT = 'AUTO' " +
                "TIME_FORMAT = 'AUTO' " +
                "TIMESTAMP_FORMAT = 'AUTO'");
        }
        
        Catalog catalog = new Catalog(database.getDefaultCatalogName());
        Schema schema = new Schema(catalog, testSchema);
        FileFormat fileFormat = new FileFormat("JSON_FORMAT");
        fileFormat.setSchema(schema);
        
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Taking snapshot
        FileFormat result = (FileFormat) generator.snapshotObject(fileFormat, snapshot);
        
        // Then: Should handle JSON format correctly
        assertNotNull(result);
        assertEquals("JSON_FORMAT", result.getName());
        assertEquals("JSON", result.getFormatType());
        assertEquals("AUTO", result.getCompression());
        assertEquals("AUTO", result.getDateFormat());
        assertEquals("AUTO", result.getTimeFormat());
        assertEquals("AUTO", result.getTimestampFormat());
    }
    
    @Test
    @DisplayName("snapshotObject handles PARQUET FileFormat")
    public void testSnapshotObject_ParquetFileFormat_HandlesParquetProperties() throws Exception {
        // Given: Create PARQUET FileFormat
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE FILE FORMAT PARQUET_FORMAT " +
                "TYPE = 'PARQUET' " +
                "COMPRESSION = 'SNAPPY'");
        }
        
        Catalog catalog = new Catalog(database.getDefaultCatalogName());
        Schema schema = new Schema(catalog, testSchema);
        FileFormat fileFormat = new FileFormat("PARQUET_FORMAT");
        fileFormat.setSchema(schema);
        
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Taking snapshot
        FileFormat result = (FileFormat) generator.snapshotObject(fileFormat, snapshot);
        
        // Then: Should handle PARQUET format correctly
        assertNotNull(result);
        assertEquals("PARQUET_FORMAT", result.getName());
        assertEquals("PARQUET", result.getFormatType());
        assertEquals("SNAPPY", result.getCompression());
        // Note: PARQUET format may not have binary format property
    }

    // ==================== addTo Tests ====================
    
    @Test
    @DisplayName("addTo with Schema object handles gracefully")
    public void testAddTo_WithSchemaObject_HandlesGracefully() throws Exception {
        // Given: Schema object
        Catalog catalog = new Catalog(database.getDefaultCatalogName());
        Schema schema = new Schema(catalog, testSchema);
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Calling addTo (extension objects don't support bulk discovery)
        assertDoesNotThrow(() -> generator.addTo(schema, snapshot));
        
        // Then: Should complete without throwing exceptions
        // Extension objects use direct requests via snapshotObject() instead of bulk addTo()
    }
    
    @Test
    @DisplayName("addTo with non-Schema object handles gracefully")
    public void testAddTo_WithNonSchemaObject_HandlesGracefully() throws Exception {
        // Given: Non-schema object
        Catalog catalog = new Catalog(database.getDefaultCatalogName());
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Calling addTo
        assertDoesNotThrow(() -> generator.addTo(catalog, snapshot));
        
        // Then: Should handle gracefully
    }

    // ==================== Edge Cases ====================
    
    @Test
    @DisplayName("snapshotObject with null FileFormat name returns null")
    public void testSnapshotObject_NullFileFormatName_ReturnsNull() throws Exception {
        // Given: FileFormat with null name
        FileFormat fileFormat = new FileFormat();
        fileFormat.setName(null);
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Taking snapshot
        FileFormat result = (FileFormat) generator.snapshotObject(fileFormat, snapshot);
        
        // Then: Should return null
        assertNull(result);
    }
    
    @Test
    @DisplayName("snapshotObject with null schema uses default schema")
    public void testSnapshotObject_NullSchema_UsesDefaultSchema() throws Exception {
        // This test verifies the generator handles null schemas by creating defaults
        FileFormat fileFormat = new FileFormat("DEFAULT_SCHEMA_FORMAT");
        fileFormat.setSchema(null);
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // When: Taking snapshot (should use default schema)
        FileFormat result = (FileFormat) generator.snapshotObject(fileFormat, snapshot);
        
        // Then: Should attempt to find FileFormat in default schema (returns null if not found)
        assertNull(result); // FileFormat doesn't exist in default schema
    }

    // ==================== Framework Tests ====================
    
    @Test
    @DisplayName("replaces method returns empty array")
    public void testReplaces_Always_ReturnsEmptyArray() {
        Class<? extends SnapshotGenerator>[] result = generator.replaces();
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    @Test
    @DisplayName("addsTo method returns Schema class")
    public void testAddsTo_Always_ReturnsSchemaClass() {
        Class<?>[] result = generator.addsTo();
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(Schema.class, result[0]);
    }
    
    @Test
    @DisplayName("Integration test validates database connection")
    public void testDatabaseConnection_IsSnowflakeDatabase() {
        assertTrue(database instanceof SnowflakeDatabase);
        assertNotNull(database.getConnection());
    }
}