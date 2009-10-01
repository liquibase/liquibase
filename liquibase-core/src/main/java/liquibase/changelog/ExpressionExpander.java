package liquibase.changelog;

import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionExpander {
    private List<ChangeLogParameter> changeLogParameters;

    public ExpressionExpander(List<ChangeLogParameter> changeLogParameters) {
        this.changeLogParameters = changeLogParameters;
    }

    public String expandExpressions(String text) {
        if (text == null) {
            return null;
        }
        Pattern expressionPattern = Pattern.compile("(\\$\\{[^\\}]+\\})");
        Matcher matcher = expressionPattern.matcher(text);
        String originalText = text;
        while (matcher.find()) {
            String expressionString = originalText.substring(matcher.start(), matcher.end());
            String valueTolookup = expressionString.replaceFirst("\\$\\{", "").replaceFirst("\\}$", "");

            int dotIndex = valueTolookup.indexOf('.');
            ChangeLogParameter value = getParameterValue(valueTolookup);

            if (value != null) {
                text = text.replace(expressionString, value.getValue().toString());
            }
        }
        return text;
    }

    public ChangeLogParameter getParameterValue(String key) {
        for (ChangeLogParameter param : changeLogParameters) {
            if (param.getKey().equalsIgnoreCase(key)) {
                return param;
            }
        }
        return null;
    }

    public void addParameter(ChangeLogParameter value) {
        changeLogParameters.add(value);
    }

}
