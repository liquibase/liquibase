package liquibase.changelog;

import liquibase.*;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnknownChangeLogParameterException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.util.StringUtil;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds the parameters configured for a {@link DatabaseChangeLog}.
 * <p>
 * In general, the end behavior of defined parameters is "the first set value wins".
 * For example, if you set a parameter "x" to "1" and then set it to "2", the value will remain "1".
 * This immutable property behavior allows users to easily set default values, knowing that any "upstream" overrides will take priority.
 * <p>
 * In determining which property value is actually "first set", context, label, and dbms filtering is taken into account.
 * <p>
 * Properties can be defined as "global" or "local". Global properties span all change logs.
 * A global setting configured in an included changelog is still available to all changesets
 * Local properties are  only available in the change log that they are defined in -- not even in changelogs "included" by the file that defines the property.
 */
public class ChangeLogParameters {

    private final List<ChangeLogParameter> globalParameters = new ArrayList<>();
    private final Map<String, List<ChangeLogParameter>> localParameters = new HashMap<>();

    private final ExpressionExpander expressionExpander;
    private String filterDatabase;
    private Contexts filterContexts;
    private LabelExpression filterLabels;

    private enum LiquibaseExecutionParameter {
        LIQUIBASE_EXECUTION_CHANGELOG_FILE {
            @Override
            public String getValue(DatabaseChangeLog changeLog) {
                return changeLog.getFilePath();
            }
        },
        LIQUIBASE_EXECUTION_CHANGESET_ID {
            @Override
            public String getValue(DatabaseChangeLog changeLog) {
                ParsedNode changeSetParsedNode = changeLog.getCurrentlyLoadedChangeSetNode();
                String changesetId;

                try {
                    changesetId = changeSetParsedNode.getChildValue(null, "id", String.class);
                    return changesetId;
                }
                catch (ParsedNodeException e) {
                    return null;
                }
            }
        },
        LIQUIBASE_EXECUTION_CHANGESET_AUTHOR {
            @Override
            public String getValue(DatabaseChangeLog changeLog) {
                ParsedNode changeSetParsedNode = changeLog.getCurrentlyLoadedChangeSetNode();
                String changesetAuthor;

                try {
                    changesetAuthor = changeSetParsedNode.getChildValue(null, "author", String.class);
                    return changesetAuthor;
                }
                catch (ParsedNodeException e) {
                    return null;
                }
            }
        };

        public abstract String getValue(DatabaseChangeLog changeLog);

        /**
         * @param name the name of the {@link LiquibaseExecutionParameter} to find
         * @return The {@link LiquibaseExecutionParameter} if found, else null
         */
        public static LiquibaseExecutionParameter findByName(String name) {
            LiquibaseExecutionParameter result = null;

            for (LiquibaseExecutionParameter param: LiquibaseExecutionParameter.values()) {
                if (param.name().equalsIgnoreCase(name)) {
                    result = param;
                    break;
                }
            }

            return result;
        }
    }

    /**
     * Calls {@link #ChangeLogParameters(Database)} with a null database.
     */
    public ChangeLogParameters() {
        this(null);
    }

    /**
     * Creates a new ChangeLogParameters instance, populated with a set of "database.*" global parameters based on the passed database configuration.
     * If the database is null, no global parameters are added.
     * <p>
     * The passed database is used as a default value for {@link #getDatabase()}
     */
    public ChangeLogParameters(Database database) {
        globalParameters.addAll(System.getenv().entrySet().stream().map(e -> new ChangeLogParameter(e.getKey(), e.getValue())).collect(Collectors.toList()));
        globalParameters.addAll(System.getProperties().entrySet().stream().map(e -> new ChangeLogParameter(String.valueOf(e.getKey()), e.getValue())).collect(Collectors.toList()));

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
            this.set("database.supportsSchemas", database.supports(Schema.class));
            this.set("database.supportsSequences", database.supports(Sequence.class));
            this.set("database.supportsTablespaces", database.supportsTablespaces());
            this.set("database.supportsNotNullConstraintNames", database.supportsNotNullConstraintNames());

            this.filterDatabase = database.getShortName();
        }

