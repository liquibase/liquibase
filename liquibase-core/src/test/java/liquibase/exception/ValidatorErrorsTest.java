package liquibase.exception;

import static org.junit.Assert.*;
import org.junit.Test;

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
}
