package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.exception.DatabaseException;
import liquibase.parser.ChangeLogParserCofiguration;
import liquibase.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeLogParameters {

    private List<ChangeLogParameter> changeLogParameters = new ArrayList<>();
    private ExpressionExpander expressionExpander;
    private Database currentDatabase;
    private Contexts currentContexts;
    private LabelExpression currentLabelExpression;

    public ChangeLogParameters() {
        this(null);
    }

    public ChangeLogParameters(Database database) {
        for (Map.Entry entry : ((Properties) System.getProperties().clone()).entrySet()) {
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
            this.set("database.defaultSchemaNamePrefix", (StringUtils.trimToNull(database.getDefaultSchemaName()) ==
                null) ? "" : ("." + database.getDefaultSchemaName()));
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
            this.set("database.supportsNotNullConstraintNames", database.supportsNotNullConstraintNames());
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

    public List<ChangeLogParameter> getChangeLogParameters() {
        return Collections.unmodifiableList(changeLogParameters);
    }

    public void set(String paramter, Object value) {
        /*
         * TODO: this was a bug. Muliple created parameters have been created, but the corresponding method in
         * #findParameter() is only catching the first one. So here we should eliminate duplicate entries
         */
        ChangeLogParameter param = findParameter(paramter, null);
        if (param == null) {
            // okay add it
            changeLogParameters.add(new ChangeLogParameter(paramter, value));
        }
    }

    public void set(String key, String value, String contexts, String labels, String databases, boolean globalParam,
                    DatabaseChangeLog changeLog) {
        set(key, value, new ContextExpression(contexts), new Labels(labels), databases, globalParam, changeLog);
    }

    public void set(String key, String value, ContextExpression contexts, Labels labels, String databases,
                    boolean globalParam, DatabaseChangeLog changeLog) {
        /**
         * TODO: this was a bug. Muliple created parameters have been created, but the corresponding method in
         * #findParameter() is only catching the first one. So here we should eliminate duplicate entries
         **/
        if (globalParam) {
            // if it is global param ignore additional adds
            ChangeLogParameter param = findParameter(key, null);
            if (param == null) {
                // okay add it
                changeLogParameters.add(new ChangeLogParameter(key, value, contexts, labels, databases, globalParam,
                    changeLog));
            }
        } else {
            //this is a non-global param, just add it
            changeLogParameters.add(new ChangeLogParameter(key, value, contexts, labels, databases, globalParam,
                changeLog));
        }
    }

    /**
     * Return the value of a parameter
     *
     * @param key Name of the parameter
     * @return The parameter value or null if not found. (Note that null can also be return if it is the parameter
     * value. For strict parameter existence use {@link #hasValue(String, DatabaseChangeLog)}
     */
    public Object getValue(String key, DatabaseChangeLog changeLog) {
        ChangeLogParameter parameter = findParameter(key, changeLog);
        return (parameter != null) ? parameter.getValue() : null;
    }

    private ChangeLogParameter findParameter(String key, DatabaseChangeLog changeLog) {
        ChangeLogParameter result = null;

        List<ChangeLogParameter> found = new ArrayList<>();
        for (ChangeLogParameter param : changeLogParameters) {
            if (param.getKey().equalsIgnoreCase(key) && param.isValid()) {
                found.add(param);
            }
        }
        
        if (found.size() == 1) {
            // this case is typically a global param, but could also be a unique non-global param in one specific
            // changelog
            result = found.get(0);
        } else if (found.size() > 1) {
            for (ChangeLogParameter changeLogParameter : found) {
                if (changeLogParameter.getChangeLog() == changeLog) {
                    result = changeLogParameter;
                }
            }
        }
        
        return result;
    }

    public boolean hasValue(String key, DatabaseChangeLog changeLog) {
        return findParameter(key, changeLog) != null;
    }

    public String expandExpressions(String string, DatabaseChangeLog changeLog) {
        return expressionExpander.expandExpressions(string, changeLog);
    }

    public void setLabels(LabelExpression labels) {
        this.currentLabelExpression = labels;
    }

    public LabelExpression getLabels() {
        return currentLabelExpression;
    }

    protected static class ExpressionExpander {
        private boolean enableEscaping;
        private ChangeLogParameters changeLogParameters;
        private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(\\$\\{[^\\}]+\\})");

        public ExpressionExpander(ChangeLogParameters changeLogParameters) {
            this.changeLogParameters = changeLogParameters;
            this.enableEscaping = LiquibaseConfiguration.getInstance()
                .getConfiguration(ChangeLogParserCofiguration.class).getSupportPropertyEscaping();
        }

        public String expandExpressions(String text, DatabaseChangeLog changeLog) {
            if (text == null) {
                return null;
            }
            Matcher matcher = EXPRESSION_PATTERN.matcher(text);
            String originalText = text;
            while (matcher.find()) {
                String expressionString = originalText.substring(matcher.start(), matcher.end());
                String valueTolookup = expressionString.replaceFirst("\\$\\{", "").replaceFirst("\\}$", "");

                Object value = (enableEscaping && valueTolookup.startsWith(":")) ? null : changeLogParameters
                    .getValue(valueTolookup, changeLog);

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

    public class ChangeLogParameter {
        private String key;
        private Object value;
        private ContextExpression validContexts;
        private Labels labels;
        private List<String> validDatabases;
        /** is this parameter a global parameter, means globally over all changesets. */
        private boolean global = true;
        private DatabaseChangeLog changeLog;

        public ChangeLogParameter(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public ChangeLogParameter(String key, Object value, String validContexts, String labels, String validDatabases,
                                  boolean globalParam, DatabaseChangeLog changeLog) {
            this(key, value, new ContextExpression(validContexts), new Labels(labels),
                StringUtils.splitAndTrim(validDatabases, ","), globalParam, changeLog);
        }

        private ChangeLogParameter(String key, Object value, ContextExpression validContexts, Labels labels,
                                   String validDatabases, boolean globalParam, DatabaseChangeLog changeLog) {
            this(key, value, validContexts, labels, StringUtils.splitAndTrim(validDatabases, ","),
                globalParam, changeLog);
        }

        public ChangeLogParameter(String key, Object value, ContextExpression validContexts, Labels labels,
                                  List<String> validDatabases, boolean globalParam, DatabaseChangeLog changeLog) {
            this.key = key;
            this.value = value;
            this.validContexts = validContexts;
            this.labels = labels;
            this.validDatabases = validDatabases;
            this.global = globalParam;
            this.changeLog = changeLog;
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
            boolean isValid = (validContexts == null)
                || validContexts.matches(ChangeLogParameters.this.currentContexts);

            if (isValid) {
                isValid = (labels == null) || (currentLabelExpression == null)
                    || currentLabelExpression.matches(labels);
            }

            if (isValid) {
                isValid = DatabaseList.definitionMatches(validDatabases, currentDatabase, true);
            }

            return isValid;
        }

        public boolean isGlobal() {
            return global;
        }

        public DatabaseChangeLog getChangeLog() {
            return changeLog;
        }
    }
}
