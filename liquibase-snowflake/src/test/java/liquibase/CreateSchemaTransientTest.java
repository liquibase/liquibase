package liquibase;

import liquibase.change.core.CreateSchemaChange;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.core.snowflake.CreateSchemaGeneratorSnowflake;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateSchemaStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Debug tests to isolate the TRANSIENT schema issue systematically
 */
@DisplayName("CreateSchema TRANSIENT Debug")
public class CreateSchemaTransientTest {
    
    @Test
    @DisplayName("Step 1: Test Change class directly (programmatic)")
    public void debugStep1_ChangeClass() {
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TRANSIENT_SCHEMA");
        change.setTransient(true);
        
        
        // If this shows transient=true, issue is NOT in Change class
    }
    
    @Test
    @DisplayName("Step 2: Test Statement generation")
    public void debugStep2_Statement() {
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TRANSIENT_SCHEMA");
        change.setTransient(true);
        
        SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        CreateSchemaStatement stmt = (CreateSchemaStatement) stmts[0];
        
        
        // If this shows transient=true, issue is NOT in Statement generation
    }
    
    @Test
    @DisplayName("Step 3: Test SQL generation")
    public void debugStep3_SqlGeneration() {
        CreateSchemaStatement stmt = new CreateSchemaStatement();
        stmt.setSchemaName("TRANSIENT_SCHEMA");
        stmt.setTransient(true);
        
        CreateSchemaGeneratorSnowflake gen = new CreateSchemaGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        
        
        // If this contains "CREATE TRANSIENT SCHEMA", SQL generation works
        // If this contains "CREATE SCHEMA ... TRANSIENT", SQL generation has bug
    }
    
    @Test
    @DisplayName("Step 4: Test all steps together")
    public void debugStep4_FullFlow() {
        
        // Create change
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TRANSIENT_SCHEMA");
        change.setTransient(true);
        
        // Generate statement
        SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        CreateSchemaStatement stmt = (CreateSchemaStatement) stmts[0];
        
        // Generate SQL
        CreateSchemaGeneratorSnowflake gen = new CreateSchemaGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        
        // This will show us exactly where the issue occurs
    }
}