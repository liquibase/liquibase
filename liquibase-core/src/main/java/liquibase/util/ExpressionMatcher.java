package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common functionality for matching set of Context/Labels against provided expression.
 * Supported syntax:
 * - ! / not - to negate expression/token
 * - and - conjunction operator
 * - or - disjunction operator
 * - () - increase evaluation order priority for sub-expression
 *
 * Examples:
 * "(a and b) or (c and d)"
 * "!a and b"
 *
 * Usage:
 * @see liquibase.LabelExpression
 * @see liquibase.ContextExpression
 */
public final class ExpressionMatcher {

    /** find "(nested_expression)" in "left and (nested_expression) or right" expression */
    private static final Pattern NESTED_EXPRESSION_PATTERN = Pattern.compile("\\([^()]+\\)");

    private ExpressionMatcher() {
        throw new AssertionError("Utility class. Not designed for instantiation");
    }

    /**
     * Test provided {@code expression} against list of {@code items}.
     * Case insensitive.
     *
     * @param expression - expression that will be parsed and evaluated against provided list of items
     * @param items - list of items
     * @return {@code true} if provided list of items satisfy expression criteria. {@code false} otherwise.
     */
    public static boolean matches(String expression, Collection<String> items) {
        expression = StringUtils.trimToEmpty(expression);
        if (items.isEmpty()) {
            return true;
        }

        if (expression.equals(":TRUE")) {
            return true;
        }
        if (expression.equals(":FALSE")) {
            return false;
        }

        while (expression.contains("(")) {
            Matcher matcher = NESTED_EXPRESSION_PATTERN.matcher(expression);
            if (!matcher.find()) {
                throw new UnexpectedLiquibaseException("Cannot parse expression " + expression);
            }

            String left = expression.substring(0, matcher.start());
            String right = expression.substring(matcher.end());
            String nestedExpression = expression.substring(matcher.start() + 1, matcher.end() - 1); // +1/-1 -- exclude captured parenthesis

            expression = left + " :" + String.valueOf(matches(nestedExpression, items)).toUpperCase() + " " + right;
        }

        String[] orSplit = expression.split("\\s+or\\s+");
        if (orSplit.length > 1) {
            for (String split : orSplit) {
                if (matches(split, items)) {
                    return true;
                }
            }
            return false;
        }

        String[] andSplit = expression.split("\\s+and\\s+");
        if (andSplit.length > 1) {
            for (String split : andSplit) {
                if (!matches(split, items)) {
                    return false;
                }
            }
            return true;
        }

        boolean notExpression = false;
        if (expression.startsWith("!")) {
            notExpression = true;
            expression = expression.substring(1).trim();
        } else if (expression.toLowerCase().startsWith("not ")) {
            notExpression = true;
            expression = expression.substring(4).trim();
        }

        if (expression.trim().equals(":TRUE")) {
            return !notExpression;
        }
        if (expression.trim().equals(":FALSE")) {
            return notExpression;
        }

        for (String item : items) {
            if (item.equalsIgnoreCase(expression)) {
                return !notExpression;
            }
        }
        return notExpression;
    }
}
