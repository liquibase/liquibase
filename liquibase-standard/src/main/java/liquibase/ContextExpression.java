package liquibase;

import liquibase.util.ExpressionMatcher;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * Encapsulates logic for evaluating if a set of runtime contexts matches a context expression string.
 */
@NoArgsConstructor
@Setter
public class ContextExpression {

    /**
     * Pseudo-context that evaluates to {@code true} when no runtime contexts are specified.
     * <p>
     * This can be used in context expressions to opt into stricter context-matching behaviour while
     * keeping backward compatibility.  For example, a changeset with
     * {@code context="!nocontexts AND specificcontext"} will only be applied when at least one
     * runtime context is provided <em>and</em> that context matches {@code specificcontext}.
     * When no runtime context is specified the {@code nocontexts} pseudo-context evaluates to
     * {@code true}, so {@code !nocontexts} evaluates to {@code false} and the changeset is
     * skipped.
     * </p>
     */
    public static final String NOCONTEXTS = "nocontexts";

    private HashSet<String> contexts = new HashSet<>();
    @Getter
    private String originalString;

    public ContextExpression(String... contexts) {
        if (contexts.length == 1) {
            parseContextString(contexts[0]);
            originalString = contexts[0];
        } else {
            for (String context : contexts) {
                parseContextString(context.toLowerCase());
            }
            originalString = StringUtil.join(contexts, ",");
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
            originalString = StringUtil.join(contexts, ",");
        }
    }

    private void parseContextString(String contexts) {
        contexts = StringUtil.trimToNull(contexts);

        if (contexts == null) {
            return;
        }
        for (String context : StringUtil.splitAndTrim(contexts, ",")) {
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
        return "(" + StringUtil.join(new TreeSet<>(this.contexts), "), (") + ")";
    }

    /**
     * Returns true if the passed runtime contexts match this context expression
     */
    public boolean matches(Contexts runtimeContexts) {
        if (runtimeContexts == null) {
            runtimeContexts = new Contexts();
        }
        // If there are required runtime contexts, we need to evaluate those match as well
        boolean noRequiredRuntime = runtimeContexts.getContexts()
                .stream()
                .noneMatch(context -> context.startsWith("@"));

        if (this.contexts.isEmpty() && noRequiredRuntime) {
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
        if (runtimeContexts == null) {
            runtimeContexts = new Contexts();
        }
        return ExpressionMatcher.matches(expression, runtimeContexts.getContexts());
    }

    public boolean isEmpty() {
        return this.contexts.isEmpty();
    }

    public static boolean matchesAll(Collection<ContextExpression> expressions, Contexts contexts) {
        if ((expressions == null) || expressions.isEmpty()) {
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
