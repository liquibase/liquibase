package liquibase.command.core.helpers;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.core.DiffCommandStep;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.structure.DatabaseObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Internal command step to be used on pipeline to manage the database connection  to the reference database.
 */
public class ReferenceDbUrlConnectionCommandStep extends AbstractDatabaseConnectionCommandStep implements CleanUpCommandStep {

    public static final String[] COMMAND_NAME = {"referenceDbUrlConnectionCommandStep"};

    public static final CommandArgumentDefinition<Database> REFERENCE_DATABASE_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_USERNAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_URL_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DRIVER_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DRIVER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_LIQUIBASE_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_LIQUIBASE_CATALOG_NAME_ARG;


    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        REFERENCE_DEFAULT_SCHEMA_NAME_ARG = builder.argument("referenceDefaultSchemaName", String.class)
                .description("The default schema name to use for the reference database connection").build();
        REFERENCE_DEFAULT_CATALOG_NAME_ARG = builder.argument("referenceDefaultCatalogName", String.class)
                .description("The default catalog name to use for the reference database connection").build();
        REFERENCE_DRIVER_ARG = builder.argument("referenceDriver", String.class)
                .description("The JDBC driver class for the reference database").build();
        REFERENCE_DRIVER_PROPERTIES_FILE_ARG = builder.argument("referenceDriverPropertiesFile", String.class)
                .description("The JDBC driver properties file for the reference database").build();
        REFERENCE_USERNAME_ARG = builder.argument("referenceUsername", String.class)
                .description("The reference database username").build();
        REFERENCE_PASSWORD_ARG = builder.argument("referencePassword", String.class)
                .description("The reference database password")
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();
        REFERENCE_DATABASE_ARG = builder.argument("referenceDatabase", Database.class).hidden().build();
        REFERENCE_URL_ARG = builder.argument("referenceUrl", String.class).required().supersededBy(REFERENCE_DATABASE_ARG)
                .description("The JDBC reference database connection URL").build();
        REFERENCE_DATABASE_ARG.setSupersededBy(REFERENCE_URL_ARG);

        REFERENCE_LIQUIBASE_SCHEMA_NAME_ARG = builder.argument("referenceLiquibaseSchemaName", String.class)
                .description("Reference schema to use for Liquibase objects").build();
        REFERENCE_LIQUIBASE_CATALOG_NAME_ARG = builder.argument("referenceLiquibaseCatalogName", String.class)
                .description("Reference catalog to use for Liquibase objects").build();
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(ReferenceDatabase.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        commandScope.provideDependency(ReferenceDatabase.class, this.obtainDatabase(commandScope));
    }

