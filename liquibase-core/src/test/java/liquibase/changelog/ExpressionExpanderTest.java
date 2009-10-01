package liquibase.changelog;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.ArrayList;

public class ExpressionExpanderTest {
    
    private ExpressionExpander handler;

    @Before
    public void setup() {
        this.handler = new ExpressionExpander(new ArrayList<ChangeLogParameter>());
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
        handler.addParameter(new ChangeLogParameter("here", 4));
        assertEquals("A string with one expression 4 set", handler.expandExpressions("A string with one expression ${here} set"));
    }

    @Test
    public void expandExpressions_doubleObjectExpression() {
        handler.addParameter(new ChangeLogParameter("here", 4));
        handler.addParameter(new ChangeLogParameter("there", 15));
        assertEquals("A string with two expressions 4 and 15 set", handler.expandExpressions("A string with two expressions ${here} and ${there} set"));
    }

    @Test
    public void expandExpressions_nomatchExpression() {
        assertEquals("A string no expressions ${notset} set", handler.expandExpressions("A string no expressions ${notset} set"));
        assertEquals("A string no expressions ${notset.orParams} set", handler.expandExpressions("A string no expressions ${notset.orParams} set"));
    }

}
