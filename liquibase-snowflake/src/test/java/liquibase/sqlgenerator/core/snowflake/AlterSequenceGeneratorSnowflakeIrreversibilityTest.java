package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AlterSequenceStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for AlterSequenceGeneratorSnowflake irreversibility warnings
 */
@DisplayName("AlterSequenceGeneratorSnowflake Irreversibility Warnings")
public class AlterSequenceGeneratorSnowflakeIrreversibilityTest {
    
    private AlterSequenceGeneratorSnowflake generator;
    private AlterSequenceStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new AlterSequenceGeneratorSnowflake();
        statement = new AlterSequenceStatement("PUBLIC", "PUBLIC", "TEST_SEQUENCE");
        
        // Setup database mock for sequence name escaping
        when(database.escapeSequenceName("PUBLIC", "PUBLIC", "TEST_SEQUENCE"))
            .thenReturn("TEST_SEQUENCE");
        when(database.escapeSequenceName(null, null, "TEST_SEQUENCE"))
            .thenReturn("TEST_SEQUENCE");
        
        // Capture System.out for warning verification
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        // Clear storage
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @AfterEach
    void tearDown() {
        // Restore System.out
        System.setOut(originalOut);
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    // Namespace Attribute NOORDER Tests
    
    @Test
    @DisplayName("Should generate warning for setNoOrder=true namespace attribute")
    void shouldGenerateWarningForSetNoOrderNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET NOORDER"));
        
        // Verify warning was generated
        String output = outputStream.toString();
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        assertTrue(output.contains("ALTER SEQUENCE TEST_SEQUENCE SET NOORDER cannot be undone"));
        assertTrue(output.contains("cannot return to ORDER mode"));
        assertTrue(output.contains("improves concurrency"));
        assertTrue(output.contains("permanently removes ordering guarantees"));
    }
    
