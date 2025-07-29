package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.snowflake.DropWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DropWarehouseGeneratorSnowflake
 */
@DisplayName("DropWarehouseGeneratorSnowflake")
public class DropWarehouseGeneratorSnowflakeTest {
    
    private DropWarehouseGeneratorSnowflake generator;
    private DropWarehouseStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new DropWarehouseGeneratorSnowflake();
        statement = new DropWarehouseStatement();
        
        // Setup database mock
        when(database.escapeObjectName("TEST_WAREHOUSE", liquibase.structure.core.Table.class))
            .thenReturn("TEST_WAREHOUSE");
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should validate warehouse name is required")
    void shouldValidateWarehouseNameRequired() {
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"));
    }
    
    @Test
    @DisplayName("Should validate empty warehouse name")
    void shouldValidateEmptyWarehouseName() {
        statement.setWarehouseName("");
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"));
    }
    
    @Test
    @DisplayName("Should validate whitespace warehouse name")
    void shouldValidateWhitespaceWarehouseName() {
        statement.setWarehouseName("   ");
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"));
    }
    
    @Test
    @DisplayName("Should generate basic DROP WAREHOUSE SQL")
    void shouldGenerateBasicDropWarehouseSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP WAREHOUSE TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP WAREHOUSE IF EXISTS SQL")
    void shouldGenerateDropWarehouseIfExistsSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setIfExists(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP WAREHOUSE IF EXISTS TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle null ifExists as no IF EXISTS clause")
    void shouldHandleNullIfExists() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setIfExists(null);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP WAREHOUSE TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle false ifExists as no IF EXISTS clause")
    void shouldHandleFalseIfExists() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setIfExists(false);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP WAREHOUSE TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should escape warehouse name")
    void shouldEscapeWarehouseName() {
        when(database.escapeObjectName("SPECIAL-WAREHOUSE", liquibase.structure.core.Table.class))
            .thenReturn("\"SPECIAL-WAREHOUSE\"");
        
        statement.setWarehouseName("SPECIAL-WAREHOUSE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP WAREHOUSE \"SPECIAL-WAREHOUSE\"", sqls[0].toSql());
    }
}