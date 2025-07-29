package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.DropWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropWarehouseChange
 */
@DisplayName("DropWarehouseChange")
public class DropWarehouseChangeTest {
    
    private DropWarehouseChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new DropWarehouseChange();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should not support rollback")
    void shouldNotSupportRollback() {
        assertFalse(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should generate basic drop warehouse statement")
    void shouldGenerateBasicDropWarehouseStatement() {
        change.setWarehouseName("TEST_WAREHOUSE");
        
        SqlStatement[] statements = change.generateStatements(database);
        assertEquals(1, statements.length);
        
        DropWarehouseStatement stmt = (DropWarehouseStatement) statements[0];
        assertEquals("TEST_WAREHOUSE", stmt.getWarehouseName());
        assertNull(stmt.getIfExists());
    }
    
    @Test
    @DisplayName("Should generate statement with IF EXISTS")
    void shouldGenerateStatementWithIfExists() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setIfExists(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        DropWarehouseStatement stmt = (DropWarehouseStatement) statements[0];
        
        assertEquals("TEST_WAREHOUSE", stmt.getWarehouseName());
        assertEquals(true, stmt.getIfExists());
    }
    
    @Test
    @DisplayName("Should validate warehouse name is required")
    void shouldValidateWarehouseNameRequired() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("warehouseName is required"));
    }
    
    
    @Test
    @DisplayName("Should pass validation with valid warehouse name")
    void shouldPassValidationWithValidWarehouseName() {
        change.setWarehouseName("VALID_WAREHOUSE");
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.setWarehouseName("TEST_WAREHOUSE");
        
        assertEquals("Warehouse TEST_WAREHOUSE dropped", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should use correct XML namespace")
    void shouldUseCorrectNamespace() {
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", change.getSerializedObjectNamespace());
    }
    
    @Test
    @DisplayName("Should handle null ifExists as false")
    void shouldHandleNullIfExists() {
        change.setWarehouseName("TEST_WAREHOUSE");
        // Don't set ifExists, leave as null
        
        SqlStatement[] statements = change.generateStatements(database);
        DropWarehouseStatement stmt = (DropWarehouseStatement) statements[0];
        
        assertNull(stmt.getIfExists());
    }
    
    @Test
    @DisplayName("Should handle ifExists false explicitly")
    void shouldHandleIfExistsFalse() {
        change.setWarehouseName("TEST_WAREHOUSE");
        change.setIfExists(false);
        
        SqlStatement[] statements = change.generateStatements(database);
        DropWarehouseStatement stmt = (DropWarehouseStatement) statements[0];
        
        assertEquals(false, stmt.getIfExists());
    }
}