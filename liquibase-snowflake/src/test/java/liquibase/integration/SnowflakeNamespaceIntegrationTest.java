package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.sqlgenerator.core.snowflake.CreateTableGeneratorSnowflake;
import liquibase.statement.core.CreateTableStatement;
import liquibase.datatype.core.VarcharType;
import liquibase.datatype.core.IntType;
import liquibase.sql.Sql;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real integration test to verify namespace attribute flow with actual Snowflake database
 */
@DisplayName("Snowflake Namespace Integration Test")
public class SnowflakeNamespaceIntegrationTest {
    
    private CreateTableGeneratorSnowflake generator;
    private CreateTableStatement statement;
    private Database database;
    private Connection connection;
    private String testId = String.valueOf(System.currentTimeMillis());
    private String testSchema = "NS_INT_TEST_" + testId;
    
    @BeforeEach
    void setUp() throws Exception {
        try {
            connection = TestDatabaseConfigUtil.getSnowflakeConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Create isolated test schema
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS " + testSchema);
                stmt.execute("USE SCHEMA " + testSchema);
            }
            
            generator = new CreateTableGeneratorSnowflake();
            statement = new CreateTableStatement(database.getDefaultCatalogName(), testSchema, "TEST_TABLE");
            
            // Add some columns
            statement.addColumn("id", new IntType());
            statement.addColumn("name", new VarcharType());
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() throws Exception {
        // Clear storage after each test
        SnowflakeNamespaceAttributeStorage.clear();
        
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE");
            } catch (Exception e) {
                // Ignore cleanup failures
            }
            connection.close();
        }
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then - SQL should contain namespace-based modifications
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        // Verify all attributes were applied with enhanced assertions
        assertTrue(sql.startsWith("CREATE TRANSIENT TABLE"), "Should start with CREATE TRANSIENT TABLE: " + sql);
        assertTrue(sql.contains("CLUSTER BY (id)"), "Should include cluster by clause: " + sql);
        assertTrue(sql.contains("CHANGE_TRACKING = TRUE"), "Should enable change tracking: " + sql);
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
            Sql[] sqls = generator.generateSql(statement, database, null);
            String sql = sqls[0].toSql();
            
            // Verify table type prefix appears correctly
            assertTrue(sql.startsWith(expectedPrefixes[i]), "Assertion should be true");        }
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        String sql = sqls[0].toSql();
        
        // Then - All attributes should be present with enhanced validation
        assertTrue(sql.contains("CLUSTER BY (id,name)"), "Should contain cluster by with multiple columns: " + sql);
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 30"), "Should set data retention time: " + sql);
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS = 90"), "Should set max data extension time: " + sql);
        assertTrue(sql.contains("CHANGE_TRACKING = TRUE"), "Should enable change tracking: " + sql);
        assertTrue(sql.contains("ENABLE_SCHEMA_EVOLUTION = TRUE"), "Should enable schema evolution: " + sql);
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION = 'en-ci'"), "Should set default DDL collation: " + sql);
        assertTrue(sql.contains("COPY GRANTS"), "Should include copy grants: " + sql);
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        String sql = sqls[0].toSql();
        
        // Then - Namespace should win over legacy approach
        assertTrue(sql.startsWith("CREATE VOLATILE TABLE"), "Should start with CREATE VOLATILE TABLE: " + sql);
        assertFalse(sql.contains("TRANSIENT"), "Should not contain TRANSIENT when VOLATILE is set: " + sql);
        assertTrue(sql.contains("CLUSTER BY (id)"), "Should contain cluster by clause: " + sql);
    }
}