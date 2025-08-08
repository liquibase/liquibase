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
        
        
        SnowflakeDatabase database = new SnowflakeDatabase();
        ValidationErrors errors = change.validate(database);
        
        for (String error : errors.getErrorMessages()) {
        }
        
        assertTrue(errors.getErrorMessages().isEmpty(), "Should not have validation errors when unsetDataRetentionTimeInDays is true");
    }

    @Test
    public void testUnsetCommentValidation() {
        AlterSchemaChange change = new AlterSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        change.setUnsetComment(true);
        
        
        SnowflakeDatabase database = new SnowflakeDatabase();
        ValidationErrors errors = change.validate(database);
        
        for (String error : errors.getErrorMessages()) {
        }
        
        assertTrue(errors.getErrorMessages().isEmpty(), "Should not have validation errors when unsetComment is true");
    }
}