package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.AlterTableClusterStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlterTableClusterGeneratorSnowflakeTest {

    private AlterTableClusterGeneratorSnowflake generator;
    private SnowflakeDatabase database;

    @BeforeEach
    void setUp() {
        generator = new AlterTableClusterGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }

    @Test
    void testSupports() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, "test_table");
        
        assertTrue(generator.supports(statement, database));
        assertFalse(generator.supports(statement, new liquibase.database.core.PostgresDatabase()));
    }

    @Test
    void testPriority() {
        assertEquals(generator.PRIORITY_DATABASE, generator.getPriority());
    }

    @Test
    void testGenerateSqlClusterBy() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement("catalog", "schema", "test_table");
        statement.setClusterBy("col1, col2");

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("ALTER TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertTrue(sqlText.contains("CLUSTER BY (col1, col2)"));
    }

    @Test
    void testGenerateSqlDropClusteringKey() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, "schema", "test_table");
        statement.setDropClusteringKey(true);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("ALTER TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertTrue(sqlText.contains("DROP CLUSTERING KEY"));
    }

    @Test
    void testGenerateSqlSuspendRecluster() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, "test_table");
        statement.setSuspendRecluster(true);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("ALTER TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertTrue(sqlText.contains("SUSPEND RECLUSTER"));
    }

    @Test
    void testGenerateSqlResumeRecluster() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, "test_table");
        statement.setResumeRecluster(true);

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("ALTER TABLE"));
        assertTrue(sqlText.contains("test_table"));
        assertTrue(sqlText.contains("RESUME RECLUSTER"));
    }

    @Test
    void testValidationMissingTableName() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, null);
        statement.setClusterBy("col1");

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("tableName is required")));
    }

    @Test
    void testValidationNoOperation() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, "test_table");

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("At least one clustering operation must be specified")));
    }

    @Test
    void testValidationMultipleOperations() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, "test_table");
        statement.setClusterBy("col1");
        statement.setDropClusteringKey(true);

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Only one clustering operation allowed")));
    }

    @Test
    void testValidationSingleOperationValid() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, "test_table");
        statement.setClusterBy("col1, col2");

        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testTableNameEscaping() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement("my_catalog", "my_schema", "my_table");
        statement.setClusterBy("col1");

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        // The exact escaping format depends on SnowflakeDatabase implementation
        assertTrue(sqlText.contains("my_table"));
        assertTrue(sqlText.contains("CLUSTER BY (col1)"));
    }

    @Test
    void testComplexClusterByExpression() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, "sales_data");
        statement.setClusterBy("YEAR(sale_date), region, UPPER(customer_type)");

        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CLUSTER BY (YEAR(sale_date), region, UPPER(customer_type))"));
    }

    @Test
    void testFalseBooleansIgnored() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(null, null, "test_table");
        statement.setClusterBy("col1");
        statement.setDropClusteringKey(false);  // Should be ignored
        statement.setSuspendRecluster(false);   // Should be ignored
        statement.setResumeRecluster(false);    // Should be ignored

        ValidationErrors errors = generator.validate(statement, database, null);
        assertFalse(errors.hasErrors()); // Should be valid because only clusterBy is active

        Sql[] sql = generator.generateSql(statement, database, null);
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CLUSTER BY (col1)"));
        assertFalse(sqlText.contains("DROP CLUSTERING KEY"));
        assertFalse(sqlText.contains("SUSPEND RECLUSTER"));
        assertFalse(sqlText.contains("RESUME RECLUSTER"));
    }

    @Test
    void testAffectedTable() {
        AlterTableClusterStatement statement = new AlterTableClusterStatement("cat", "sch", "tbl");
        
        liquibase.structure.core.Table table = generator.getAffectedTable(statement);
        
        assertEquals("cat", table.getSchema().getCatalogName());
        assertEquals("sch", table.getSchema().getName());
        assertEquals("tbl", table.getName());
    }
}