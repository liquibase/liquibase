package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateDatabaseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateDatabaseGeneratorSnowflake
 */
@DisplayName("CreateDatabaseGeneratorSnowflake")
public class CreateDatabaseGeneratorSnowflakeTest {
    
    private CreateDatabaseGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new CreateDatabaseGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should validate database name is required")
    void shouldValidateDatabaseNameRequired() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("databaseName is required"));
    }
    
    @Test
    @DisplayName("Should generate basic CREATE DATABASE SQL")
    void shouldGenerateBasicSql() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE DATABASE TEST_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with OR REPLACE")
    void shouldGenerateSqlWithOrReplace() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setOrReplace(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE OR REPLACE DATABASE TEST_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with IF NOT EXISTS")
    void shouldGenerateSqlWithIfNotExists() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setIfNotExists(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE DATABASE IF NOT EXISTS TEST_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with TRANSIENT")
    void shouldGenerateSqlWithTransient() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setTransient(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE TRANSIENT DATABASE TEST_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with CLONE")
    void shouldGenerateSqlWithClone() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("CLONED_DB");
        statement.setCloneFrom("SOURCE_DB");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE DATABASE CLONED_DB CLONE SOURCE_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with all modifiers in correct order")
    void shouldGenerateSqlWithCorrectOrder() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setOrReplace(true);
        statement.setTransient(true);
        statement.setIfNotExists(false); // Can't use with OR REPLACE
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE OR REPLACE TRANSIENT DATABASE TEST_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with retention parameters")
    void shouldGenerateSqlWithRetentionParameters() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setDataRetentionTimeInDays("7");
        statement.setMaxDataExtensionTimeInDays("30");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE DATABASE TEST_DB DATA_RETENTION_TIME_IN_DAYS = 7 MAX_DATA_EXTENSION_TIME_IN_DAYS = 30", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with collation")
    void shouldGenerateSqlWithCollation() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setDefaultDdlCollation("en-ci");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE DATABASE TEST_DB DEFAULT_DDL_COLLATION = 'en-ci'", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with comment")
    void shouldGenerateSqlWithComment() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setComment("Test database");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE DATABASE TEST_DB COMMENT = 'Test database'", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should escape single quotes in comments")
    void shouldEscapeQuotesInComments() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setComment("Test's database");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE DATABASE TEST_DB COMMENT = 'Test''s database'", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with all attributes")
    void shouldGenerateSqlWithAllAttributes() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("FULL_DB");
        statement.setOrReplace(true);
        statement.setTransient(true);
        statement.setDataRetentionTimeInDays("0"); // Must be 0 for transient
        statement.setMaxDataExtensionTimeInDays("0");
        statement.setDefaultDdlCollation("en-ci");
        statement.setComment("Full test database");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals("CREATE OR REPLACE TRANSIENT DATABASE FULL_DB DATA_RETENTION_TIME_IN_DAYS = 0 MAX_DATA_EXTENSION_TIME_IN_DAYS = 0 DEFAULT_DDL_COLLATION = 'en-ci' COMMENT = 'Full test database'", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should validate OR REPLACE and IF NOT EXISTS are mutually exclusive")
    void shouldValidateMutualExclusivity() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setOrReplace(true);
        statement.setIfNotExists(true);
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both OR REPLACE and IF NOT EXISTS")));
    }
    
    @Test
    @DisplayName("Should validate transient database retention")
    void shouldValidateTransientRetention() {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName("TEST_DB");
        statement.setTransient(true);
        statement.setDataRetentionTimeInDays("7");
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Transient databases must have DATA_RETENTION_TIME_IN_DAYS = 0")));
    }
}