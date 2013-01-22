package liquibase.changelog;

import static liquibase.Liquibase.ENABLE_CHANGELOG_PROP_ESCAPING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;
import liquibase.util.StringUtils;

public class ChangeLogParameters {
	
	public static final boolean EnableEscaping;
	
	static {
		String enableEscaping = System.getProperty(ENABLE_CHANGELOG_PROP_ESCAPING, "false");
		EnableEscaping = Boolean.valueOf(enableEscaping);
	}

    private List<ChangeLogParameter> changeLogParameters = new ArrayList<ChangeLogParameter>();
    private ExpressionExpander expressionExpander;
    private Database currentDatabase;
    private List<String> currentContexts;

    public ChangeLogParameters() {
        this(null);
    }

    public ChangeLogParameters(Database currentDatabase) {
        for (Map.Entry entry : System.getProperties().entrySet()) {
            changeLogParameters.add(new ChangeLogParameter(entry.getKey().toString(), entry.getValue()));
        }
        
        this.expressionExpander = new ExpressionExpander(this, EnableEscaping);
        this.currentDatabase = currentDatabase;
        this.currentContexts = new ArrayList<String>();
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
    	private boolean enableEscaping;
        private ChangeLogParameters changeLogParameters;

        public ExpressionExpander(ChangeLogParameters changeLogParameters) {
            this(changeLogParameters, false);
        }
        
        public ExpressionExpander(ChangeLogParameters changeLogParameters, boolean enableEscaping) {
            this.changeLogParameters = changeLogParameters;
            this.enableEscaping = enableEscaping;
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

                Object value = enableEscaping && valueTolookup.startsWith(":") 
                		? null 
                		: changeLogParameters.getValue(valueTolookup);

                if (value != null) {
                    text = text.replace(expressionString, value.toString());
                }
            }
            
            // replace all escaped expressions with its literal
            if (enableEscaping) {
            	text = text.replaceAll("\\$\\{:(.+?)}", "\\$\\{$1}");
            }
            
            return text;
        }
    }
}
