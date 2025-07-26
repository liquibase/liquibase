package liquibase.sqlgenerator.core.snowflake;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateSchemaStatement;

public class CreateSchemaGeneratorSnowflakeTest {

    @Test
    public void testCreateSchemaWithTransientAndComment() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        CreateSchemaGeneratorSnowflake generator = new CreateSchemaGeneratorSnowflake();
        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName("TEST_TRANSIENT_SCHEMA");
        statement.setTransient(true);
        statement.setComment("Transient test schema");
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertThat(sql).isNotNull();
        assertThat(sql).hasSize(1);
        assertThat(sql[0].toSql())
            .isEqualTo("CREATE TRANSIENT SCHEMA TEST_TRANSIENT_SCHEMA COMMENT = 'Transient test schema'");
    }
    
    @Test
    public void testCreateSchemaWithManaged() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        CreateSchemaGeneratorSnowflake generator = new CreateSchemaGeneratorSnowflake();
        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName("TEST_MANAGED_SCHEMA");
        statement.setManaged(true);
        statement.setComment("Managed access schema");
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertThat(sql).isNotNull();
        assertThat(sql).hasSize(1);
        assertThat(sql[0].toSql())
            .isEqualTo("CREATE SCHEMA TEST_MANAGED_SCHEMA WITH MANAGED ACCESS COMMENT = 'Managed access schema'");
    }
    
    @Test
    public void testCreateSchemaWithTransientAndManaged() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        CreateSchemaGeneratorSnowflake generator = new CreateSchemaGeneratorSnowflake();
        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName("TEST_FULL_SCHEMA");
        statement.setTransient(true);
        statement.setManaged(true);
        statement.setComment("Full featured schema");
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertThat(sql).isNotNull();
        assertThat(sql).hasSize(1);
        assertThat(sql[0].toSql())
            .isEqualTo("CREATE TRANSIENT SCHEMA TEST_FULL_SCHEMA WITH MANAGED ACCESS COMMENT = 'Full featured schema'");
    }
    
    @Test
    public void testCreateSchemaWithRetentionTime() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        CreateSchemaGeneratorSnowflake generator = new CreateSchemaGeneratorSnowflake();
        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName("TEST_RETENTION_SCHEMA");
        statement.setDataRetentionTimeInDays("7");
        statement.setComment("Schema with retention");
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertThat(sql).isNotNull();
        assertThat(sql).hasSize(1);
        assertThat(sql[0].toSql())
            .isEqualTo("CREATE SCHEMA TEST_RETENTION_SCHEMA DATA_RETENTION_TIME_IN_DAYS = 7 COMMENT = 'Schema with retention'");
    }
    
    @Test
    public void testCreateSchemaWithOrReplace() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        CreateSchemaGeneratorSnowflake generator = new CreateSchemaGeneratorSnowflake();
        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName("TEST_OR_REPLACE_SCHEMA");
        statement.setOrReplace(true);
        statement.setComment("Test or replace");
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertThat(sql).isNotNull();
        assertThat(sql).hasSize(1);
        assertThat(sql[0].toSql())
            .isEqualTo("CREATE OR REPLACE SCHEMA TEST_OR_REPLACE_SCHEMA COMMENT = 'Test or replace'");
    }
}