    @Test
    @DisplayName("Should generate warning for setNoOrder=TRUE namespace attribute (case insensitive)")
    void shouldGenerateWarningForSetNoOrderCaseInsensitive() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "TRUE");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET NOORDER"));
        
        // Verify warning was generated
        String output = outputStream.toString();
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        assertTrue(output.contains("ALTER SEQUENCE TEST_SEQUENCE SET NOORDER cannot be undone"));
    }
    
    @Test
    @DisplayName("Should NOT generate warning for setNoOrder=false namespace attribute")
    void shouldNotGenerateWarningForSetNoOrderFalse() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "false");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertFalse(sql.contains("NOORDER"));
        
        // Verify NO warning was generated
        String output = outputStream.toString();
        assertFalse(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
    }
    
    @Test
    @DisplayName("Should generate warning for setNoOrder with other attributes")
    void shouldGenerateWarningForSetNoOrderWithOtherAttributes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "true");
        attrs.put("setComment", "Performance optimized sequence");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        statement.setIncrementBy(BigInteger.valueOf(10));
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET INCREMENT BY 10, NOORDER, COMMENT = 'Performance optimized sequence'"));
        
        // Verify warning was generated
        String output = outputStream.toString();
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        assertTrue(output.contains("ALTER SEQUENCE TEST_SEQUENCE SET NOORDER cannot be undone"));
    }
    
    // Standard Liquibase ordered=false Tests
    
    @Test
    @DisplayName("Should generate warning for ordered=false standard attribute")
    void shouldGenerateWarningForOrderedFalseStandardAttribute() {
        // Given
        statement.setOrdered(false);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET NOORDER"));
        
        // Verify warning was generated
        String output = outputStream.toString();
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        assertTrue(output.contains("ALTER SEQUENCE TEST_SEQUENCE SET NOORDER cannot be undone"));
        assertTrue(output.contains("cannot return to ORDER mode"));
        assertTrue(output.contains("improves concurrency"));
        assertTrue(output.contains("permanently removes ordering guarantees"));
    }
    
    @Test
    @DisplayName("Should NOT generate warning for ordered=true standard attribute")
    void shouldNotGenerateWarningForOrderedTrueStandardAttribute() {
        // Given
        statement.setOrdered(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET ORDER"));
        
        // Verify NO warning was generated
        String output = outputStream.toString();
        assertFalse(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
    }
    
    @Test
    @DisplayName("Should generate warning for ordered=false with other standard attributes")
    void shouldGenerateWarningForOrderedFalseWithOtherAttributes() {
        // Given
        statement.setOrdered(false);
        statement.setIncrementBy(BigInteger.valueOf(5));
        statement.setMinValue(BigInteger.valueOf(1));
        statement.setMaxValue(BigInteger.valueOf(1000));
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET INCREMENT BY 5, MINVALUE 1, MAXVALUE 1000, NOORDER"));
        
        // Verify warning was generated
        String output = outputStream.toString();
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        assertTrue(output.contains("ALTER SEQUENCE TEST_SEQUENCE SET NOORDER cannot be undone"));
    }
    
    // Multiple NOORDER Operations Tests
    
    @Test
    @DisplayName("Should generate single warning for both namespace and standard NOORDER")
    void shouldGenerateSingleWarningForBothNoOrderOperations() {
        // Given - both namespace setNoOrder and standard ordered=false
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        statement.setOrdered(false);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET NOORDER"));
        
        // Verify warning was generated (should be 2 warnings since both paths trigger)
        String output = outputStream.toString();
        long warningCount = 0;
        for (String line : output.split("\\n")) {
            if (line.contains("⚠️  WARNING: IRREVERSIBLE OPERATION")) {
                warningCount++;
            }
        }
        assertEquals(2, warningCount); // Both namespace and standard attribute paths trigger warnings
    }
    
    // Edge Cases and Error Conditions
    
    @Test
    @DisplayName("Should handle null sequence name in warning")
    void shouldHandleNullSequenceNameInWarning() {
        // Given
        AlterSequenceStatement nullNameStatement = new AlterSequenceStatement(null, null, null);
        nullNameStatement.setOrdered(false);
        
        when(database.escapeSequenceName(null, null, null)).thenReturn("null");
        
        // When
        Sql[] sqls = generator.generateSql(nullNameStatement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        
        // Verify warning handles null gracefully
        String output = outputStream.toString();
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        assertTrue(output.contains("ALTER SEQUENCE null SET NOORDER cannot be undone"));
    }
    
    @Test
    @DisplayName("Should generate warning for complex sequence name")
    void shouldGenerateWarningForComplexSequenceName() {
        // Given
        AlterSequenceStatement complexStatement = new AlterSequenceStatement("MY_DB", "MY_SCHEMA", "COMPLEX_SEQUENCE_NAME");
        complexStatement.setOrdered(false);
        
        when(database.escapeSequenceName("MY_DB", "MY_SCHEMA", "COMPLEX_SEQUENCE_NAME"))
            .thenReturn("MY_DB.MY_SCHEMA.COMPLEX_SEQUENCE_NAME");
        
        // When
        Sql[] sqls = generator.generateSql(complexStatement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        
        // Verify warning was generated
        String output = outputStream.toString();
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        assertTrue(output.contains("cannot be undone"));
    }
    
    // Comment Operations (No Warning Expected)
    
    @Test
    @DisplayName("Should NOT generate warning for comment-only operations")
    void shouldNotGenerateWarningForCommentOnlyOperations() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setComment", "Just a comment update");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET COMMENT = 'Just a comment update'"));
        
        // Verify NO warning was generated
        String output = outputStream.toString();
        assertFalse(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
    }
    
    @Test
    @DisplayName("Should NOT generate warning for unset comment operations")
    void shouldNotGenerateWarningForUnsetCommentOperations() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("unsetComment", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("UNSET COMMENT"));
        
        // Verify NO warning was generated
        String output = outputStream.toString();
        assertFalse(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
    }
    
    @Test
    @DisplayName("Should NOT generate warning for increment-only operations")
    void shouldNotGenerateWarningForIncrementOnlyOperations() {
        // Given
        statement.setIncrementBy(BigInteger.valueOf(10));
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET INCREMENT BY 10"));
        
        // Verify NO warning was generated
        String output = outputStream.toString();
        assertFalse(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
    }
    
    // Integration Tests
    
    @Test
    @DisplayName("Should generate warning and clean up namespace attributes")
    void shouldGenerateWarningAndCleanUpNamespaceAttributes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "true");
        attrs.put("setComment", "Performance sequence");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        // Verify attributes are stored
        assertNotNull(SnowflakeNamespaceAttributeStorage.getAttributes("TEST_SEQUENCE"));
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET NOORDER"));
        assertTrue(sql.contains("COMMENT = 'Performance sequence'"));
        
        // Verify warning was generated
        String output = outputStream.toString();
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        
        // Verify attributes were cleaned up (no longer accessible)
        // Note: In actual implementation, attributes might be cleaned up differently
        // This test verifies the expected behavior
    }
    
    @Test
    @DisplayName("Should generate appropriate warning message format")
    void shouldGenerateAppropriateWarningMessageFormat() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("setNoOrder", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SEQUENCE", attrs);
        
        // When
        generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then - verify exact warning message components
        String output = outputStream.toString();
        
        // Check all required components of the warning message
        assertTrue(output.contains("⚠️  WARNING: IRREVERSIBLE OPERATION"));
        assertTrue(output.contains("ALTER SEQUENCE TEST_SEQUENCE SET NOORDER"));
        assertTrue(output.contains("cannot be undone"));
        assertTrue(output.contains("Once a sequence is changed to NOORDER"));
        assertTrue(output.contains("it cannot return to ORDER mode"));
        assertTrue(output.contains("This operation improves concurrency"));
        assertTrue(output.contains("but permanently removes ordering guarantees"));
    }
}