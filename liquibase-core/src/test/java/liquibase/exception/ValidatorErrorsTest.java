package liquibase.exception;

import liquibase.database.core.PostgresDatabase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidatorErrorsTest {
    @Test
    public void hasErrors() {
        ValidationErrors errors = new ValidationErrors();
        assertFalse(errors.hasErrors());

        errors.addError("test message");

        assertTrue(errors.hasErrors());

    }

    @Test
    public void checkRequiredField_nullValue() {
        ValidationErrors errors = new ValidationErrors();
        assertFalse(errors.hasErrors());

        errors.checkRequiredField("testField", null);

        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().contains("testField is required"));

    }

    @Test
    public void checkDisallowedField_falseValue() {
        ValidationErrors errors = new ValidationErrors();
        assertFalse(errors.hasErrors());

        errors.checkDisallowedField("ordered", Boolean.FALSE, new PostgresDatabase(), PostgresDatabase.class);

        assertFalse(errors.hasErrors());

    }

    @Test
    public void checkDisallowedField_nullValue() {
        ValidationErrors errors = new ValidationErrors();
        assertFalse(errors.hasErrors());

        errors.checkDisallowedField("ordered", null, new PostgresDatabase(), PostgresDatabase.class);

        assertFalse(errors.hasErrors());

    }

    @Test
    public void checkDisallowedField_trueValue() {
        ValidationErrors errors = new ValidationErrors();
        assertFalse(errors.hasErrors());

        errors.checkDisallowedField("ordered", Boolean.TRUE, new PostgresDatabase(), PostgresDatabase.class);

        assertTrue(errors.getErrorMessages().contains("ordered is not allowed on postgresql"));
    }
}
