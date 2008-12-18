package liquibase.parser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionExpander {
    private Map<String, Object> changeLogParameters;

    public ExpressionExpander(Map<String, Object> changeLogParameters) {
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
            Object value = getParameterValue(valueTolookup);

            if (value != null) {
                text = text.replace(expressionString, value.toString());
            }
        }
        return text;
    }

    public Object getParameterValue(String paramter) {
        return changeLogParameters.get(paramter);
    }

    public void setParameterValue(String paramter, Object value) {
        if (!changeLogParameters.containsKey(paramter)) {
            changeLogParameters.put(paramter, value);
        }
    }

}
