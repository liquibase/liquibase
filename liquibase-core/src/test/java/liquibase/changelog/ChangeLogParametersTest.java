package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.Labels;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.core.H2Database;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ChangeLogParametersTest {

    @Before
    public void before() {
        LiquibaseConfiguration.getInstance().reset();
    }

    @Test
    public void setParameterValue_doubleSet() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();

        changeLogParameters.set("doubleSet", "originalValue");
        changeLogParameters.set("doubleSet", "newValue");

        assertEquals("re-setting a param should not overwrite the value (like how ant works)", "originalValue", changeLogParameters.getValue("doubleSet", null));
    }

    @Test
    public void getParameterValue_systemProperty() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();

        assertEquals(System.getProperty("user.name"), changeLogParameters.getValue("user.name", null));
    }

    @Test
    public void setParameterValue_doubleSetButSecondWrongDatabase() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());

        changeLogParameters.set("doubleSet", "originalValue", new ContextExpression(), new Labels(), "baddb", true, null);
        changeLogParameters.set("doubleSet", "newValue");

        assertEquals("newValue", changeLogParameters.getValue("doubleSet", null));
    }

    @Test
    public void setParameterValue_multiDatabase() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());

        changeLogParameters.set("doubleSet", "originalValue", new ContextExpression(), new Labels(), "baddb, h2", true, null);

        assertEquals("originalValue", changeLogParameters.getValue("doubleSet", null));
    }

    @Test
    public void setParameterValue_rightDBWrongContext() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        changeLogParameters.setContexts(new Contexts("junit"));

        changeLogParameters.set("doubleSet", "originalValue", "anotherContext", "anotherLabel", "baddb, h2", true, null);

        assertNull(changeLogParameters.getValue("doubleSet", null));
    }
   @Test
    public void setParameterValue_rightDBRightContext() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        changeLogParameters.setContexts(new Contexts("junit"));

        changeLogParameters.set("doubleSet", "originalValue", "junit", "junitLabel", "baddb, h2", true, null);

        assertEquals("originalValue", changeLogParameters.getValue("doubleSet", null));
    }
}