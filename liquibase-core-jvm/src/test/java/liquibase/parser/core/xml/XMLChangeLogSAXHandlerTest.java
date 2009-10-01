package liquibase.parser.core.xml;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.ArrayList;

import liquibase.changelog.ChangeLogParameter;


public class XMLChangeLogSAXHandlerTest {
    private XMLChangeLogSAXHandler handler;

    @Before
    public void setup() {
        handler = new XMLChangeLogSAXHandler(null, null, new ArrayList<ChangeLogParameter>());
    }


    @Test
    public void setParameterValue_doubleSet() {
        handler.setParameter("doubleSet", "originalValue", null);
        handler.setParameter("doubleSet", "newValue", null);

        assertEquals("re-setting a param should not overwrite the value (like how ant works)", "originalValue", handler.getParameterValue("doubleSet"));
    }

    @Test
    public void getParameterValue_systemProperty() {
        assertEquals(System.getProperty("user.name"), handler.getParameterValue("user.name"));
    }
}
