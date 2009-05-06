package liquibase.statement.generator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

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
