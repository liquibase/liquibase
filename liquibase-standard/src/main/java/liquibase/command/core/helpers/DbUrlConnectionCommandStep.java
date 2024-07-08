package liquibase.command.core.helpers;

import liquibase.Beta;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CleanUpCommandStep;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.configuration.ConfiguredValue;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.logging.mdc.MdcKey;
import liquibase.util.StringUtil;

import java.util.Collections;
import java.util.List;

/**
 * Internal command step to be used on CommandStep pipeline to manage the database connection.
 */
public class DbUrlConnectionCommandStep extends AbstractDatabaseConnectionCommandStep implements CleanUpCommandStep {

    protected static final String[] COMMAND_NAME = {"dbUrlConnectionCommandStep"};

    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<Database> DATABASE_ARG = DbUrlConnectionArgumentsCommandStep.DATABASE_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> URL_ARG = DbUrlConnectionArgumentsCommandStep.URL_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG = DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG = DbUrlConnectionArgumentsCommandStep.DEFAULT_CATALOG_NAME_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> USERNAME_ARG = DbUrlConnectionArgumentsCommandStep.USERNAME_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> PASSWORD_ARG = DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> DRIVER_ARG = DbUrlConnectionArgumentsCommandStep.DRIVER_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG = DbUrlConnectionArgumentsCommandStep.DRIVER_PROPERTIES_FILE_ARG;

    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Beta
    @Deprecated
    public static final CommandArgumentDefinition<Boolean> SKIP_DATABASE_STEP_ARG = DbUrlConnectionArgumentsCommandStep.SKIP_DATABASE_STEP_ARG;

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        if (commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.SKIP_DATABASE_STEP_ARG)) {
            return;
        }
        commandScope.provideDependency(Database.class, this.obtainDatabase(commandScope));
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(DbUrlConnectionArgumentsCommandStep.class);
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(Database.class);
    }

    /**
     * Try to retrieve and set the database object from the command scope, otherwise creates a new one .
     *
     * @param commandScope current command scope
     * @throws DatabaseException Thrown when there is a connection error
     */
    public Database obtainDatabase(CommandScope commandScope) throws DatabaseException {
        if (commandScope.getArgumentValue(DATABASE_ARG) == null) {
            String url = commandScope.getArgumentValue(URL_ARG);
            String username = commandScope.getArgumentValue(USERNAME_ARG);
            String password = commandScope.getArgumentValue(PASSWORD_ARG);
            String defaultSchemaName = commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG);
            String defaultCatalogName = commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_CATALOG_NAME_ARG);
            String driver = getDriver(commandScope);
            String driverPropertiesFile = commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_PROPERTIES_FILE_ARG);
            Database database = createDatabaseObject(url, username, password, defaultSchemaName, defaultCatalogName, driver, driverPropertiesFile,
                    StringUtil.trimToNull(GlobalConfiguration.LIQUIBASE_CATALOG_NAME.getCurrentValue()),
                    StringUtil.trimToNull(GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentValue()));
            logMdc(url, database);
            return database;
        } else {
            return commandScope.getArgumentValue(DATABASE_ARG);
        }
    }

    private static String getDriver(CommandScope commandScope) {
        ConfiguredValue<String> globalConfig = LiquibaseCommandLineConfiguration.DRIVER.getCurrentConfiguredValue();
        ConfiguredValue<String> commandConfig = commandScope.getConfiguredValue(DbUrlConnectionArgumentsCommandStep.DRIVER_ARG);
        if (globalConfig.found() && commandConfig.found()) {
            Scope.getCurrentScope().getLog(DbUrlConnectionCommandStep.class).warning("Ignoring the global " + LiquibaseCommandLineConfiguration.DRIVER.getKey() + " value in favor of the command value.");
        }
        if (commandConfig.found()) {
            return commandConfig.getValue();
        }
        if (globalConfig.found()) {
            return globalConfig.getValue();
        }
        return null;
    }

    public static void logMdc(String url, Database database) {
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, JdbcConnection.sanitizeUrl(url));
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_CATALOG_NAME, database.getLiquibaseCatalogName());
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_SCHEMA_NAME, database.getLiquibaseSchemaName());
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

}
