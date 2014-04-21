package liquibase;

import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Encapsulates logic for evaluating if a set of runtime contexts matches a context expression string.
 */
public class ContextExpression {

    private HashSet<String> contexts = new HashSet<String>();

    public ContextExpression() {
    }

    public ContextExpression(String... contexts) {
        if (contexts.length == 1) {
            parseContextString(contexts[0]);
        } else {
            for (String context : contexts) {
                this.contexts.add(context.toLowerCase());
            }
        }
    }

    public ContextExpression(String contexts) {
        parseContextString(contexts);
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

    @Override
    public String toString() {
        return StringUtils.join(new TreeSet(this.contexts),",");
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

        for (String context : runtimeContexts.getContexts()) {
            if (this.contexts.contains(context)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.contexts == null || this.contexts.size() == 0;
    }
}
