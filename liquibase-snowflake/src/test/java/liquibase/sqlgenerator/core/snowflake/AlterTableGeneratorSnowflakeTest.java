package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.AlterTableStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterTableGeneratorSnowflake
 */
@DisplayName("AlterTableGeneratorSnowflake")
public class AlterTableGeneratorSnowflakeTest {
    
    private final AlterTableGeneratorSnowflake generator = new AlterTableGeneratorSnowflake();
    private final SnowflakeDatabase database = new SnowflakeDatabase();
    
    @Test
    @DisplayName("Should only support Snowflake database")
    public void testSupports() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        
        assertTrue(generator.supports(statement, database));
        assertFalse(generator.supports(statement, new PostgresDatabase()));
    }
    
    @Test
    @DisplayName("Should validate required tableName")
    public void testValidation() {
        AlterTableStatement statement = new AlterTableStatement(null, null, null);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("tableName is required")));
        
        // Test with valid tableName
        statement.setTableName("TEST_TABLE");
        errors = generator.validate(statement, database, null);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should generate SQL for basic clustering")
    public void testBasicClusteringGeneration() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setClusterBy("col1,col2");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE CLUSTER BY (col1,col2)", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL for drop clustering key")
    public void testDropClusteringKeyGeneration() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setDropClusteringKey(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE DROP CLUSTERING KEY", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL for suspend recluster")
    public void testSuspendReclusterGeneration() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setSuspendRecluster(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE SUSPEND RECLUSTER", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL for resume recluster")
    public void testResumeReclusterGeneration() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setResumeRecluster(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE RESUME RECLUSTER", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL for single property setting")
    public void testSinglePropertyGeneration() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setSetDataRetentionTimeInDays(30);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 30", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL for multiple property settings")
    public void testMultiplePropertiesGeneration() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setSetDataRetentionTimeInDays(30);
        statement.setSetChangeTracking(true);
        statement.setSetEnableSchemaEvolution(false);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 30, CHANGE_TRACKING = TRUE, ENABLE_SCHEMA_EVOLUTION = FALSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate separate SQL statements for clustering and properties")
    public void testClusteringAndPropertiesSeparate() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setClusterBy("col1,col2");
        statement.setSetDataRetentionTimeInDays(30);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(2, sqls.length);
        
        // First SQL should be clustering
        assertEquals("ALTER TABLE TEST_TABLE CLUSTER BY (col1,col2)", sqls[0].toSql());
        
        // Second SQL should be properties
        assertEquals("ALTER TABLE TEST_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 30", sqls[1].toSql());
    }
    
    @Test
    @DisplayName("Should handle schema and catalog names correctly")
    public void testSchemaAndCatalogHandling() {
        AlterTableStatement statement = new AlterTableStatement("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        statement.setClusterBy("col1");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_CATALOG.TEST_SCHEMA.TEST_TABLE CLUSTER BY (col1)", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle special characters in table names")
    public void testSpecialCharacters() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "\"TEST-TABLE\"");
        statement.setClusterBy("col1");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE \"\"\"TEST-TABLE\"\"\" CLUSTER BY (col1)", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle boundary values for retention time")
    public void testBoundaryValues() {
        // Test minimum value
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setSetDataRetentionTimeInDays(0);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 0", sqls[0].toSql());
        
        // Test maximum value
        statement.setSetDataRetentionTimeInDays(90);
        sqls = generator.generateSql(statement, database, null);
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 90", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle complex clustering expressions")
    public void testComplexClusteringExpressions() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setClusterBy("SUBSTR(name, 1, 3), created_date DESC, id");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE CLUSTER BY (SUBSTR(name, 1, 3), created_date DESC, id)", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate no SQL when no operations are specified")
    public void testNoOperations() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        // No operations set
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(0, sqls.length);
    }
    
    @Test
    @DisplayName("Should prioritize clustering operations correctly")
    public void testClusteringPriority() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setClusterBy("col1,col2");
        statement.setDropClusteringKey(true); // Should not be used due to mutual exclusivity
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE CLUSTER BY (col1,col2)", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle null boolean values correctly")
    public void testNullBooleanHandling() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        statement.setSetChangeTracking(null); // Should be ignored
        statement.setSetDataRetentionTimeInDays(30); // Should be used
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER TABLE TEST_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 30", sqls[0].toSql());
    }
}