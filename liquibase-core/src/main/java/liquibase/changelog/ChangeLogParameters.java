package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.exception.DatabaseException;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.util.StringUtil;

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
    	LinkedHashMap<Object, Object> externalParameters = new LinkedHashMap<>();
    	// First add environment variables
    	externalParameters.putAll(System.getenv());
    	
    	// Next add system properties; they have higher precedence than / overwrite environment variables
    	externalParameters.putAll((Properties) System.getProperties().clone());
        
    	for (Map.Entry entry : externalParameters.entrySet()) {
            changeLogParameters.add(new ChangeLogParameter(entry.getKey().toString(), entry.getValue()));
        }

        if (database != null) {
            this.set("database.autoIncrementClause", database.getAutoIncrementClause(null, null, null, null));
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
            this.set("database.defaultSchemaNamePrefix", (StringUtil.trimToNull(database.getDefaultSchemaName()) ==
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

    public void set(String parameter, Object value) {
        /*
         * TODO: this was a bug. Multiple created parameters have been created, but the corresponding method in
         * #findParameter() is only catching the first one. So here we should eliminate duplicate entries
         */
        ChangeLogParameter param = findParameter(parameter, null);
        // okay add it
        changeLogParameters.add(new ChangeLogParameter(parameter, value));
        if (param != null && ! param.isGlobal()) {
            changeLogParameters.remove(param);
        }
    }

    public void set(String key, String value, String contexts, String labels, String databases, boolean globalParam,
                    DatabaseChangeLog changeLog) {
        set(key, value, new ContextExpression(contexts), new Labels(labels), databases, globalParam, changeLog);
    }

    public void set(String key, String value, ContextExpression contexts, Labels labels, String databases,
                    boolean globalParam, DatabaseChangeLog changeLog) {
        /**
         * TODO: this was a bug. Multiple created parameters have been created, but the corresponding method in
         * #findParameter() is only catching the first one. So here we should eliminate duplicate entries
         **/
        if (globalParam) {
            // if it is global param remove duplicate non-global parameters
            ChangeLogParameter param = findParameter(key, null);
            if (param != null && isDuplicate(param) && ! param.isGlobal()) {
                changeLogParameters.remove(param);
            }
            // okay add it
            changeLogParameters.add(new ChangeLogParameter(key, value, contexts, labels, databases, globalParam,
                changeLog));
        } else {
           ChangeLogParameter param = findParameter(key, changeLog);
           if (param != null && isDuplicate(param) && ! param.isGlobal() && param.getChangeLog() == changeLog) {
               changeLogParameters.remove(param);
           }
           //this is a non-global param, just add it
           changeLogParameters.add(new ChangeLogParameter(key, value, contexts, labels, databases, globalParam, changeLog));
        }
    }

    private boolean isDuplicate(ChangeLogParameter param) {
        for (ChangeLogParameter parameter : changeLogParameters) {
            if (param != parameter && param.isDuplicate(parameter)) {
                return true;
            }
        }
        return false;
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

        // if any parameters were found, determine which parameter value to use
        // (even if only one was found, we can not assume it should be used)
        if (found.size() > 0) {
            // look for the first global parameter
            for (ChangeLogParameter changeLogParameter : found) {
                if (changeLogParameter.isGlobal()) {
                    result = changeLogParameter;
                    break;
                }
            }

            // if none of the found parameters are global (all of them are local) and
            // the parameter is searched in the context of a changeSet (otherwise implicitly a global parameter is wanted)
            if (result == null && changeLog != null) {
                // look for the first parameter belonging to the current changeLog or the closest ancestor of the changeLog
                DatabaseChangeLog changeLogOrParent = changeLog;
                do {
                    for (ChangeLogParameter changeLogParameter : found) {
                        //
                        // If we are iterating through multiple found parameters for the key
                        // then we skip any with unexpanded parameter values.
                        // If all of the found parameters have unexpanded values
                        // then we will just return the first one in the current changelog or closest ancestor.
                        //
                        if (found.size() > 1 && isUnexpanded(changeLogParameter)) {
                            continue;
                        }
                        if (changeLogParameter.getChangeLog().equals(changeLogOrParent)) {
                            result = changeLogParameter;
                            break;
                        }
                    }
                } while (result == null && (changeLogOrParent = changeLogOrParent.getParentChangeLog()) != null);
            }
        }

        return result;
    }

    private boolean isUnexpanded(ChangeLogParameter changeLogParameter) {
        Object value = changeLogParameter.getValue();
        if (value instanceof String) {
            String string = (String) value;
            Matcher matcher = ExpressionExpander.EXPRESSION_PATTERN.matcher(string);
            return matcher.find();
        }
        return false;
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
            this.enableEscaping = ChangeLogParserConfiguration.SUPPORT_PROPERTY_ESCAPING.getCurrentValue();
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
                StringUtil.splitAndTrim(validDatabases, ","), globalParam, changeLog);
        }

        private ChangeLogParameter(String key, Object value, ContextExpression validContexts, Labels labels,
                                   String validDatabases, boolean globalParam, DatabaseChangeLog changeLog) {
            this(key, value, validContexts, labels, StringUtil.splitAndTrim(validDatabases, ","),
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

        public boolean isDuplicate(ChangeLogParameter other) {
            String contextString = (this.getValidContexts() != null ? this.getValidContexts().toString() : null);
            String labelsString = (this.getLabels() != null ? this.getLabels().toString() : null);
            String databases = (this.getValidDatabases() != null ? StringUtil.join(this.getValidDatabases(), ",") : null);

            String otherContextString = (other.getValidContexts() != null ? other.getValidContexts().toString() : null);
            String otherLabelsString = (other.getLabels() != null ? other.getLabels().toString() : null);
            String otherDatabases = (other.getValidDatabases() != null ? StringUtil.join(other.getValidDatabases(), ",") : null);
            return StringUtil.equalsIgnoreCaseAndEmpty(contextString, otherContextString) &&
                   StringUtil.equalsIgnoreCaseAndEmpty(labelsString, otherLabelsString) &&
                   StringUtil.equalsIgnoreCaseAndEmpty(databases, otherDatabases);
        }

        public boolean isGlobal() {
            return global;
        }

        public DatabaseChangeLog getChangeLog() {
            return changeLog;
        }
    }
}
