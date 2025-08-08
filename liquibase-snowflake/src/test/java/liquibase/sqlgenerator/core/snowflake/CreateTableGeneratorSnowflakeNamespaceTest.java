package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateTableStatement;
import liquibase.datatype.core.VarcharType;
import liquibase.datatype.core.IntType;
import liquibase.datatype.core.TimestampType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure SQL tests for CreateTableGeneratorSnowflake namespace attribute support.
 * Tests SQL generation without mocks, focusing on actual string output validation.
 */
@DisplayName("CreateTableGeneratorSnowflake Namespace Attributes")
public class CreateTableGeneratorSnowflakeNamespaceTest {
    
    private CreateTableGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private CreateTableStatement statement;
    
    @BeforeEach
    void setUp() {
        generator = new CreateTableGeneratorSnowflake();
        database = new SnowflakeDatabase();
        statement = new CreateTableStatement("PUBLIC", "PUBLIC", "TEST_TABLE");
        
        // Add some columns
        statement.addColumn("id", new IntType());
        statement.addColumn("name", new VarcharType());
        statement.addColumn("created_at", new TimestampType());
        
        // Clear storage
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @AfterEach
    void tearDown() {
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @Test
    @DisplayName("Should create transient table from namespace attribute")
    void shouldCreateTransientTableFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE TRANSIENT TABLE PUBLIC.PUBLIC.TEST_TABLE"), "SQL should start with CREATE TRANSIENT TABLE PUBLIC.PUBLIC.TEST_TABLE but was: " + sql);
        assertFalse(sql.contains("CREATE TABLE TABLE")); // Should not double TABLE
        
        // Verify attributes were cleaned up
        assertNull(SnowflakeNamespaceAttributeStorage.getAttributes("TEST_TABLE"));
    }
    
    @Test
    @DisplayName("Should create volatile table from namespace attribute")
    void shouldCreateVolatileTableFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("volatile", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE VOLATILE TABLE PUBLIC.PUBLIC.TEST_TABLE"), "SQL should start with CREATE VOLATILE TABLE PUBLIC.PUBLIC.TEST_TABLE but was: " + sql);
    }
    
    @Test
    @DisplayName("Should create temporary table from namespace attribute")
    void shouldCreateTemporaryTableFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("temporary", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE TEMPORARY TABLE PUBLIC.PUBLIC.TEST_TABLE"), "SQL should start with CREATE TEMPORARY TABLE PUBLIC.PUBLIC.TEST_TABLE but was: " + sql);
    }
    
    @Test
    @DisplayName("Should create local temporary table from namespace attribute")
    void shouldCreateLocalTemporaryTableFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("localTemporary", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE LOCAL TEMPORARY TABLE PUBLIC.PUBLIC.TEST_TABLE"), "SQL should start with CREATE LOCAL TEMPORARY TABLE PUBLIC.PUBLIC.TEST_TABLE but was: " + sql);
    }
    
    @Test
    @DisplayName("Should add cluster by clause from namespace attribute")
    void shouldAddClusterByFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("clusterBy", "id,created_at");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CLUSTER BY (id,created_at)"), "SQL should contain cluster by clause: " + sql);
    }
    
