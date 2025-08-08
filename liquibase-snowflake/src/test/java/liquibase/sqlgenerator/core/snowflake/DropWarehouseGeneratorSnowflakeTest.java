package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.snowflake.DropWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for DropWarehouseGeneratorSnowflake SQL generation
 * Tests SQL string output without database dependencies - NO MOCKING!
 */
@DisplayName("DropWarehouseGeneratorSnowflake - Pure SQL Tests")
public class DropWarehouseGeneratorSnowflakeTest {
    
    private DropWarehouseGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new DropWarehouseGeneratorSnowflake();
        database = new SnowflakeDatabase(); // Real database object, no mocking needed
    }
    
    @Test
    @DisplayName("Should generate basic DROP WAREHOUSE SQL")
    void testBasicDropWarehouse() {
        // Given
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP WAREHOUSE TEST_WH", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP WAREHOUSE IF EXISTS SQL")
    void testDropWarehouseIfExists() {
        // Given
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setIfExists(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP WAREHOUSE IF EXISTS TEST_WH", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should validate warehouse name is required")
    void testValidationRequiresWarehouseName() {
        // Given
        DropWarehouseStatement statement = new DropWarehouseStatement();
        // No warehouse name set
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"));
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void testSupportsSnowflakeDatabase() {
        // Given
        DropWarehouseStatement statement = new DropWarehouseStatement();
        
        // When/Then
        assertTrue(generator.supports(statement, database));
    }
}