package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.parser.ChangeLogParserCofiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.exception.DatabaseException;
import liquibase.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeLogParameters {
	
    private List<ChangeLogParameter> changeLogParameters = new ArrayList<ChangeLogParameter>();
    private ExpressionExpander expressionExpander;
    private Database currentDatabase;
    private Contexts currentContexts;
    private LabelExpression currentLabelExpression;

    public ChangeLogParameters() {
        this(null);
    }

    public ChangeLogParameters(Database database) {
        for (Map.Entry entry : new HashSet<Map.Entry>(System.getProperties().entrySet())) {
            changeLogParameters.add(new ChangeLogParameter(entry.getKey().toString(), entry.getValue()));
        }

        if (database != null) {
            this.set("database.autoIncrementClause", database.getAutoIncrementClause(null, null));
            this.set("database.currentDateTimeFunction", database.getCurrentDateTimeFunction());
            this.set("database.databaseChangeLogLockTableName", database.getDatabaseChangeLogLockTableName());
            this.set("database.databaseChangeLogTableName", database.getDatabaseChangeLogTableName());
            try {
                this.set("database.databaseMajorVersion", database.getDatabaseMajorVersion());
            } catch (DatabaseException ignore) {
            }
            try {
                this.set("database.databaseMinorVersion", database.getDatabaseMinorVersion());
            } catch (DatabaseException ignore) {
            }
            this.set("database.databaseProductName", database.getDatabaseProductName());
            try {
                this.set("database.databaseProductVersion", database.getDatabaseProductVersion());
            } catch (DatabaseException ignore) {
            }
            this.set("database.defaultCatalogName", database.getDefaultCatalogName());
            this.set("database.defaultSchemaName", database.getDefaultSchemaName());
            this.set("database.defaultSchemaNamePrefix", StringUtils.trimToNull(database.getDefaultSchemaName()) == null ? "" : "." + database.getDefaultSchemaName());
            this.set("database.lineComment", database.getLineComment());
            this.set("database.liquibaseSchemaName", database.getLiquibaseSchemaName());
            this.set("database.typeName", database.getShortName());
            try {
                this.set("database.isSafeToRunUpdate", database.isSafeToRunUpdate());
            } catch (DatabaseException ignore) {
            }
            this.set("database.requiresPassword", database.requiresPassword());
            this.set("database.requiresUsername", database.requiresUsername());
            this.set("database.supportsForeignKeyDisable", database.supportsForeignKeyDisable());
            this.set("database.supportsInitiallyDeferrableColumns", database.supportsInitiallyDeferrableColumns());
            this.set("database.supportsRestrictForeignKeys", database.supportsRestrictForeignKeys());
            this.set("database.supportsSchemas", database.supportsSchemas());
            this.set("database.supportsSequences", database.supportsSequences());
            this.set("database.supportsTablespaces", database.supportsTablespaces());
        }


        this.expressionExpander = new ExpressionExpander(this);
        this.currentDatabase = database;
        this.currentContexts = new Contexts();
        this.currentLabelExpression = new LabelExpression();
    }

    public void setContexts(Contexts contexts) {
        this.currentContexts = contexts;
    }

    public Contexts getContexts() {
        return currentContexts;
    }

    public void set(String paramter, Object value) {
        changeLogParameters.add(new ChangeLogParameter(paramter, value));
    }

    public void set(String key, String value, String contexts, String labels, String databases) {
        set(key, value, new ContextExpression(contexts), new Labels(labels), databases);
    }
    public void set(String key, String value, ContextExpression contexts, Labels labels, String databases) {
        changeLogParameters.add(new ChangeLogParameter(key, value, contexts, labels, databases));
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

    public void setLabels(LabelExpression labels) {
        this.currentLabelExpression = labels;
    }

    public LabelExpression getLabels() {
        return currentLabelExpression;
    }

    private class ChangeLogParameter {
        private String key;
        private Object value;
        private ContextExpression validContexts;
        private Labels labels;
        private List<String> validDatabases;

        public ChangeLogParameter(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public ChangeLogParameter(String key, Object value, String validContexts, String labels, String validDatabases) {
            this(key, value, new ContextExpression(validContexts), new Labels(labels), StringUtils.splitAndTrim(validDatabases, ","));
        }

        private ChangeLogParameter(String key, Object value, ContextExpression validContexts, Labels labels, String validDatabases) {
            this(key, value, validContexts, labels, StringUtils.splitAndTrim(validDatabases, ","));
        }

        public ChangeLogParameter(String key, Object value, ContextExpression validContexts, Labels labels, List<String> validDatabases) {
            this.key = key;
            this.value = value;
            this.validContexts = validContexts;
            this.labels = labels;
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

        public ContextExpression getValidContexts() {
            return validContexts;
        }

        public Labels getLabels() {
            return labels;
        }

        @Override
        public String toString() {
            return getValue().toString();
        }

        public boolean isValid() {
            boolean isValid = validContexts == null || validContexts.matches(ChangeLogParameters.this.currentContexts);

            if (isValid) {
                isValid = labels == null || currentLabelExpression == null || currentLabelExpression.matches(labels);
            }

            if (isValid) {
                isValid = DatabaseList.definitionMatches(validDatabases, currentDatabase, true);
            }

            return isValid;
        }
    }

    protected static class ExpressionExpander {
    	private boolean enableEscaping;
        private ChangeLogParameters changeLogParameters;
        private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(\\$\\{[^\\}]+\\})");

        public ExpressionExpander(ChangeLogParameters changeLogParameters) {
            this.changeLogParameters = changeLogParameters;
            this.enableEscaping = LiquibaseConfiguration.getInstance().getConfiguration(ChangeLogParserCofiguration.class).getSupportPropertyEscaping();
        }

        public String expandExpressions(String text) {
            if (text == null) {
                return null;
            }
            Matcher matcher = EXPRESSION_PATTERN.matcher(text);
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
