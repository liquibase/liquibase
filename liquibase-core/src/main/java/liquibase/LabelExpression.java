package liquibase;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelExpression {

    private HashSet<String> labels = new HashSet<String>();

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
        parseLabelString(labels);
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
        return "(" + StringUtils.join(new TreeSet(this.labels), "), (") + ")";
    }

    /**
     * Returns true if the passed runtime labels match this label expression
     */
    public boolean matches(Labels runtimeLabels) {
        if (runtimeLabels == null || runtimeLabels.isEmpty()) {
            return true;
        }
        if (this.labels.size() == 0) {
            return true;
        }

        for (String expression : this.labels) {
            if (matches(expression, runtimeLabels)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(String expression, Labels runtimeLabels) {
        if (runtimeLabels.isEmpty()) {
            return true;
        }

        if (expression.trim().equals(":TRUE")) {
            return true;
        }
        if (expression.trim().equals(":FALSE")) {
            return false;
        }

        while (expression.contains("(")) {
            Pattern pattern = Pattern.compile("(.*?)\\((.*?)\\)(.*)");
            Matcher matcher = pattern.matcher(expression);
            if (!matcher.matches()) {
                throw new UnexpectedLiquibaseException("Cannot parse label pattern "+expression);
            }
            String parenExpression = matcher.group(2);

            parenExpression = ":"+String.valueOf(matches(parenExpression, runtimeLabels)).toUpperCase();

            expression = matcher.group(1)+" "+parenExpression+" "+matcher.group(3);
        }

        String[] orSplit = expression.split("\\s+or\\s+");
        if (orSplit.length > 1) {
            for (String split : orSplit) {
                if (matches(split, runtimeLabels)) {
                    return true;
                }
            }
            return false;
        }

        String[] andSplit = expression.split("\\s+and\\s+");
        if (andSplit.length > 1) {
            for (String split : andSplit) {
                if (!matches(split, runtimeLabels)) {
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

        for (String label : runtimeLabels.getLabels()) {
            if (label.equalsIgnoreCase(expression)) {
                if (notExpression) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        if (notExpression) {
            return true;
        } else {
            return false;
        }


    }

    public boolean isEmpty() {
        return this.labels == null || this.labels.size() == 0;
    }

}
