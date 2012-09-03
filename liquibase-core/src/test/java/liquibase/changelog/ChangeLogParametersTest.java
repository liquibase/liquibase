package liquibase.changelog;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.core.H2Database;

import java.util.Arrays;
import static java.lang.String.format;


public class ChangeLogParametersTest {

    @Test
    public void setParameterValue_doubleSet() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();

        changeLogParameters.set("doubleSet", "originalValue");
        changeLogParameters.set("doubleSet", "newValue");

        assertEquals("re-setting a param should not overwrite the value (like how ant works)", "originalValue", changeLogParameters.getValue("doubleSet"));
    }

    @Test 
    public void getParameterValue_escaped_simple() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(true);
        
        String expanded = changeLogParameters.expandExpressions("${:user.name}");
        assertEquals("${user.name}", expanded);
    }

    @Test 
    public void getParameterValue_escaped_and_undescaped() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(true);
        
        String expanded = changeLogParameters.expandExpressions("${user.name} != ${:user.name}");
        assertEquals(format("%s != ${user.name}", changeLogParameters.getValue("user.name")), expanded);
    }
        
    @Test 
    public void getParameterValue_escaped_complex() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(true);
        
        String expanded = changeLogParameters.expandExpressions("${user.name} != ${:user.name} but does equal ${user.name}");
        assertEquals(format("%s != ${user.name} but does equal %s", changeLogParameters.getValue("user.name"), changeLogParameters.getValue("user.name")), expanded);
    }

    @Test
    public void getParameterValue_systemProperty() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();

        assertEquals(System.getProperty("user.name"), changeLogParameters.getValue("user.name"));
    }

    @Test
    public void setParameterValue_doubleSetButSecondWrongDatabase() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());

        changeLogParameters.set("doubleSet", "originalValue", null, "baddb");
        changeLogParameters.set("doubleSet", "newValue");

        assertEquals("newValue", changeLogParameters.getValue("doubleSet"));
    }

    @Test
    public void setParameterValue_multiDatabase() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());

        changeLogParameters.set("doubleSet", "originalValue", null, "baddb, h2");

        assertEquals("originalValue", changeLogParameters.getValue("doubleSet"));
    }

    @Test
    public void setParameterValue_rightDBWrongContext() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        changeLogParameters.setContexts(Arrays.asList("junit"));

        changeLogParameters.set("doubleSet", "originalValue", "anotherContext", "baddb, h2");

        assertNull(changeLogParameters.getValue("doubleSet"));
    }
   @Test
    public void setParameterValue_rightDBRightContext() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        changeLogParameters.setContexts(Arrays.asList("junit"));

        changeLogParameters.set("doubleSet", "originalValue", "junit", "baddb, h2");

        assertEquals("originalValue", changeLogParameters.getValue("doubleSet"));
    }
}
