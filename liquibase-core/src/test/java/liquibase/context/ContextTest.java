package liquibase.context;

import junit.framework.TestCase;
import liquibase.exception.UnexpectedLiquibaseException;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNull;

public class ContextTest {

    private Context exampleContext;

    @Before
    public void before() {
        System.clearProperty("liquibase.example.propertyBooleanNoDefault");
        System.clearProperty("liquibase.example.propertyBooleanDefaultFalse");
        System.clearProperty("liquibase.example.property.default.true");

        exampleContext = new ExampleContext();
        exampleContext.init(new SystemPropertyValueContainer());
    }

    @Test
    public void getValue() {
        assertNull(exampleContext.getState().getValue("propertyBooleanNoDefault", Boolean.class));
        assertEquals(Boolean.TRUE, exampleContext.getState().getValue("propertyBooleanDefaultTrue", Boolean.class));
        assertEquals(Boolean.FALSE, exampleContext.getState().getValue("propertyBooleanDefaultFalse", Boolean.class));

    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void setValue_wrongType() {
        exampleContext.getState().setValue("propertyBooleanDefaultFalse", 124);
    }

    @Test
    public void getValue_defaultFromSystemProperties() {
        System.setProperty("liquibase.example.propertyBooleanNoDefault", "true");
        System.setProperty("liquibase.example.propertyBooleanDefaultFalse", "true");
        System.setProperty("liquibase.example.property.default.true", "false");
        ExampleContext exampleContext = new ExampleContext();
        exampleContext.init(new SystemPropertyValueContainer());

        TestCase.assertEquals(Boolean.TRUE, exampleContext.getState().getValue("propertyBooleanNoDefault", Boolean.class));
        TestCase.assertEquals(Boolean.TRUE, exampleContext.getState().getValue("propertyBooleanDefaultFalse", Boolean.class));
        TestCase.assertEquals(Boolean.FALSE, exampleContext.getState().getValue("propertyBooleanDefaultTrue", Boolean.class));
    }

    private static class ExampleContext extends Context {
        private ExampleContext() {
            super("liquibase.example");

            getState().addProperty("propertyBooleanNoDefault", Boolean.class).setDescription("An example boolean property with no default");
            getState().addProperty("propertyBooleanDefaultTrue", Boolean.class).setDefaultValue(true).addAlias("property.default.true");
            getState().addProperty("propertyBooleanDefaultFalse", Boolean.class).setDefaultValue(false);
        }

    }


}
