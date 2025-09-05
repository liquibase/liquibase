package liquibase.statement.core.snowflake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropWarehouseStatement
 */
@DisplayName("DropWarehouseStatement")
public class DropWarehouseStatementTest {
    
    private DropWarehouseStatement statement;
    
    @BeforeEach
    void setUp() {
        statement = new DropWarehouseStatement();
    }
    
    @Test
    @DisplayName("Should initialize with null values")
    void shouldInitializeWithNullValues() {
        assertNull(statement.getWarehouseName());
        assertNull(statement.getIfExists());
    }
    
    @Test
    @DisplayName("Should set and get warehouse name")
    void shouldSetAndGetWarehouseName() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        assertEquals("TEST_WAREHOUSE", statement.getWarehouseName());
    }
    
    @Test
    @DisplayName("Should set and get ifExists")
    void shouldSetAndGetIfExists() {
        statement.setIfExists(true);
        assertEquals(true, statement.getIfExists());
        
        statement.setIfExists(false);
        assertEquals(false, statement.getIfExists());
    }
    
    @Test
    @DisplayName("Should handle null ifExists")
    void shouldHandleNullIfExists() {
        statement.setIfExists(null);
        assertNull(statement.getIfExists());
    }
}