        this.expressionExpander = new ExpressionExpander(this);
        this.filterContexts = new Contexts();
        this.filterLabels = new LabelExpression();
    }

    /**
     * Sets a global changelog parameter with no context/label/database filters on it.
     * Convenience version of {@link #set(String, Object, ContextExpression, Labels, String...)}.
     */
    public void set(String parameter, Object value) {
        this.set(parameter, value, new ContextExpression(), new Labels());
    }

    /**
     * Sets a local changelog parameter with no context/label/database filters on it.
     * Convenience version of {@link #setLocal(String, Object, DatabaseChangeLog, ContextExpression, Labels, String...)}.
     */
    public void setLocal(String parameter, Object value, DatabaseChangeLog changeLog) {
        this.setLocal(parameter, value, changeLog, new ContextExpression(), new Labels());
    }

    /**
     * Calls either {@link #set(String, Object, ContextExpression, Labels, String...)} or {@link #setLocal(String, Object, DatabaseChangeLog, ContextExpression, Labels, String...)} depending on the value of globalParam.
     */
    public void set(String key, Object value, ContextExpression contexts, Labels labels, String databases, boolean globalParam, DatabaseChangeLog changeLog) {
        String[] parsedDatabases = null;
        if (databases != null && databases.length() > 0) {
            parsedDatabases = StringUtil.splitAndTrim(databases, ",").toArray(new String[0]);
        }

        if (globalParam) {
            set(key, value, contexts, labels, parsedDatabases);
        } else {
            setLocal(key, value, changeLog, contexts, labels, parsedDatabases);
        }
    }

    /**
     * Convenience version of {@link #set(String, Object, ContextExpression, Labels, String, boolean, DatabaseChangeLog)}.
     */
    public void set(String key, Object value, String contexts, String labels, String databases, boolean globalParam, DatabaseChangeLog changeLog) {
        set(key, value, new ContextExpression(contexts), new Labels(labels), databases, globalParam, changeLog);
    }

    /**
     * Sets a global changelog parameter.
     * Just because you call this with a particular key, does not mean it will override the existing value. See the class description for more details on how values act as if they are immutable.
     */
    public void set(String key, Object value, ContextExpression contexts, Labels labels, String... databases) {
        globalParameters.add(new ChangeLogParameter(key, value, contexts, labels, databases));
    }

    /**
     * Sets a changelog parameter local to the given changeLog file.
     * Just because you call this with a particular key, does not mean it will override the existing value. See the class description for more details on how values act as if they are immutable.
     *
     * @param changeLog required for local parameters, ignored for global parameters
     **/
    public void setLocal(String key, Object value, DatabaseChangeLog changeLog, ContextExpression contexts, Labels labels, String... databases) {
        if (changeLog == null) {
            throw new IllegalArgumentException("changeLog cannot be null when setting a local parameter");
        }

        final String changelogKey = getLocalKey(changeLog);

        List<ChangeLogParameter> localParams = localParameters.get(changelogKey);
        if (localParams == null) {
            localParams = new ArrayList<>();
            this.localParameters.put(changelogKey, localParams);
        }
        localParams.add(new ChangeLogParameter(key, value, contexts, labels, databases));
    }

    /**
     * Get the value of the given parameter, taking into account parameters local to the given changelog file and
     * values configured in {@link #getContexts()} and {@link #getLabels()} and the database.
     */
    public Object getValue(String key, DatabaseChangeLog changeLog) {
        final ChangeLogParameter param = getChangelogParameter(key, changeLog, getFilter());
        if (param == null) {
            return null;
        }
        return param.getValue();
    }

    /**
     * Return whether the given parameters is defined, taking into account parameters local to the given changelog file
     * as well as contexts, labels, and database configured on this instance
     */
    public boolean hasValue(String key, DatabaseChangeLog changeLog) {
        return getChangelogParameter(key, changeLog, getFilter()) != null;
    }

    /**
     * Expand any expressions in the given string, taking into account parameters local to the given changelog file as well as
     * contexts, labels, and database configured in this instance.
     */
    public String expandExpressions(String string, DatabaseChangeLog changeLog) throws UnknownChangeLogParameterException {
        return expressionExpander.expandExpressions(string, changeLog);
    }

    /**
     * Gets the contexts to filter calls to {@link #getValue(String, DatabaseChangeLog)} etc. with.
     */
    public Contexts getContexts() {
        return filterContexts;
    }

    /**
     * Sets the contexts to filter calls to {@link #getValue(String, DatabaseChangeLog)} etc. with.
     */
    public void setContexts(Contexts contexts) {
        this.filterContexts = contexts;
    }


    /**
     * Gets the labels to filter calls to {@link #getValue(String, DatabaseChangeLog)} etc. with.
     */
    public LabelExpression getLabels() {
        return filterLabels;
    }

    /**
     * Sets the labels to filter calls to {@link #getValue(String, DatabaseChangeLog)} etc. with.
     */
    public void setLabels(LabelExpression labels) {
        this.filterLabels = labels;
    }

    /**
     * Sets the database to filter calls to {@link #getValue(String, DatabaseChangeLog)} etc. with.
     */
    public String getDatabase() {
        return filterDatabase;
    }

    /**
     * Sets the database to filter calls to {@link #getValue(String, DatabaseChangeLog)} etc. with.
     */
    public void setDatabase(String filterDatabase) {
        this.filterDatabase = filterDatabase;
    }

    private Filter getFilter() {
        return new Filter(this.filterDatabase, this.filterContexts, this.filterLabels);
    }

    private ChangeLogParameter getChangelogParameter(String key, DatabaseChangeLog changeLog, Filter filter) {
        List<ChangeLogParameter> localList = null;
        if (changeLog != null) {
            LiquibaseExecutionParameter executionParameter = LiquibaseExecutionParameter.findByName(key);
            if (executionParameter != null) {
                return new ChangeLogParameter(executionParameter.name(), executionParameter.getValue(changeLog));
            }

            localList = localParameters.get(getLocalKey(changeLog));
            if (localList != null) {
                localList = new ArrayList<>(localList); // make a copy as we don't want to reverse the original list
                Collections.reverse(localList);
            }
        }

        for (List<ChangeLogParameter> paramList : Arrays.asList(globalParameters, localList)) {
            if (paramList == null) {
                continue;
            }

            for (ChangeLogParameter parameter : paramList) {
                if (parameter.getKey().equalsIgnoreCase(key) && (filter == null || filter.matches(parameter))) {
                    return parameter;
                }
            }

        }

        return null;
    }

    /**
     * The key to use in {@link #localParameters}
     */
    private String getLocalKey(DatabaseChangeLog changeLog) {
        if (changeLog == null) {
            return "null changelog path";
        }
        return changeLog.getLogicalFilePath();
    }

    private static class ChangeLogParameter {
        @Getter
        private final String key;
        @Getter
        private final Object value;

        @Getter
        private final ContextExpression validContexts;
        private final Labels validLabels;
        @Getter
        private final List<String> validDatabases;

        public ChangeLogParameter(String key, Object value) {
            this(key, value, null, null, null);
        }

        public ChangeLogParameter(String key, Object value, ContextExpression validContexts, Labels labels, String[] validDatabases) {
            this.key = key;
            this.value = value;
            this.validContexts = validContexts == null ? new ContextExpression() : validContexts;
            this.validLabels = labels == null ? new Labels() : labels;

            if (validDatabases == null) {
                this.validDatabases = null;
            } else {
                this.validDatabases = Arrays.asList(validDatabases);
            }
        }

        public Labels getLabels() {
            return validLabels;
        }

        @Override
        public String toString() {
            return getValue().toString();
        }
    }

    private static class Filter {
        private final Contexts contexts;
        private final LabelExpression labels;
        private final String database;

        public Filter(String database, Contexts contexts, LabelExpression labels) {
            this.contexts = contexts;
            this.labels = labels;
            this.database = database;
        }

        public boolean matches(ChangeLogParameter parameter) {
            return (labels == null || labels.matches(parameter.getLabels()))
                    && (contexts == null || parameter.getValidContexts().matches(contexts))
                    && (database == null || DatabaseList.definitionMatches(parameter.getValidDatabases(), database, true))
                    ;
        }
    }

    /**
     * Add java property arguments to changelog parameters
     */
    public void addJavaProperties() {
        HashMap javaProperties = Scope.getCurrentScope().get("javaProperties", HashMap.class);
        if (javaProperties != null) {
            javaProperties.forEach((key, value) -> this.set((String) key, value));
        }
    }

    /**
     * Add default-file properties to changelog parameters
     */
    public void addDefaultFileProperties() {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        for (ConfigurationValueProvider cvp : liquibaseConfiguration.getProviders()) {
            if (cvp instanceof DefaultsFileValueProvider) {
                DefaultsFileValueProvider dfvp = (DefaultsFileValueProvider) cvp;
                dfvp.getMap().entrySet().stream()
                        .filter(entry -> ((String) entry.getKey()).startsWith("parameter."))
                        .forEach(entry -> this.set(((String) entry.getKey()).replaceFirst("^parameter.", ""), entry.getValue()));
            }
        }
    }
}
