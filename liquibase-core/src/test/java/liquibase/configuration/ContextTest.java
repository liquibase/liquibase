package liquibase.configuration;

import junit.framework.TestCase;
import liquibase.exception.UnexpectedLiquibaseException;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class ContextTest {

    private AbstractConfigurationContainer exampleConfiguration;

    @Before
    public void before() {
        System.clearProperty("liquibase.example.propertyBooleanNoDefault");
        System.clearProperty("liquibase.example.propertyBooleanDefaultFalse");
        System.clearProperty("liquibase.example.property.default.true");

        exampleConfiguration = new ExampleContext();
        exampleConfiguration.init(new SystemPropertyProvider());
    }

    @Test
    public void getValue() {
        assertNull(exampleConfiguration.getContainer().getValue("propertyBooleanNoDefault", Boolean.class));
        assertEquals(Boolean.TRUE, exampleConfiguration.getContainer().getValue("propertyBooleanDefaultTrue", Boolean.class));
        assertEquals(Boolean.FALSE, exampleConfiguration.getContainer().getValue("propertyBooleanDefaultFalse", Boolean.class));

    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void setValue_wrongType() {
        exampleConfiguration.getContainer().setValue("propertyBooleanDefaultFalse", 124);
    }

    @Test
    public void getValue_defaultFromSystemProperties() {
        System.setProperty("liquibase.example.propertyBooleanNoDefault", "true");
        System.setProperty("liquibase.example.propertyBooleanDefaultFalse", "true");
        System.setProperty("liquibase.example.property.default.true", "false");
        ExampleContext exampleContext = new ExampleContext();
        exampleContext.init(new SystemPropertyProvider());

        TestCase.assertEquals(Boolean.TRUE, exampleContext.getContainer().getValue("propertyBooleanNoDefault", Boolean.class));
        TestCase.assertEquals(Boolean.TRUE, exampleContext.getContainer().getValue("propertyBooleanDefaultFalse", Boolean.class));
        TestCase.assertEquals(Boolean.FALSE, exampleContext.getContainer().getValue("propertyBooleanDefaultTrue", Boolean.class));
    }

    private static class ExampleContext extends AbstractConfigurationContainer {
        private ExampleContext() {
            super("liquibase.example");

            getContainer().addProperty("propertyBooleanNoDefault", Boolean.class).setDescription("An example boolean property with no default");
            getContainer().addProperty("propertyBooleanDefaultTrue", Boolean.class).setDefaultValue(true).addAlias("property.default.true");
            getContainer().addProperty("propertyBooleanDefaultFalse", Boolean.class).setDefaultValue(false);
        }
    }


}
