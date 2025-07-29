package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterTableClusterStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlterTableClusterChangeTest {

    private AlterTableClusterChange change;
    private SnowflakeDatabase database;

    @BeforeEach
    void setUp() {
        change = new AlterTableClusterChange();
        database = new SnowflakeDatabase();
    }

    @Test
    void testGetConfirmationMessageClusterBy() {
        change.setTableName("test_table");
        change.setClusterBy("col1, col2");
        
        String message = change.getConfirmationMessage();
        assertTrue(message.contains("test_table"));
        assertTrue(message.contains("clustering"));
        assertTrue(message.contains("set to (col1, col2)"));
    }

    @Test
    void testGetConfirmationMessageDropClusteringKey() {
        change.setTableName("test_table");
        change.setDropClusteringKey(true);
        
        String message = change.getConfirmationMessage();
        assertTrue(message.contains("test_table"));
        assertTrue(message.contains("clustering"));
        assertTrue(message.contains("key dropped"));
    }

    @Test
    void testGetConfirmationMessageSuspendRecluster() {
        change.setTableName("test_table");
        change.setSuspendRecluster(true);
        
        String message = change.getConfirmationMessage();
        assertTrue(message.contains("test_table"));
        assertTrue(message.contains("clustering"));
        assertTrue(message.contains("suspended"));
    }

    @Test
    void testGetConfirmationMessageResumeRecluster() {
        change.setTableName("test_table");
        change.setResumeRecluster(true);
        
        String message = change.getConfirmationMessage();
        assertTrue(message.contains("test_table"));
        assertTrue(message.contains("clustering"));
        assertTrue(message.contains("resumed"));
    }

    @Test
    void testGenerateStatementsClusterBy() {
        change.setTableName("test_table");
        change.setSchemaName("test_schema");
        change.setCatalogName("test_catalog");
        change.setClusterBy("col1, col2");
        
        SqlStatement[] statements = change.generateStatements(database);
        
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof AlterTableClusterStatement);
        
        AlterTableClusterStatement statement = (AlterTableClusterStatement) statements[0];
        assertEquals("test_table", statement.getTableName());
        assertEquals("test_schema", statement.getSchemaName());
        assertEquals("test_catalog", statement.getCatalogName());
        assertEquals("col1, col2", statement.getClusterBy());
        assertNull(statement.getDropClusteringKey());
        assertNull(statement.getSuspendRecluster());
        assertNull(statement.getResumeRecluster());
    }

    @Test
    void testGenerateStatementsDropClusteringKey() {
        change.setTableName("test_table");
        change.setDropClusteringKey(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        
        assertEquals(1, statements.length);
        AlterTableClusterStatement statement = (AlterTableClusterStatement) statements[0];
        assertEquals("test_table", statement.getTableName());
        assertNull(statement.getClusterBy());
        assertTrue(statement.getDropClusteringKey());
        assertNull(statement.getSuspendRecluster());
        assertNull(statement.getResumeRecluster());
    }

    @Test
    void testGenerateStatementsSuspendRecluster() {
        change.setTableName("test_table");
        change.setSuspendRecluster(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        
        assertEquals(1, statements.length);
        AlterTableClusterStatement statement = (AlterTableClusterStatement) statements[0];
        assertEquals("test_table", statement.getTableName());
        assertNull(statement.getClusterBy());
        assertNull(statement.getDropClusteringKey());
        assertTrue(statement.getSuspendRecluster());
        assertNull(statement.getResumeRecluster());
    }

    @Test
    void testGenerateStatementsResumeRecluster() {
        change.setTableName("test_table");
        change.setResumeRecluster(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        
        assertEquals(1, statements.length);
        AlterTableClusterStatement statement = (AlterTableClusterStatement) statements[0];
        assertEquals("test_table", statement.getTableName());
        assertNull(statement.getClusterBy());
        assertNull(statement.getDropClusteringKey());
        assertNull(statement.getSuspendRecluster());
        assertTrue(statement.getResumeRecluster());
    }

    @Test
    void testValidationMissingTableName() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("tableName is required")));
    }

    @Test
    void testValidationNoOperation() {
        change.setTableName("test_table");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("At least one clustering operation must be specified")));
    }

    @Test
    void testValidationMultipleOperations() {
        change.setTableName("test_table");
        change.setClusterBy("col1");
        change.setDropClusteringKey(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Only one clustering operation allowed")));
    }

    @Test
    void testValidationSingleOperationValid() {
        change.setTableName("test_table");
        change.setClusterBy("col1, col2");
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testGettersAndSetters() {
        // Test all getters and setters
        change.setCatalogName("catalog");
        assertEquals("catalog", change.getCatalogName());
        
        change.setSchemaName("schema");
        assertEquals("schema", change.getSchemaName());
        
        change.setTableName("table");
        assertEquals("table", change.getTableName());
        
        change.setClusterBy("col1, col2");
        assertEquals("col1, col2", change.getClusterBy());
        
        change.setDropClusteringKey(true);
        assertTrue(change.getDropClusteringKey());
        
        change.setSuspendRecluster(true);
        assertTrue(change.getSuspendRecluster());
        
        change.setResumeRecluster(true);
        assertTrue(change.getResumeRecluster());
    }

    @Test
    void testSerializedObjectNamespace() {
        assertEquals(AlterTableClusterChange.STANDARD_CHANGELOG_NAMESPACE, 
                    change.getSerializedObjectNamespace());
    }

    @Test
    void testComplexClusterByExpression() {
        change.setTableName("sales_data");
        change.setClusterBy("YEAR(sale_date), region, customer_id");
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterTableClusterStatement statement = (AlterTableClusterStatement) statements[0];
        
        assertEquals("YEAR(sale_date), region, customer_id", statement.getClusterBy());
    }

    @Test
    void testFalseBooleansIgnored() {
        change.setTableName("test_table");
        change.setClusterBy("col1");
        change.setDropClusteringKey(false);  // Should be ignored
        change.setSuspendRecluster(false);   // Should be ignored
        change.setResumeRecluster(false);    // Should be ignored
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors()); // Should be valid because only clusterBy is set to true operation
    }
}