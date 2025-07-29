package liquibase.integration;

import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sqlgenerator.core.snowflake.CreateTableGeneratorSnowflake;
import liquibase.statement.core.CreateTableStatement;
import liquibase.datatype.core.VarcharType;
import liquibase.datatype.core.IntType;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test to verify namespace attribute flow from storage to SQL generation
 */
@DisplayName("Snowflake Namespace Integration Test")
public class SnowflakeNamespaceIntegrationTest {
    
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
    @DisplayName("Integration: Namespace attributes flow from storage to SQL")
    void shouldIntegrateNamespaceAttributesIntoSql() {
        // Given - Simulate what the parser would do
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        attrs.put("clusterBy", "id");
        attrs.put("changeTracking", "true");
        attrs.put("dataRetentionTimeInDays", "7"); // Should be ignored for transient
        
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When - Generator processes the statement
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then - SQL should contain namespace-based modifications
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        // Verify all attributes were applied
        assertTrue(sql.contains("CREATE TRANSIENT TABLE"), "Should create transient table");
        assertTrue(sql.contains("CLUSTER BY (id)"), "Should include cluster by");
        assertTrue(sql.contains("CHANGE_TRACKING = TRUE"), "Should enable change tracking");
        assertFalse(sql.contains("DATA_RETENTION_TIME_IN_DAYS"), "Should not include retention for transient table");
        
        // Verify storage was cleaned up
        assertNull(SnowflakeNamespaceAttributeStorage.getAttributes("TEST_TABLE"));
    }
    
    @Test
    @DisplayName("Integration: All table type variations")
    void shouldHandleAllTableTypeVariations() {
        String[] tableTypes = {"transient", "volatile", "temporary", "localTemporary", "globalTemporary"};
        String[] expectedPrefixes = {
            "CREATE TRANSIENT TABLE",
            "CREATE VOLATILE TABLE", 
            "CREATE TEMPORARY TABLE",
            "CREATE LOCAL TEMPORARY TABLE",
            "CREATE GLOBAL TEMPORARY TABLE"
        };
        
        for (int i = 0; i < tableTypes.length; i++) {
            // Reset
            SnowflakeNamespaceAttributeStorage.clear();
            
            // Store attribute
            Map<String, String> attrs = new HashMap<>();
            attrs.put(tableTypes[i], "true");
            SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
            
            // Generate SQL
            Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
            String sql = sqls[0].toSql();
            
            // Verify
            assertTrue(sql.contains(expectedPrefixes[i]), 
                "Should create " + tableTypes[i] + " table: " + sql);
        }
    }
    
    @Test
    @DisplayName("Integration: Complex attribute combinations")
    void shouldHandleComplexAttributeCombinations() {
        // Given - Multiple non-conflicting attributes
        Map<String, String> attrs = new HashMap<>();
        attrs.put("clusterBy", "id,name");
        attrs.put("dataRetentionTimeInDays", "30");
        attrs.put("maxDataExtensionTimeInDays", "90");
        attrs.put("changeTracking", "true");
        attrs.put("enableSchemaEvolution", "true");
        attrs.put("defaultDdlCollation", "en-ci");
        attrs.put("copyGrants", "true");
        
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        String sql = sqls[0].toSql();
        
        // Then - All attributes should be present
        assertTrue(sql.contains("CLUSTER BY (id,name)"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 30"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS = 90"));
        assertTrue(sql.contains("CHANGE_TRACKING = TRUE"));
        assertTrue(sql.contains("ENABLE_SCHEMA_EVOLUTION = TRUE"));
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION = 'en-ci'"));
        assertTrue(sql.contains("COPY GRANTS"));
    }
    
    @Test
    @DisplayName("Integration: Namespace attributes override legacy approach")
    void shouldPreferNamespaceOverLegacy() {
        // Given - Both namespace and legacy approaches
        Map<String, String> attrs = new HashMap<>();
        attrs.put("volatile", "true");
        attrs.put("clusterBy", "id");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_TABLE", attrs);
        
        // Also set legacy tablespace
        statement.setTablespace("transient");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        String sql = sqls[0].toSql();
        
        // Then - Namespace should win
        assertTrue(sql.contains("CREATE VOLATILE TABLE"));
        assertFalse(sql.contains("TRANSIENT"));
        assertTrue(sql.contains("CLUSTER BY (id)"));
    }
}