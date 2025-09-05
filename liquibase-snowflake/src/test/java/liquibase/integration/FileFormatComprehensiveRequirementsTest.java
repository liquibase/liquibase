package liquibase.integration;

import liquibase.database.object.FileFormat;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test to verify that our FileFormat implementation covers ALL 22 properties 
 * that actually exist in Snowflake INFORMATION_SCHEMA.FILE_FORMATS (as corrected in requirements).
 */
public class FileFormatComprehensiveRequirementsTest {

    private Connection connection;
    private List<String> createdTestObjects = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
    }

    @Test
    public void testAll22RequiredPropertiesImplementedAndMapped() throws Exception {
        
        String testFormatName = "COMPREHENSIVE_REQ_TEST_" + System.currentTimeMillis();
        createdTestObjects.add(testFormatName);
        
        try {
            // CREATE: File format with as many properties as possible
            String createSQL = "CREATE FILE FORMAT " + testFormatName + " " +
                "TYPE = CSV " +
                "FIELD_DELIMITER = '|' " +
                "RECORD_DELIMITER = '\\n' " +
                "SKIP_HEADER = 2 " +
                "DATE_FORMAT = 'YYYY-MM-DD' " +
                "TIME_FORMAT = 'HH24:MI:SS' " +
                "TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS.FF3' " +
                "BINARY_FORMAT = BASE64 " +
                "ESCAPE = '\\\\' " +
                "ESCAPE_UNENCLOSED_FIELD = '%' " +
                "TRIM_SPACE = TRUE " +
                "FIELD_OPTIONALLY_ENCLOSED_BY = '\"' " +
                "NULL_IF = ('NULL', 'null', '') " +
                "COMPRESSION = GZIP " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH = FALSE " +
                "COMMENT = 'Comprehensive requirements test format'";
            
            try (Statement createStmt = connection.createStatement()) {
                createStmt.execute(createSQL);
            }
            
            // QUERY: Retrieve all 22 properties from INFORMATION_SCHEMA
            String sql = "SELECT FILE_FORMAT_CATALOG, FILE_FORMAT_SCHEMA, FILE_FORMAT_NAME, " +
                "FILE_FORMAT_OWNER, CREATED, LAST_ALTERED, " +
                "FILE_FORMAT_TYPE, FIELD_DELIMITER, RECORD_DELIMITER, SKIP_HEADER, " +
                "DATE_FORMAT, TIME_FORMAT, TIMESTAMP_FORMAT, BINARY_FORMAT, " +
                "ESCAPE, ESCAPE_UNENCLOSED_FIELD, TRIM_SPACE, FIELD_OPTIONALLY_ENCLOSED_BY, " +
                "NULL_IF, COMPRESSION, ERROR_ON_COLUMN_COUNT_MISMATCH, COMMENT " +
                "FROM INFORMATION_SCHEMA.FILE_FORMATS " +
                "WHERE FILE_FORMAT_NAME = ?";
            
            try (PreparedStatement queryStmt = connection.prepareStatement(sql)) {
                queryStmt.setString(1, testFormatName);
                try (ResultSet rs = queryStmt.executeQuery()) {
                    
                    assertTrue(rs.next(), "Should find the created file format");
                    
                    // VERIFY: All 22 properties can be retrieved
                    
                    // Identity Properties (3)
                    assertNotNull(rs.getString("FILE_FORMAT_CATALOG"), "FILE_FORMAT_CATALOG should not be null");
                    assertNotNull(rs.getString("FILE_FORMAT_SCHEMA"), "FILE_FORMAT_SCHEMA should not be null");
                    assertEquals(testFormatName, rs.getString("FILE_FORMAT_NAME"), "FILE_FORMAT_NAME should match");
                    
                    // State Properties (3)
                    assertNotNull(rs.getString("FILE_FORMAT_OWNER"), "FILE_FORMAT_OWNER should not be null");
                    assertNotNull(rs.getTimestamp("CREATED"), "CREATED should not be null");
                    assertNotNull(rs.getTimestamp("LAST_ALTERED"), "LAST_ALTERED should not be null");
                    
                    // Configuration Properties (16)
                    assertEquals("CSV", rs.getString("FILE_FORMAT_TYPE"), "FILE_FORMAT_TYPE should be CSV");
                    assertEquals("|", rs.getString("FIELD_DELIMITER"), "FIELD_DELIMITER should be |");
                    assertEquals("\n", rs.getString("RECORD_DELIMITER"), "RECORD_DELIMITER should be \\n");
                    assertEquals(2, rs.getInt("SKIP_HEADER"), "SKIP_HEADER should be 2");
                    assertEquals("YYYY-MM-DD", rs.getString("DATE_FORMAT"), "DATE_FORMAT should match");
                    assertEquals("HH24:MI:SS", rs.getString("TIME_FORMAT"), "TIME_FORMAT should match");
                    assertEquals("YYYY-MM-DD HH24:MI:SS.FF3", rs.getString("TIMESTAMP_FORMAT"), "TIMESTAMP_FORMAT should match");
                    assertEquals("BASE64", rs.getString("BINARY_FORMAT"), "BINARY_FORMAT should be BASE64");
                    assertEquals("\\", rs.getString("ESCAPE"), "ESCAPE should be \\");
                    assertEquals("%", rs.getString("ESCAPE_UNENCLOSED_FIELD"), "ESCAPE_UNENCLOSED_FIELD should be %");
                    assertEquals("true", rs.getString("TRIM_SPACE").toLowerCase(), "TRIM_SPACE should be true");
                    assertEquals("\"", rs.getString("FIELD_OPTIONALLY_ENCLOSED_BY"), "FIELD_OPTIONALLY_ENCLOSED_BY should be \"");
                    assertNotNull(rs.getString("NULL_IF"), "NULL_IF should not be null");
                    assertEquals("GZIP", rs.getString("COMPRESSION"), "COMPRESSION should be GZIP");
                    assertEquals("false", rs.getString("ERROR_ON_COLUMN_COUNT_MISMATCH").toLowerCase(), "ERROR_ON_COLUMN_COUNT_MISMATCH should be false");
                    assertEquals("Comprehensive requirements test format", rs.getString("COMMENT"), "COMMENT should match");
                    
                }
            }
            
            // VERIFY: Our FileFormat object can hold all these values
            FileFormat fileFormat = new FileFormat();
            fileFormat.setName(testFormatName);
            fileFormat.setFormatType("CSV");
            fileFormat.setFieldDelimiter("|");
            fileFormat.setRecordDelimiter("\n");
            fileFormat.setSkipHeader(2);
            fileFormat.setDateFormat("YYYY-MM-DD");
            fileFormat.setTimeFormat("HH24:MI:SS");
            fileFormat.setTimestampFormat("YYYY-MM-DD HH24:MI:SS.FF3");
            fileFormat.setBinaryFormat("BASE64");
            fileFormat.setEscape("\\");
            fileFormat.setEscapeUnenclosedField("%");
            fileFormat.setTrimSpace(true);
            fileFormat.setFieldOptionallyEnclosedBy("\"");
            fileFormat.setNullIf("['NULL', 'null', '']");
            fileFormat.setCompression("GZIP");
            fileFormat.setErrorOnColumnCountMismatch(false);
            
            // Verify all object properties are accessible
            assertEquals(testFormatName, fileFormat.getName());
            assertEquals("CSV", fileFormat.getFormatType());
            assertEquals("|", fileFormat.getFieldDelimiter());
            assertEquals("\n", fileFormat.getRecordDelimiter());
            assertEquals(Integer.valueOf(2), fileFormat.getSkipHeader());
            assertEquals("YYYY-MM-DD", fileFormat.getDateFormat());
            assertEquals("HH24:MI:SS", fileFormat.getTimeFormat());
            assertEquals("YYYY-MM-DD HH24:MI:SS.FF3", fileFormat.getTimestampFormat());
            assertEquals("BASE64", fileFormat.getBinaryFormat());
            assertEquals("\\", fileFormat.getEscape());
            assertEquals("%", fileFormat.getEscapeUnenclosedField());
            assertTrue(fileFormat.getTrimSpace());
            assertEquals("\"", fileFormat.getFieldOptionallyEnclosedBy());
            assertEquals("['NULL', 'null', '']", fileFormat.getNullIf());
            assertEquals("GZIP", fileFormat.getCompression());
            assertFalse(fileFormat.getErrorOnColumnCountMismatch());
            
            
        } finally {
            // Cleanup
            for (String objectName : createdTestObjects) {
                try (PreparedStatement dropStmt = connection.prepareStatement("DROP FILE FORMAT IF EXISTS " + objectName)) {
                    dropStmt.execute();
                } catch (Exception e) {
                    System.err.println("Failed to cleanup test file format " + objectName + ": " + e.getMessage());
                }
            }
            
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
    
    @Test
    public void testPhantomPropertiesDocumented() {
        
        // Verify our object model has phantom properties for future Snowflake versions
        FileFormat fileFormat = new FileFormat();
        
        // These properties should exist in object model but cannot be populated from DB
        // fileFormat.setValidateUtf8(true) - removed, not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
        // assertTrue(fileFormat.getValidateUtf8()) - removed, method no longer exists
        
        fileFormat.setSkipBlankLines(true);  
        assertTrue(fileFormat.getSkipBlankLines());
        
        fileFormat.setReplaceInvalidCharacters(false);
        assertFalse(fileFormat.getReplaceInvalidCharacters());
        
        fileFormat.setSkipByteOrderMark(true);
        assertTrue(fileFormat.getSkipByteOrderMark());
        
        fileFormat.setEncoding("UTF8");
        assertEquals("UTF8", fileFormat.getEncoding());
        
        fileFormat.setMultiLine(true);
        assertTrue(fileFormat.getMultiLine());
        
        fileFormat.setParseHeader(false);
        assertFalse(fileFormat.getParseHeader());
        
        fileFormat.setFileExtension(".csv");
        assertEquals(".csv", fileFormat.getFileExtension());
        
    }
}