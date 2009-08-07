package liquibase.changelog;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class ExpressionExpanderTest {
    
    private ExpressionExpander handler;

    @Before
    public void setup() {
        this.handler = new ExpressionExpander(new HashMap<String, Object>());
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

}
