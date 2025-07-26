package liquibase.change.core;

import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDomainStatement;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.core.postgresql.CreateDomainGeneratorPostgreSQL;
import org.junit.Test;
import static org.junit.Assert.*;

public class CreateDomainTest {
    
    @Test
    public void testChangeValidation() {
        CreateDomainChange change = new CreateDomainChange();
        PostgresDatabase database = new PostgresDatabase();
        
        // Test missing required fields
        ValidationErrors errors = change.validate(database);
        assertTrue("Should have errors for missing fields", errors.hasErrors());
        assertTrue("Should report missing domainName", 
            errors.getErrorMessages().contains("domainName is required"));
        assertTrue("Should report missing dataType", 
            errors.getErrorMessages().contains("dataType is required"));
        
        // Test with valid data
        change.setDomainName("email_type");
        change.setDataType("VARCHAR(255)");
        errors = change.validate(database);
        assertFalse("Should not have errors with valid data", errors.hasErrors());
    }
    
    @Test
    public void testDatabaseSupport() {
        CreateDomainChange change = new CreateDomainChange();
        
        assertTrue("Should support PostgreSQL", 
            change.supports(new PostgresDatabase()));
        assertFalse("Should not support MySQL", 
            change.supports(new MySQLDatabase()));
    }
    
    @Test
    public void testStatementGeneration() {
        CreateDomainChange change = new CreateDomainChange();
        change.setDomainName("email_type");
        change.setDataType("VARCHAR(255)");
        change.setDefaultValue("'unknown@example.com'");
        change.setNotNull(true);
        change.setCheckConstraint("VALUE ~ '^[^@]+@[^@]+$'");
        change.setConstraintName("email_check");
        
        SqlStatement[] statements = change.generateStatements(new PostgresDatabase());
        assertEquals("Should generate one statement", 1, statements.length);
        
        CreateDomainStatement statement = (CreateDomainStatement) statements[0];
        assertEquals("email_type", statement.getDomainName());
        assertEquals("VARCHAR(255)", statement.getDataType());
        assertEquals("'unknown@example.com'", statement.getDefaultValue());
        assertTrue(statement.getNotNull());
        assertEquals("VALUE ~ '^[^@]+@[^@]+$'", statement.getCheckConstraint());
        assertEquals("email_check", statement.getConstraintName());
    }
    
    @Test
    public void testSqlGeneration() {
        CreateDomainStatement statement = new CreateDomainStatement();
        statement.setDomainName("email_type");
        statement.setDataType("VARCHAR(255)");
        statement.setDefaultValue("'unknown@example.com'");
        statement.setNotNull(true);
        statement.setCheckConstraint("VALUE ~ '^[^@]+@[^@]+$'");
        statement.setConstraintName("email_check");
        
        CreateDomainGeneratorPostgreSQL generator = new CreateDomainGeneratorPostgreSQL();
        PostgresDatabase database = new PostgresDatabase();
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        assertEquals("Should generate one SQL statement", 1, sqls.length);
        
        String sql = sqls[0].toSql();
        System.out.println("Generated SQL: " + sql);
        
        // Verify SQL contains expected elements
        assertTrue("Should contain CREATE DOMAIN", sql.contains("CREATE DOMAIN"));
        assertTrue("Should contain domain name", sql.contains("email_type"));
        assertTrue("Should contain AS VARCHAR(255)", sql.contains("AS VARCHAR(255)"));
        assertTrue("Should contain DEFAULT", sql.contains("DEFAULT 'unknown@example.com'"));
        assertTrue("Should contain NOT NULL", sql.contains("NOT NULL"));
        assertTrue("Should contain CONSTRAINT", sql.contains("CONSTRAINT"));
        assertTrue("Should contain CHECK", sql.contains("CHECK"));
    }
    
    @Test
    public void testMinimalDomain() {
        CreateDomainStatement statement = new CreateDomainStatement();
        statement.setDomainName("simple_type");
        statement.setDataType("INTEGER");
        
        CreateDomainGeneratorPostgreSQL generator = new CreateDomainGeneratorPostgreSQL();
        PostgresDatabase database = new PostgresDatabase();
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        String sql = sqls[0].toSql();
        
        assertEquals("CREATE DOMAIN simple_type AS INTEGER", sql);
    }
    
    @Test
    public void testWithSchema() {
        CreateDomainStatement statement = new CreateDomainStatement();
        statement.setSchemaName("custom_schema");
        statement.setDomainName("my_type");
        statement.setDataType("TEXT");
        
        CreateDomainGeneratorPostgreSQL generator = new CreateDomainGeneratorPostgreSQL();
        PostgresDatabase database = new PostgresDatabase();
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        String sql = sqls[0].toSql();
        
        assertTrue("Should contain schema name", sql.contains("custom_schema.my_type"));
    }
}