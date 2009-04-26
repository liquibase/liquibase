package liquibase.database.statement.generator;

import org.junit.Test;
import static org.junit.Assert.*;

public class GeneratorValidatorErrorsTest {
    @Test
    public void hasErrors() {
        GeneratorValidationErrors errors = new GeneratorValidationErrors();
        assertFalse(errors.hasErrors());

        errors.addError("test message");

        assertTrue(errors.hasErrors());

    }

    @Test
    public void checkRequiredField_nullValue() {
        GeneratorValidationErrors errors = new GeneratorValidationErrors();
        assertFalse(errors.hasErrors());

        errors.checkRequiredField("testField", null);

        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().contains("testField is required"));

    }
}
