package liquibase;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.RenameTableGenerator;
import liquibase.sqlgenerator.core.RenameTableGeneratorSnowflake;
import liquibase.statement.core.RenameTableStatement;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;

public class RenameTableGeneratorSelectionTest {
    
    @Test
    public void testGeneratorSelection() {
        Database database = new SnowflakeDatabase();
        RenameTableStatement statement = new RenameTableStatement(null, null, "old_table", "new_table");
        
        System.out.println("Database class: " + database.getClass().getName());
        System.out.println("Database short name: " + database.getShortName());
        
        SqlGeneratorFactory factory = SqlGeneratorFactory.getInstance();
        SortedSet<SqlGenerator> generators = factory.getGenerators(statement, database);
        
        System.out.println("\nAll applicable generators for RenameTableStatement on Snowflake:");
        for (SqlGenerator gen : generators) {
            System.out.println("  - " + gen.getClass().getName() + 
                             " (priority: " + gen.getPriority() + 
                             ", supports: " + gen.supports(statement, database) + ")");
        }
        
        // Test each generator directly
        System.out.println("\nDirect generator tests:");
        RenameTableGenerator baseGen = new RenameTableGenerator();
        System.out.println("Base generator supports Snowflake: " + baseGen.supports(statement, database));
        System.out.println("Base generator priority: " + baseGen.getPriority());
        
        RenameTableGeneratorSnowflake snowGen = new RenameTableGeneratorSnowflake();
        System.out.println("Snowflake generator supports Snowflake: " + snowGen.supports(statement, database));
        System.out.println("Snowflake generator priority: " + snowGen.getPriority());
    }
}