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
    
    @Test
    public void expandExpressions_escapedSimple() {
    	this.handler = new ChangeLogParameters.ExpressionExpander(changeLogParameters, true);
    	
        assertEquals("${user.name}", handler.expandExpressions("${:user.name}"));
    }
    
    @Test
    public void expandExpressions_escapedNonGreedy() {
    	this.handler = new ChangeLogParameters.ExpressionExpander(changeLogParameters, true);
    	
        assertEquals("${user.name}${user.name}", handler.expandExpressions("${:user.name}${:user.name}"));
    }
    
    @Test
    public void expandExpressions_escapedMultipleSimple() {
    	this.handler = new ChangeLogParameters.ExpressionExpander(changeLogParameters, true);
    	
        assertEquals("${user.name} and ${user.name} are literals", 
        		handler.expandExpressions("${:user.name} and ${:user.name} are literals"));
    }
    
    @Test
    public void expandExpressions_escapedMultipleComplex() {
    	this.handler = new ChangeLogParameters.ExpressionExpander(changeLogParameters, true);
    	
        assertEquals("${user.name} and ${user.name} are literals but this isn't: " + System.getProperty("user.name"), 
        		handler.expandExpressions("${:user.name} and ${:user.name} are literals but this isn't: ${user.name}"));
    }
    
    @Test
    public void expandExpressions_escapedBeforeVariable() {
    	this.handler = new ChangeLogParameters.ExpressionExpander(changeLogParameters, true);
    	
    	assertEquals("${user.name} is a literal, " + System.getProperty("user.name") + " is a variable", 
        		handler.expandExpressions("${:user.name} is a literal, ${user.name} is a variable"));
    }
    
    @Test
    public void expandExpressions_escapedAfterVariable() {
    	this.handler = new ChangeLogParameters.ExpressionExpander(changeLogParameters, true);
    	
    	assertEquals(System.getProperty("user.name") + " is a variable, ${user.name} is a literal", 
        		handler.expandExpressions("${user.name} is a variable, ${:user.name} is a literal"));
    }
    
    @Test
    public void expandExpressions_escapedMultipleComplexVariant() {
    	changeLogParameters.set("a", "Value A");
    	changeLogParameters.set("b", "Value B");
    	
    	this.handler = new ChangeLogParameters.ExpressionExpander(changeLogParameters, true);
    	
        assertEquals("Value A is a variable, ${a} and ${b} are literals but this isn't: Value B", 
        		handler.expandExpressions("${a} is a variable, ${:a} and ${:b} are literals but this isn't: ${b}"));
    }

}
