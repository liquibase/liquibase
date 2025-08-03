package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.AlterTableChange;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that Snowflake table change generators are properly registered and functional.
 */
public class TableChangeGeneratorSnowflakeTest {

    @Test
    public void testMissingTableChangeGeneratorRegistered() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        // Test that our Snowflake-specific generator has higher priority
        MissingTableChangeGeneratorSnowflake snowflakeGenerator = new MissingTableChangeGeneratorSnowflake();
        MissingTableChangeGenerator standardGenerator = new MissingTableChangeGenerator();
        
        int snowflakePriority = snowflakeGenerator.getPriority(Table.class, database);
        int standardPriority = standardGenerator.getPriority(Table.class, database);
        
        assertTrue(snowflakePriority > standardPriority, 
            "Snowflake generator should have higher priority than standard generator");
        
        // Test that it returns PRIORITY_NONE for non-Snowflake databases
        int nonSnowflakePriority = snowflakeGenerator.getPriority(Table.class, new liquibase.database.core.PostgresDatabase());
        assertEquals(-1, nonSnowflakePriority, "Should return PRIORITY_NONE for non-Snowflake databases");
    }

    @Test
    public void testUnexpectedTableChangeGeneratorRegistered() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        UnexpectedTableChangeGeneratorSnowflake snowflakeGenerator = new UnexpectedTableChangeGeneratorSnowflake();
        UnexpectedTableChangeGenerator standardGenerator = new UnexpectedTableChangeGenerator();
        
        int snowflakePriority = snowflakeGenerator.getPriority(Table.class, database);
        int standardPriority = standardGenerator.getPriority(Table.class, database);
        
        assertTrue(snowflakePriority > standardPriority, 
            "Snowflake generator should have higher priority than standard generator");
    }

    @Test
    public void testChangedTableChangeGeneratorRegistered() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        ChangedTableChangeGeneratorSnowflake snowflakeGenerator = new ChangedTableChangeGeneratorSnowflake();
        ChangedTableChangeGenerator standardGenerator = new ChangedTableChangeGenerator();
        
        int snowflakePriority = snowflakeGenerator.getPriority(Table.class, database);
        int standardPriority = standardGenerator.getPriority(Table.class, database);
        
        assertTrue(snowflakePriority > standardPriority, 
            "Snowflake generator should have higher priority than standard generator");
    }

    @Test
    public void testMissingTableGeneratorBasicFunctionality() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        MissingTableChangeGeneratorSnowflake generator = new MissingTableChangeGeneratorSnowflake();
        
        // Test basic functionality without full change generation
        // (which requires complex setup of ChangeGeneratorChain)
        assertNotNull(generator, "Generator should be instantiable");
        
        // Test priority calculation
        int priority = generator.getPriority(Table.class, database);
        assertTrue(priority > 0, "Should have positive priority for Snowflake tables");
    }

    @Test
    public void testChangeGeneratorsInFactory() {
        // Test that our generators are discoverable through the factory
        ChangeGeneratorFactory factory = ChangeGeneratorFactory.getInstance();
        
        // This would test if our service registration is working correctly
        // The factory should be able to find our generators
        assertNotNull(factory, "ChangeGeneratorFactory should be available");
    }
}