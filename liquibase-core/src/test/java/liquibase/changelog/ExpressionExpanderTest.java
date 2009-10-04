package liquibase.changelog;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ExpressionExpanderTest {
    
    private ChangeLogParameters.ExpressionExpander handler;
    private ChangeLogParameters changeLogParameters;

    @Before
    public void setup() {
        changeLogParameters = new ChangeLogParameters();
        this.handler = new ChangeLogParameters.ExpressionExpander(changeLogParameters);
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
        changeLogParameters.set("here", 4);
        assertEquals("A string with one expression 4 set", handler.expandExpressions("A string with one expression ${here} set"));
    }

    @Test
    public void expandExpressions_doubleObjectExpression() {
        changeLogParameters.set("here", 4);
        changeLogParameters.set("there", 15);
        assertEquals("A string with two expressions 4 and 15 set", handler.expandExpressions("A string with two expressions ${here} and ${there} set"));
    }

    @Test
    public void expandExpressions_nomatchExpression() {
        assertEquals("A string no expressions ${notset} set", handler.expandExpressions("A string no expressions ${notset} set"));
        assertEquals("A string no expressions ${notset.orParams} set", handler.expandExpressions("A string no expressions ${notset.orParams} set"));
    }

}