    /**
     * Try to retrieve and set the database object from the command scope, otherwise creates a new one.
     * If creating a new database, also retrieves objectChangeFilter and snapshotTypes from command arguments
     * and passes them through Scope for offline snapshot filtering.
     *
     * @param commandScope current command scope
     * @throws DatabaseException Thrown when there is a connection error
     */
    private Database obtainDatabase(CommandScope commandScope) throws DatabaseException {
        Database existingDatabase = commandScope.getArgumentValue(REFERENCE_DATABASE_ARG);

        if (existingDatabase == null) {
            String url = commandScope.getArgumentValue(REFERENCE_URL_ARG);
            String username = commandScope.getArgumentValue(REFERENCE_USERNAME_ARG);
            String password = commandScope.getArgumentValue(REFERENCE_PASSWORD_ARG);
            String defaultSchemaName = commandScope.getArgumentValue(REFERENCE_DEFAULT_SCHEMA_NAME_ARG);
            String defaultCatalogName = commandScope.getArgumentValue(REFERENCE_DEFAULT_CATALOG_NAME_ARG);
            String driver = commandScope.getArgumentValue(REFERENCE_DRIVER_ARG);
            String driverPropertiesFile = commandScope.getArgumentValue(REFERENCE_DRIVER_PROPERTIES_FILE_ARG);
            logMdc(url, username, defaultSchemaName, defaultCatalogName);
            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Database.IGNORE_MISSING_REFERENCES_KEY, commandScope.getArgumentValue(DiffArgumentsCommandStep.IGNORE_MISSING_REFERENCES));

            ObjectChangeFilter objectChangeFilter = getObjectChangeFilter(commandScope);
            if (objectChangeFilter != null) {
                scopeValues.put("objectChangeFilter", objectChangeFilter);
            }

            Class<? extends DatabaseObject>[] snapshotTypes = getSnapshotTypes(commandScope);
            if (snapshotTypes != null) {
                scopeValues.put("snapshotTypes", snapshotTypes);
            }

            AtomicReference<Database> database = new AtomicReference<>();
            try {
                Scope.child(scopeValues, () -> {
                    database.set(createDatabaseObject(url, username, password, defaultSchemaName, defaultCatalogName, driver, driverPropertiesFile,
                            StringUtils.trimToNull(commandScope.getArgumentValue(REFERENCE_LIQUIBASE_CATALOG_NAME_ARG)),
                            StringUtils.trimToNull(commandScope.getArgumentValue(REFERENCE_LIQUIBASE_SCHEMA_NAME_ARG))));
                });
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
            logLicenseUsage(url, database.get(), false, true);
            return database.get();
        } else {
            return existingDatabase;
        }
    }

    public static void logMdc(String url, String username, String defaultSchemaName, String defaultCatalogName) {
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_REF_URL, JdbcConnection.sanitizeUrl(url));
        Scope.getCurrentScope().addMdcValue(MdcKey.REFERENCE_USERNAME, username);
        Scope.getCurrentScope().addMdcValue(MdcKey.REFERENCE_DEFAULT_SCHEMA_NAME, defaultSchemaName);
        Scope.getCurrentScope().addMdcValue(MdcKey.REFERENCE_DEFAULT_CATALOG_NAME, defaultCatalogName);
    }

    /**
     * Creates ObjectChangeFilter from command arguments (excludeObjects or includeObjects).
     *
     * @param commandScope current command scope
     * @return ObjectChangeFilter if exclude/include arguments are present, null otherwise
     */
    private ObjectChangeFilter getObjectChangeFilter(CommandScope commandScope) {
        if (commandScope.getArgumentValue(PreCompareCommandStep.OBJECT_CHANGE_FILTER_ARG) != null) {
            return commandScope.getArgumentValue(PreCompareCommandStep.OBJECT_CHANGE_FILTER_ARG);
        }
        String excludeObjects = commandScope.getArgumentValue(PreCompareCommandStep.EXCLUDE_OBJECTS_ARG);
        String includeObjects = commandScope.getArgumentValue(PreCompareCommandStep.INCLUDE_OBJECTS_ARG);

        if ((excludeObjects != null) && (includeObjects != null)) {
            throw new UnexpectedLiquibaseException(
                    String.format("Cannot specify both '%s' and '%s'",
                            PreCompareCommandStep.EXCLUDE_OBJECTS_ARG.getName(),
                            PreCompareCommandStep.INCLUDE_OBJECTS_ARG.getName()));
        }

        ObjectChangeFilter objectChangeFilter = null;
        if (excludeObjects != null) {
            objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE,
                    excludeObjects);
        }
        if (includeObjects != null) {
            objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE,
                    includeObjects);
        }

        return objectChangeFilter;
    }

    /**
     * Gets snapshot types from command arguments (either from snapshotTypes arg or parsed from diffTypes).
     *
     * @param commandScope current command scope
     * @return Array of DatabaseObject classes to include in snapshot, or empty array if not specified
     */
    private Class<? extends DatabaseObject>[] getSnapshotTypes(CommandScope commandScope) {
        if (commandScope.getArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG) != null) {
            return commandScope.getArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG);
        }
        String diffTypes = commandScope.getArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG);
        return DiffCommandStep.parseSnapshotTypes(diffTypes);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

}
