package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.DropDatabaseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropDatabaseGeneratorSnowflake
 */
@DisplayName("DropDatabaseGeneratorSnowflake")
public class DropDatabaseGeneratorSnowflakeTest {
    
    private DropDatabaseGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new DropDatabaseGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should validate database name is required")
    void shouldValidateDatabaseNameRequired() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("databaseName is required"));
    }
    
    @Test
    @DisplayName("Should generate basic DROP DATABASE SQL")
    void shouldGenerateBasicSql() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("DROP DATABASE TEST_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with IF EXISTS")
    void shouldGenerateSqlWithIfExists() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setIfExists(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("DROP DATABASE IF EXISTS TEST_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with CASCADE")
    void shouldGenerateSqlWithCascade() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setCascade(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("DROP DATABASE TEST_DB CASCADE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with RESTRICT")
    void shouldGenerateSqlWithRestrict() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setRestrict(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("DROP DATABASE TEST_DB RESTRICT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with IF EXISTS and CASCADE")
    void shouldGenerateSqlWithIfExistsAndCascade() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setIfExists(true);
        statement.setCascade(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("DROP DATABASE IF EXISTS TEST_DB CASCADE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with IF EXISTS and RESTRICT")
    void shouldGenerateSqlWithIfExistsAndRestrict() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setIfExists(true);
        statement.setRestrict(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("DROP DATABASE IF EXISTS TEST_DB RESTRICT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should validate CASCADE and RESTRICT are mutually exclusive")
    void shouldValidateMutualExclusivity() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setCascade(true);
        statement.setRestrict(true);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both CASCADE and RESTRICT")));
    }
    
    @Test
    @DisplayName("Should not add CASCADE or RESTRICT when neither is set")
    void shouldNotAddCascadeOrRestrictWhenNeitherSet() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("DROP DATABASE TEST_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should escape database name")
    void shouldEscapeDatabaseName() {
        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName("TEST-DB");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("DROP DATABASE \"TEST-DB\"", sqls[0].toSql());
    }
}