    @Test
    @DisplayName("Should add data retention from namespace attribute")
    void shouldAddDataRetentionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("dataRetentionTimeInDays", "7");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 7"), "SQL should contain data retention clause: " + sql);
    }
    
    @Test
    @DisplayName("Should add change tracking from namespace attribute")
    void shouldAddChangeTrackingFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("changeTracking", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CHANGE_TRACKING = TRUE"), "SQL should contain change tracking clause: " + sql);
    }
    
    @Test
    @DisplayName("Should combine multiple namespace attributes")
    void shouldCombineMultipleNamespaceAttributes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        attrs.put("clusterBy", "id,name");
        attrs.put("changeTracking", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE TRANSIENT TABLE PUBLIC.PUBLIC.TEST_TABLE"), "SQL should start with CREATE TRANSIENT TABLE PUBLIC.PUBLIC.TEST_TABLE but was: " + sql);
        assertTrue(sql.contains("CLUSTER BY (id,name)"), "SQL should contain cluster by clause: " + sql);
        assertTrue(sql.contains("CHANGE_TRACKING = TRUE"), "SQL should contain change tracking clause: " + sql);
    }
    
    @Test
    @DisplayName("Should not add data retention for transient table")
    void shouldNotAddDataRetentionForTransientTable() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        attrs.put("dataRetentionTimeInDays", "7"); // Should be ignored
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE TRANSIENT TABLE PUBLIC.PUBLIC.TEST_TABLE"), "SQL should start with CREATE TRANSIENT TABLE PUBLIC.PUBLIC.TEST_TABLE but was: " + sql);
        assertFalse(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
    }
    
    @Test
    @DisplayName("Should throw error for multiple table types")
    void shouldThrowErrorForMultipleTableTypes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        attrs.put("volatile", "true"); // Conflicting
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When/Then
        assertThrows(RuntimeException.class, () -> 
            generator.generateSql(statement, database, null)
        );
    }
    
    @Test
    @DisplayName("Should fall back to legacy tablespace approach")
    void shouldFallBackToLegacyTablespaceApproach() {
        // Given - no namespace attributes but tablespace contains "transient"
        statement.setTablespace("transient");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE TRANSIENT TABLE PUBLIC.PUBLIC.TEST_TABLE"), "SQL should start with CREATE TRANSIENT TABLE PUBLIC.PUBLIC.TEST_TABLE but was: " + sql);
    }
    
    @Test
    @DisplayName("Should prefer namespace attributes over legacy approach")
    void shouldPreferNamespaceAttributesOverLegacy() {
        // Given - both namespace attributes and legacy tablespace
        Map<String, String> attrs = new HashMap<>();
        attrs.put("volatile", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        statement.setTablespace("transient"); // Legacy approach says transient
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then - namespace attribute should win
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE VOLATILE TABLE PUBLIC.PUBLIC.TEST_TABLE"), "SQL should start with CREATE VOLATILE TABLE PUBLIC.PUBLIC.TEST_TABLE but was: " + sql);
        assertFalse(sql.contains("TRANSIENT"));
    }
    
    @Test
    @DisplayName("Should handle defaultDdlCollation with quotes")
    void shouldHandleDefaultDdlCollationWithQuotes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("defaultDdlCollation", "en-ci");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION = 'en-ci'"), "SQL should contain collation clause: " + sql);
    }
    
    @Test
    @DisplayName("Should handle enableSchemaEvolution")
    void shouldHandleEnableSchemaEvolution() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("enableSchemaEvolution", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ENABLE_SCHEMA_EVOLUTION = TRUE"), "SQL should contain schema evolution clause: " + sql);
    }
    
    // New namespace attributes tests (completed in Phase 2)
    
    @Test
    @DisplayName("Should add comment from namespace attribute")
    void shouldAddCommentFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("comment", "Table with custom comment");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Table with custom comment'"), "SQL should contain comment clause: " + sql);
    }
    
    @Test
    @DisplayName("Should add tag from namespace attribute")
    void shouldAddTagFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("tag", "production");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("TAG (production)"), "SQL should contain tag clause: " + sql);
    }
    
    @Test
    @DisplayName("Should add stageFileFormat from namespace attribute")
    void shouldAddStageFileFormatFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("stageFileFormat", "CSV_FORMAT");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("STAGE_FILE_FORMAT = CSV_FORMAT"), "SQL should contain stage file format clause: " + sql);
    }
    
    @Test
    @DisplayName("Should add stageCopyOptions from namespace attribute")
    void shouldAddStageCopyOptionsFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("stageCopyOptions", "ON_ERROR = 'CONTINUE'");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("STAGE_COPY_OPTIONS = ON_ERROR = 'CONTINUE'"), "SQL should contain stage copy options clause: " + sql);
    }
    
    @Test
    @DisplayName("Should combine all new namespace attributes")
    void shouldCombineAllNewNamespaceAttributes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("comment", "Production table with all features");
        attrs.put("tag", "production");
        attrs.put("stageFileFormat", "JSON_FORMAT");
        attrs.put("stageCopyOptions", "ON_ERROR = 'SKIP_FILE'");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("STAGE_FILE_FORMAT = JSON_FORMAT"), "SQL should contain stage file format: " + sql);
        assertTrue(sql.contains("STAGE_COPY_OPTIONS = ON_ERROR = 'SKIP_FILE'"), "SQL should contain stage copy options: " + sql);
        assertTrue(sql.contains("TAG (production)"), "SQL should contain tag clause: " + sql);
        assertTrue(sql.contains("COMMENT = 'Production table with all features'"), "SQL should contain comment clause: " + sql);
    }
    
    @Test
    @DisplayName("Should prefer namespace comment over statement comment")
    void shouldPreferNamespaceCommentOverStatementComment() {
        // Given - both namespace and statement comments
        Map<String, String> attrs = new HashMap<>();
        attrs.put("comment", "Namespace comment wins");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        statement.setRemarks("Statement comment");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then - namespace attribute should win
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Namespace comment wins'"), "SQL should contain namespace comment: " + sql);
        assertFalse(sql.contains("Statement comment"));
    }
    
    @Test
    @DisplayName("Should handle special characters in comment")
    void shouldHandleSpecialCharactersInComment() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("comment", "Comment with 'single' and \"double\" quotes");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Comment with ''single'' and \"double\" quotes'"), "SQL should contain escaped comment text: " + sql);
    }
}