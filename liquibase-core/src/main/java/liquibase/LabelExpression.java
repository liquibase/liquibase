package liquibase;

import liquibase.util.ExpressionMatcher;
import liquibase.util.StringUtil;

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
        labels = StringUtil.trimToNull(labels);

        if (labels == null) {
            return;
        }
        for (String label : StringUtil.splitAndTrim(labels, ",")) {
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
        return "(" + StringUtil.join(new TreeSet<>(this.labels), "), (") + ")";
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
     * @param   changesetLabels    Expressions to match against
     * @param   labelExpression         Runtime labels
     * @return  boolean        True if match
     *
     */
    public static boolean matchesAll(Collection<Labels> changesetLabels, LabelExpression labelExpression) {
        if (changesetLabels == null || changesetLabels.isEmpty()) {
            return true;
        }
        if (labelExpression == null || labelExpression.isEmpty()) {
            return true;
        }
        for (Labels changesetLabel : changesetLabels) {
            if (!labelExpression.matches(changesetLabel)) {
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
