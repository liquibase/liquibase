package liquibase.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExpressionExpanderTest {

    private HashMap<String, Object> replacements;
    private ExpressionExpander.ReplacementProvider replacementProvider;
    private ExpressionExpander handler;

    @Before
    public void setup() {
        this.replacements = new HashMap<>();
        this.replacementProvider = property -> {
            if (replacements.containsKey(property)) {
                return replacements.get(property).toString();
            } else {
                return "${" + property + "}";
            }
        };
        this.handler = new ExpressionExpander(false);
    }

    private String expand(String text) {
        return handler.expandExpressions(text, this.replacementProvider);
    }

    @Test
    public void expandExpressions_nullValue() {
        assertNull(expand(null));
    }

    @Test
    public void expandExpressions_emptyString() {
        assertEquals("", expand(""));
    }

    @Test
    public void expandExpressions_noExpression() {
        assertEquals("A Simple String", expand("A Simple String"));
    }

    @Test
    public void expandExpressions_singleObjectExpression() {
        replacements.put("here", "4");
        assertEquals("A string with one expression 4 set", expand("A string with one expression ${here} set"));
    }

    @Test
    public void expandExpressions_doubleObjectExpression() {
        replacements.put("here", 4);
        replacements.put("there", 15);
        assertEquals("A string with two expressions 4 and 15 set", expand("A string with two expressions ${here} and ${there} set"));
    }

    @Test
    public void expandExpressions_nomatchExpression() {
        assertEquals("A string no expressions ${notset} set", expand("A string no expressions ${notset} set"));
        assertEquals("A string no expressions ${notset.orParams} set", expand("A string no expressions ${notset.orParams} set"));
    }

    @Test
    public void expandExpressions_escapedSimple() {
        ExpressionExpander expressionExpander = new ExpressionExpander(true);
        assertEquals("${user.name}", expressionExpander.expandExpressions("${:user.name}", this.replacementProvider));
    }

    @Test
    public void expandExpressions_escapedNonGreedy() {
        ExpressionExpander expressionExpander = new ExpressionExpander(true);
        assertEquals(
                "${user.name}${user.name}",
                expressionExpander.expandExpressions("${:user.name}${:user.name}", this.replacementProvider)
        );
    }

    @Test
    public void expandExpressions_escapedMultipleSimple() {
        ExpressionExpander expressionExpander = new ExpressionExpander(true);
        assertEquals(
                "${user.name} and ${user.name} are literals",
                expressionExpander.expandExpressions(
                        "${:user.name} and ${:user.name} are literals",
                        this.replacementProvider
                )
        );
    }

    @Test
    public void expandExpressions_escapedMultipleComplex() {
        ExpressionExpander expressionExpander = new ExpressionExpander(true);
        replacements.put("user.name", "liquibase");

        assertEquals(
                "${user.name} and ${user.name} are literals but this isn't: liquibase",
                expressionExpander.expandExpressions(
                        "${:user.name} and ${:user.name} are literals but this isn't: ${user.name}",
                        this.replacementProvider
                )
        );
    }

    @Test
    public void expandExpressions_escapedBeforeVariable() {
        ExpressionExpander expressionExpander = new ExpressionExpander(true);
        replacements.put("user.name", "liquibase");

        assertEquals(
                "${user.name} is a literal, liquibase is a variable",
                expressionExpander.expandExpressions(
                        "${:user.name} is a literal, ${user.name} is a variable",
                        this.replacementProvider
                )
        );
    }

    @Test
    public void expandExpressions_escapedAfterVariable() {
        ExpressionExpander expressionExpander = new ExpressionExpander(true);
        replacements.put("user.name", "liquibase");

        assertEquals(
                "liquibase is a variable, ${user.name} is a literal",
                expressionExpander.expandExpressions(
                        "${user.name} is a variable, ${:user.name} is a literal",
                        this.replacementProvider
                )
        );
    }

    @Test
    public void expandExpressions_escapedMultipleComplexVariant() {
        ExpressionExpander expressionExpander = new ExpressionExpander(true);
        replacements.put("a", "Value A");
        replacements.put("b", "Value B");

        assertEquals(
                "Value A is a variable, ${a} and ${b} are literals but this isn't: Value B",
                expressionExpander.expandExpressions(
                        "${a} is a variable, ${:a} and ${:b} are literals but this isn't: ${b}",
                        this.replacementProvider
                )
        );
    }

    @Test
    public void expandExpressions_escapedNestedProperties() {
        ExpressionExpander expressionExpander = new ExpressionExpander(true);
        replacements.put("a", "Value A");
        replacements.put("Value A", "Value Value A");

        assertEquals(
                "${${a}} = Value Value A",
                expressionExpander.expandExpressions("${:${:a}} = ${${a}}", this.replacementProvider)
        );
    }
}
