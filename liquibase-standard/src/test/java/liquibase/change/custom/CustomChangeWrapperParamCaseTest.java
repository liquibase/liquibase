package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CustomChangeWrapperParamCaseTest {

    @Test
    public void paramWithInitialUppercaseReachesTheSetter() throws Exception {
        // changelogs written for versions before 4.29.0 use <param name="SchemaName"> for setSchemaName
        LegacyParameterChange custom = configure("SchemaName", "TEST_SCHEMA");

        assertEquals("TEST_SCHEMA", custom.schemaName);
    }

    @Test
    public void paramWithInitialLowercaseStillReachesTheSetter() throws Exception {
        LegacyParameterChange custom = configure("schemaName", "TEST_SCHEMA");

        assertEquals("TEST_SCHEMA", custom.schemaName);
    }

    @Test
    public void validationAcceptsInitialUppercaseParam() throws Exception {
        CustomChangeWrapper wrapper = wrapper("PlSqlBlock", "begin null; end;");

        ValidationErrors errors = wrapper.validate(new H2Database());

        assertFalse(errors.toString(), errors.hasErrors());
    }

    private static LegacyParameterChange configure(String name, String value) throws Exception {
        CustomChangeWrapper wrapper = wrapper(name, value);
        wrapper.validate(new H2Database());
        return (LegacyParameterChange) wrapper.getCustomChange();
    }

    private static CustomChangeWrapper wrapper(String name, String value) throws Exception {
        CustomChangeWrapper wrapper = new CustomChangeWrapper();
        wrapper.setClass(LegacyParameterChange.class.getName());
        wrapper.setParam(name, value);
        return wrapper;
    }

    public static class LegacyParameterChange implements CustomTaskChange {
        private String schemaName;
        private String plSqlBlock;

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public void setPlSqlBlock(String plSqlBlock) {
            this.plSqlBlock = plSqlBlock;
        }

        @Override
        public void execute(Database database) throws CustomChangeException {
        }

        @Override
        public String getConfirmationMessage() {
            return null;
        }

        @Override
        public void setUp() throws SetupException {
        }

        @Override
        public void setFileOpener(ResourceAccessor resourceAccessor) {
        }

        @Override
        public ValidationErrors validate(Database database) {
            return new ValidationErrors();
        }
    }
}
