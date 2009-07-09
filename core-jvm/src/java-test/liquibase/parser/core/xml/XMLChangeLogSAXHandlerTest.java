package liquibase.parser.core.xml;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;


public class XMLChangeLogSAXHandlerTest {
    private XMLChangeLogSAXHandler handler;

    @Before
    public void setup() {
        handler = new XMLChangeLogSAXHandler(null, null, new HashMap<String, Object>());
    }


    @Test
    public void setParameterValue_doubleSet() {
        handler.setParameterValue("doubleSet", "originalValue");
        handler.setParameterValue("doubleSet", "newValue");

        assertEquals("re-setting a param should not overwrite the value (like how ant works)", "originalValue", handler.getParameterValue("doubleSet"));
    }

    @Test
    public void getParameterValue_systemProperty() {
        assertEquals(System.getProperty("user.name"), handler.getParameterValue("user.name"));
    }
}
