package liquibase;

import liquibase.util.ExpressionMatcher;
import liquibase.util.StringUtils;

import java.util.*;

public class LabelExpression {

    private HashSet<String> labels = new LinkedHashSet<>();
    private String originalString;

    public LabelExpression() {
    }

    public LabelExpression(String... labels) {
        if (labels.length == 1) {
            parseLabelString(labels[0]);
        } else {
            for (String label : labels) {
                parseLabelString(label.toLowerCase());
            }
        }
    }

    public LabelExpression(String labels) {
        if (labels != null) {
            labels = labels.replace("\\", "");
        }
        parseLabelString(labels);
        originalString = labels;
    }

    public LabelExpression(Collection<String> labels) {
        if (labels != null) {
            for (String label : labels) {
                this.labels.add(label.toLowerCase());
            }
        }
    }

    private void parseLabelString(String labels) {
        labels = StringUtils.trimToNull(labels);

        if (labels == null) {
            return;
        }
        for (String label : StringUtils.splitAndTrim(labels, ",")) {
            this.labels.add(label.toLowerCase());
        }

    }

    public boolean add(String label) {
        return this.labels.add(label.toLowerCase());
    }

    public Set<String> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    @Override
    public String toString() {
        if (originalString != null) {
            return originalString;
        }
        return "(" + StringUtils.join(new TreeSet<>(this.labels), "), (") + ")";
    }

    /**
     * Returns true if the passed runtime labels match this label expression
     */
    public boolean matches(Labels runtimeLabels) {
        if ((runtimeLabels == null) || runtimeLabels.isEmpty()) {
            return true;
        }
        if (this.labels.isEmpty()) {
            return true;
        }

        for (String expression : this.labels) {
            if (matches(expression, runtimeLabels)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * Return true if any of the LabelExpression objects match the runtime
     *
     * @param   expressions    Expressions to match against
     * @param   labels         Runtime labels
     * @return  boolean        True if match
     *
     */
    public static boolean matchesAll(Collection<LabelExpression> expressions, LabelExpression labels) {
        if (expressions == null || expressions.isEmpty()) {
            return true;
        }
        if (labels == null || labels.isEmpty()) {
            return true;
        }
        Set<String> labelStrings = labels.getLabels();
        Labels runtimeLabels = new Labels(labelStrings);
        for (LabelExpression expression : expressions) {
            if (!expression.matches(runtimeLabels)) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(String expression, Labels runtimeLabels) {
        return ExpressionMatcher.matches(expression, runtimeLabels.getLabels());
    }

    public boolean isEmpty() {
        return (this.labels == null) || this.labels.isEmpty();
    }

}
