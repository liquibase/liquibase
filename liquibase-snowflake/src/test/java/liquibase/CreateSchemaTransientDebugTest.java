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
public class CreateSchemaTransientDebugTest {
    
    @Test
    @DisplayName("Step 1: Test Change class directly (programmatic)")
    public void debugStep1_ChangeClass() {
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TRANSIENT_SCHEMA");
        change.setTransient(true);
        
        System.out.println("✅ Step 1 - Change class values:");
        System.out.println("  schemaName: " + change.getSchemaName());
        System.out.println("  transient: " + change.getTransient());
        
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
        
        System.out.println("✅ Step 2 - Statement values:");
        System.out.println("  schemaName: " + stmt.getSchemaName());
        System.out.println("  transient: " + stmt.getTransient());
        
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
        
        System.out.println("✅ Step 3 - Generated SQL:");
        System.out.println("  SQL: " + sqls[0].toSql());
        
        // If this contains "CREATE TRANSIENT SCHEMA", SQL generation works
        // If this contains "CREATE SCHEMA ... TRANSIENT", SQL generation has bug
    }
    
    @Test
    @DisplayName("Step 4: Test all steps together")
    public void debugStep4_FullFlow() {
        System.out.println("✅ Step 4 - Full flow test:");
        
        // Create change
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TRANSIENT_SCHEMA");
        change.setTransient(true);
        System.out.println("  Change transient: " + change.getTransient());
        
        // Generate statement
        SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        CreateSchemaStatement stmt = (CreateSchemaStatement) stmts[0];
        System.out.println("  Statement transient: " + stmt.getTransient());
        
        // Generate SQL
        CreateSchemaGeneratorSnowflake gen = new CreateSchemaGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        System.out.println("  Final SQL: " + sqls[0].toSql());
        
        // This will show us exactly where the issue occurs
    }
}