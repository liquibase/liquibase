package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.DropSchemaStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropSchemaGeneratorSnowflake
 */
@DisplayName("DropSchemaGeneratorSnowflake")
public class DropSchemaGeneratorSnowflakeTest {
    
    private final DropSchemaGeneratorSnowflake generator = new DropSchemaGeneratorSnowflake();
    private final SnowflakeDatabase database = new SnowflakeDatabase();
    
    @Test
    @DisplayName("Should only support Snowflake database")
    public void testSupports() {
        DropSchemaStatement statement = new DropSchemaStatement();
        
        assertTrue(generator.supports(statement, database));
        assertFalse(generator.supports(statement, new PostgresDatabase()));
    }
    
    @Test
    @DisplayName("Should generate basic DROP SCHEMA SQL")
    public void testBasicDropSchema() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA TEST_SCHEMA", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP SCHEMA with IF EXISTS")
    public void testDropSchemaWithIfExists() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setIfExists(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA IF EXISTS TEST_SCHEMA", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP SCHEMA with CASCADE")
    public void testDropSchemaWithCascade() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setCascade(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA TEST_SCHEMA CASCADE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP SCHEMA with RESTRICT")
    public void testDropSchemaWithRestrict() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setRestrict(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA TEST_SCHEMA RESTRICT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP SCHEMA with IF EXISTS and CASCADE")
    public void testDropSchemaWithIfExistsAndCascade() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setIfExists(true);
        statement.setCascade(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA IF EXISTS TEST_SCHEMA CASCADE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle schema names with special characters")
    public void testSchemaNameWithSpecialCharacters() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("\"TEST_SCHEMA-WITH-DASHES\"");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA \"\"\"TEST_SCHEMA-WITH-DASHES\"\"\"", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle schema names with spaces")
    public void testSchemaNameWithSpaces() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("\"TEST SCHEMA\"");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA \"\"\"TEST SCHEMA\"\"\"", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle lowercase schema names")
    public void testLowercaseSchemaName() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("test_schema");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA test_schema", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should prioritize CASCADE over RESTRICT when both are set")
    public void testCascadeTakesPrecedenceOverRestrict() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setCascade(true);
        statement.setRestrict(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP SCHEMA TEST_SCHEMA CASCADE", sqls[0].toSql());
    }
}