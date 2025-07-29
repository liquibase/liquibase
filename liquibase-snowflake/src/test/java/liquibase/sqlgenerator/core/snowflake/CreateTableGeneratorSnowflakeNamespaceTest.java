package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateTableStatement;
import liquibase.datatype.core.VarcharType;
import liquibase.datatype.core.IntType;
import liquibase.datatype.core.TimestampType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CreateTableGeneratorSnowflake namespace attribute support
 */
@DisplayName("CreateTableGeneratorSnowflake Namespace Attributes")
public class CreateTableGeneratorSnowflakeNamespaceTest {
    
    private CreateTableGeneratorSnowflake generator;
    private CreateTableStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new CreateTableGeneratorSnowflake();
        statement = new CreateTableStatement("PUBLIC", "PUBLIC", "TEST_TABLE");
        
        // Add some columns
        statement.addColumn("id", new IntType());
        statement.addColumn("name", new VarcharType());
        statement.addColumn("created_at", new TimestampType());
        
        // Setup database mock
        when(database.escapeTableName(anyString(), anyString(), anyString())).thenReturn("TEST_TABLE");
        when(database.escapeColumnName(anyString(), anyString(), anyString(), anyString())).thenAnswer(i -> i.getArgument(3));
        
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
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE TRANSIENT TABLE"));
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
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE VOLATILE TABLE"));
    }
    
    @Test
    @DisplayName("Should create temporary table from namespace attribute")
    void shouldCreateTemporaryTableFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("temporary", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE TEMPORARY TABLE"));
    }
    
    @Test
    @DisplayName("Should create local temporary table from namespace attribute")
    void shouldCreateLocalTemporaryTableFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("localTemporary", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE LOCAL TEMPORARY TABLE"));
    }
    
    @Test
    @DisplayName("Should add cluster by clause from namespace attribute")
    void shouldAddClusterByFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("clusterBy", "id,created_at");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CLUSTER BY (id,created_at)"));
    }
    
    @Test
    @DisplayName("Should add data retention from namespace attribute")
    void shouldAddDataRetentionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("dataRetentionTimeInDays", "7");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 7"));
    }
    
    @Test
    @DisplayName("Should add change tracking from namespace attribute")
    void shouldAddChangeTrackingFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("changeTracking", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CHANGE_TRACKING = TRUE"));
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
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE TRANSIENT TABLE"));
        assertTrue(sql.contains("CLUSTER BY (id,name)"));
        assertTrue(sql.contains("CHANGE_TRACKING = TRUE"));
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
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE TRANSIENT TABLE"));
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
            generator.generateSql(statement, database, sqlGeneratorChain)
        );
    }
    
    @Test
    @DisplayName("Should fall back to legacy tablespace approach")
    void shouldFallBackToLegacyTablespaceApproach() {
        // Given - no namespace attributes but tablespace contains "transient"
        statement.setTablespace("transient");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE TRANSIENT TABLE"));
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
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then - namespace attribute should win
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE VOLATILE TABLE"));
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
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION = 'en-ci'"));
    }
    
    @Test
    @DisplayName("Should handle enableSchemaEvolution")
    void shouldHandleEnableSchemaEvolution() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("enableSchemaEvolution", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ENABLE_SCHEMA_EVOLUTION = TRUE"));
    }
}