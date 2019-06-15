package liquibase;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates logic for evaluating if a set of runtime contexts matches a context expression string.
 */
public class ContextExpression {

    private HashSet<String> contexts = new HashSet<>();
    private String originalString;

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
        return "(" + StringUtils.join(new TreeSet<String>(this.contexts), "), (") + ")";
    }

    /**
     * Returns true if the passed runtime contexts match this context expression
     */
    public boolean matches(Contexts runtimeContexts) {
        if ((runtimeContexts == null) || runtimeContexts.isEmpty()) {
            return true;
        }
        if (this.contexts.isEmpty()) {
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
        if (runtimeContexts.isEmpty()) {
            return true;
        }

        if (":TRUE".equals(expression.trim())) {
            return true;
        }
        if (":FALSE".equals(expression.trim())) {
            return false;
        }

        while (expression.contains("(")) {
            Pattern pattern = Pattern.compile("(.*?)\\((.*?)\\)(.*)");
            Matcher matcher = pattern.matcher(expression);
            if (!matcher.matches()) {
                throw new UnexpectedLiquibaseException("Cannot parse context pattern "+expression);
            }
            String parenExpression = matcher.group(2);

            parenExpression = ":"+String.valueOf(matches(parenExpression, runtimeContexts)).toUpperCase();

            expression = matcher.group(1)+" "+parenExpression+" "+matcher.group(3);
        }

        String[] orSplit = expression.split("\\s+or\\s+");
        if (orSplit.length > 1) {
            for (String split : orSplit) {
                if (matches(split, runtimeContexts)) {
                    return true;
                }
            }
            return false;
        }

        String[] andSplit = expression.split("\\s+and\\s+");
        if (andSplit.length > 1) {
            for (String split : andSplit) {
                if (!matches(split, runtimeContexts)) {
                    return false;
                }
            }
            return true;
        }


        boolean notExpression = false;
        if (expression.startsWith("!")) {
            notExpression = true;
            expression = expression.substring(1);
        }

        for (String context : runtimeContexts.getContexts()) {
            if (context.equalsIgnoreCase(expression)) {
                return !notExpression;
            }
        }
        return notExpression;


    }

    public boolean isEmpty() {
        return (this.contexts == null) || this.contexts.isEmpty();
    }

    public static boolean matchesAll(Collection<ContextExpression> expressions, Contexts contexts) {
        if ((expressions == null) || expressions.isEmpty()) {
            return true;
        }
        if ((contexts == null) || contexts.isEmpty()) {
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
