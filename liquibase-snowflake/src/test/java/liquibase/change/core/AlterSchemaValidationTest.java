package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlterSchemaValidationTest {

    @Test
    public void testUnsetDataRetentionValidation() {
        AlterSchemaChange change = new AlterSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        change.setUnsetDataRetentionTimeInDays(true);
        
        System.out.println("unsetDataRetentionTimeInDays value: " + change.getUnsetDataRetentionTimeInDays());
        System.out.println("Boolean check: " + (change.getUnsetDataRetentionTimeInDays() != null && change.getUnsetDataRetentionTimeInDays()));
        
        SnowflakeDatabase database = new SnowflakeDatabase();
        ValidationErrors errors = change.validate(database);
        
        System.out.println("Validation errors:");
        for (String error : errors.getErrorMessages()) {
            System.out.println("  - " + error);
        }
        
        assertTrue(errors.getErrorMessages().isEmpty(), "Should not have validation errors when unsetDataRetentionTimeInDays is true");
    }

    @Test
    public void testUnsetCommentValidation() {
        AlterSchemaChange change = new AlterSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        change.setUnsetComment(true);
        
        System.out.println("unsetComment value: " + change.getUnsetComment());
        System.out.println("Boolean check: " + (change.getUnsetComment() != null && change.getUnsetComment()));
        
        SnowflakeDatabase database = new SnowflakeDatabase();
        ValidationErrors errors = change.validate(database);
        
        System.out.println("Validation errors:");
        for (String error : errors.getErrorMessages()) {
            System.out.println("  - " + error);
        }
        
        assertTrue(errors.getErrorMessages().isEmpty(), "Should not have validation errors when unsetComment is true");
    }
}