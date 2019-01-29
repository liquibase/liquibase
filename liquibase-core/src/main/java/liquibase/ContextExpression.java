package liquibase;

import liquibase.util.ExpressionMatcher;
import liquibase.util.StringUtils;

import java.util.*;

/**
 * Encapsulates logic for evaluating if a set of runtime contexts matches a context expression string.
 */
public class ContextExpression {

    private HashSet<String> contexts = new HashSet<String>();
    private String originalString = null;

    public ContextExpression() {
    }

    public ContextExpression(String... contexts) {
        if (contexts.length == 1) {
            parseContextString(contexts[0]);
        } else {
            for (String context : contexts) {
                parseContextString(context.toLowerCase());
            }
        }
    }

    public ContextExpression(String contexts) {
        parseContextString(contexts);
        this.originalString = contexts;
    }

    public ContextExpression(Collection<String> contexts) {
        if (contexts != null) {
            for (String context : contexts) {
                this.contexts.add(context.toLowerCase());
            }
        }
    }

    private void parseContextString(String contexts) {
        contexts = StringUtils.trimToNull(contexts);

        if (contexts == null) {
            return;
        }
        for (String context : StringUtils.splitAndTrim(contexts, ",")) {
            this.contexts.add(context.toLowerCase());
        }

    }

    public boolean add(String context) {
        return this.contexts.add(context.toLowerCase());
    }

    public Set<String> getContexts() {
        return Collections.unmodifiableSet(contexts);
    }

    @Override
    public String toString() {
        if (originalString != null) {
            return originalString;
        }
        return "(" + StringUtils.join(new TreeSet(this.contexts), "), (") + ")";
    }

    /**
     * Returns true if the passed runtime contexts match this context expression
     */
    public boolean matches(Contexts runtimeContexts) {
        if (runtimeContexts == null || runtimeContexts.isEmpty()) {
            return true;
        }
        if (this.contexts.size() == 0) {
            return true;
        }

        for (String expression : this.contexts) {
            if (matches(expression, runtimeContexts)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(String expression, Contexts runtimeContexts) {
        return ExpressionMatcher.matches(expression, runtimeContexts.getContexts());
    }

    public boolean isEmpty() {
        return this.contexts == null || this.contexts.size() == 0;
    }

    public static boolean matchesAll(Collection<ContextExpression> expressions, Contexts contexts) {
        if (expressions == null || expressions.isEmpty()) {
            return true;
        }
        if (contexts == null || contexts.isEmpty()) {
            return true;
        }
        for (ContextExpression expression : expressions) {
            if (!expression.matches(contexts)) {
                return false;
            }
        }
        return true;
    }
}
