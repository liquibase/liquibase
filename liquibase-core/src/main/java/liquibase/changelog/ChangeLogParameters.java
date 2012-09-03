package liquibase.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;
import liquibase.util.StringUtils;

public class ChangeLogParameters {
    static final String ENABLE_ESCAPING_OPT = "liquibase.enableEscaping";

    private static final boolean EnableEscapingOpt = 
        Boolean.valueOf(System.getProperty(ENABLE_ESCAPING_OPT, "false"));

    private List<ChangeLogParameter> changeLogParameters = new ArrayList<ChangeLogParameter>();
    private ExpressionExpander expressionExpander;
    private Database currentDatabase;
    private List<String> currentContexts;

    public ChangeLogParameters() {
        this(null);
    }
    
    public ChangeLogParameters(boolean enableEscaping) {
        this(null, enableEscaping);
    }
    
    public ChangeLogParameters(Database currentDatabase, boolean enableEscaping) {
        for (Map.Entry entry : System.getProperties().entrySet()) {
            changeLogParameters.add(new ChangeLogParameter(entry.getKey().toString(), entry.getValue()));
        }
        
        this.expressionExpander = new ExpressionExpander(this, EnableEscapingOpt || enableEscaping);
        this.currentDatabase = currentDatabase;
        this.currentContexts = new ArrayList<String>();
    }
    
    public ChangeLogParameters(Database currentDatabase) {
    	this(currentDatabase, false);
    }

    public void addContext(String context) {
        this.currentContexts.add(context);
    }

    public void setContexts(Collection<String> contexts) {
        this.currentContexts = new ArrayList<String>();
        if (contexts != null) {
            this.currentContexts.addAll(contexts);
        }
    }

    public void set(String paramter, Object value) {
        changeLogParameters.add(new ChangeLogParameter(paramter, value));
    }

    public void set(String key, String value, String contexts, String databases) {
        changeLogParameters.add(new ChangeLogParameter(key, value, contexts, databases));
    }

    /**
     * Return the value of a parameter
     *
     * @param key Name of the parameter
     * @return The parameter value or null if not found. (Note that null can also be return if it is the parameter value. For
     *         strict parameter existence use {@link #hasValue(String)))
     */
    public Object getValue(String key) {
        ChangeLogParameter parameter = findParameter(key);
        return parameter != null ? parameter.getValue() : null;
    }

    private ChangeLogParameter findParameter(String key) {
        for (ChangeLogParameter param : changeLogParameters) {
            if (param.getKey().equalsIgnoreCase(key) && param.isValid()) {
                return param;
            }
        }
        return null;
    }

    public boolean hasValue(String key) {
        return findParameter(key) != null;
    }

    public String expandExpressions(String string) {
        return expressionExpander.expandExpressions(string);
    }

    private class ChangeLogParameter {
        private String key;
        private Object value;
        private List<String> validContexts;
        private List<String> validDatabases;

        public ChangeLogParameter(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public ChangeLogParameter(String key, Object value, String validContexts, String validDatabases) {
            this(key, value, StringUtils.splitAndTrim(validContexts, ","), StringUtils.splitAndTrim(validDatabases, ","));
        }

        public ChangeLogParameter(String key, Object value, List<String> validContexts, List<String> validDatabases) {
            this.key = key;
            this.value = value;
            this.validContexts = validContexts;
            this.validDatabases = validDatabases;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public List<String> getValidDatabases() {
            return validDatabases;
        }

        public List<String> getValidContexts() {
            return validContexts;
        }

        @Override
        public String toString() {
            return getValue().toString();
        }

        public boolean isValid() {
            boolean isValid = true;
            if (validContexts != null && validContexts.size() > 0) {
                if (ChangeLogParameters.this.currentContexts != null && ChangeLogParameters.this.currentContexts.size() > 0) {
                    isValid = false;
                    for (String currentContext : ChangeLogParameters.this.currentContexts) {
                        if (validContexts.contains(currentContext)) {
                            isValid = true;
                        }
                    }
                }
            }

            if (isValid && validDatabases != null && validDatabases.size() > 0) {
                isValid = validDatabases.contains(currentDatabase.getShortName());
            }

            return isValid;
        }
    }

    protected static class ExpressionExpander {
        private ChangeLogParameters changeLogParameters;
        private final boolean enableEscaping;
        
        public ExpressionExpander(ChangeLogParameters changeLogParameters, boolean enableEscaping) {
            this.changeLogParameters = changeLogParameters;
            this.enableEscaping = enableEscaping;
        }

        public ExpressionExpander(ChangeLogParameters changeLogParameters) {
        	this(changeLogParameters, false);
        }

        public String expandExpressions(String text) {
            if (text == null) {
                return null;
            }

            final String parameterExp = enableEscaping ? "(\\$\\{[^:][^\\}]+\\})" : "(\\$\\{[^\\}]+\\})";

            Pattern expressionPattern = Pattern.compile(parameterExp);
            Matcher matcher = expressionPattern.matcher(text);
            String originalText = text;
            while (matcher.find()) {
                String expressionString = originalText.substring(matcher.start(), matcher.end());
                String valueTolookup = expressionString.replaceFirst("\\$\\{", "").replaceFirst("\\}$", "");

                int dotIndex = valueTolookup.indexOf('.');
                Object value = changeLogParameters.getValue(valueTolookup);

                if (value != null) {
                    text = text.replace(expressionString, value.toString());
                }
            }

            // if enabled, then collapse ${: into ${
            if (enableEscaping) {
                expressionPattern = Pattern.compile("(\\$\\{:[^\\}]+\\})");
                matcher = expressionPattern.matcher(text);

                while (matcher.find()) {
                    String expressionString = text.substring(matcher.start(), matcher.end());
                    text = text.replace(expressionString, "${" + expressionString.substring(3));
                }
            }

            return text;
        }
    }
}
