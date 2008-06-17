package liquibase.parser.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class XMLChangeLogHandlerTest {
    private XMLChangeLogHandler handler;

    @Before
    public void setup() {
        handler = new XMLChangeLogHandler(null, null);
    }

    @Test
    public void expandExpressions_nullValue() {
        assertNull(handler.expandExpressions(null));
    }

    @Test
    public void expandExpressions_emptyString() {
        assertEquals("", handler.expandExpressions(""));
    }

    @Test
    public void expandExpressions_noExpression() {
        assertEquals("A Simple String", handler.expandExpressions("A Simple String"));
    }

    @Test
    public void expandExpressions_singleObjectExpression() {
        handler.setParameterValue("here", 4);
        assertEquals("A string with one expression 4 set", handler.expandExpressions("A string with one expression ${here} set"));
    }

    @Test
    public void expandExpressions_doubleObjectExpression() {
        handler.setParameterValue("here", 4);
        handler.setParameterValue("there", 15);
        assertEquals("A string with two expressions 4 and 15 set", handler.expandExpressions("A string with two expressions ${here} and ${there} set"));
    }

    @Test
    public void expandExpressions_nomatchExpression() {
        assertEquals("A string no expressions ${notset} set", handler.expandExpressions("A string no expressions ${notset} set"));
        assertEquals("A string no expressions ${notset.orParams} set", handler.expandExpressions("A string no expressions ${notset.orParams} set"));
    }

    @Test
    public void doubleSet() {
        handler.setParameterValue("doubleSet", "originalValue");
        handler.setParameterValue("doubleSet", "newValue");

        assertEquals("re-setting a param should not overwrite the value (like how ant works)", "originalValue", handler.getParameterValue("doubleSet"));
    }
}
