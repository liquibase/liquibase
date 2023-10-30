package liquibase.command.core.helpers;

import liquibase.Beta;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.util.StringUtil;

import java.util.Collections;
import java.util.List;

/**
 * Internal command step to be used on CommandStep pipeline to manage the database connection.
 */
public class DbUrlConnectionCommandStep extends AbstractDatabaseConnectionCommandStep implements CleanUpCommandStep {

    protected static final String[] COMMAND_NAME = {"dbUrlConnectionCommandStep"};

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;

    /**
     * This Argument skips this step. It may not be a definitive solution, as improvements to pipeline may
     * change the way that we remove/skip steps.
     */
    @Beta
    @Deprecated
    public static final CommandArgumentDefinition<Boolean> SKIP_DATABASE_STEP_ARG;


    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DEFAULT_SCHEMA_NAME_ARG = builder.argument("defaultSchemaName", String.class)
                .description("The default schema name to use for the database connection").build();
        DEFAULT_CATALOG_NAME_ARG = builder.argument("defaultCatalogName", String.class)
                .description("The default catalog name to use for the database connection").build();
        DRIVER_ARG = builder.argument("driver", String.class)
                .description("The JDBC driver class").build();
        DRIVER_PROPERTIES_FILE_ARG = builder.argument("driverPropertiesFile", String.class)
                .description("The JDBC driver properties file").build();
        USERNAME_ARG = builder.argument(CommonArgumentNames.USERNAME, String.class)
                .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument(CommonArgumentNames.PASSWORD, String.class)
                .description("Password to use to connect to the database")
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();
        DATABASE_ARG = builder.argument("database", Database.class).hidden().build();
        URL_ARG = builder.argument(CommonArgumentNames.URL, String.class).required().supersededBy(DATABASE_ARG)
                .description("The JDBC database connection URL").build();
        DATABASE_ARG.setSupersededBy(URL_ARG);

        SKIP_DATABASE_STEP_ARG = builder.argument("skipDatabaseStep", Boolean.class).hidden().defaultValue(false).build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        if (commandScope.getArgumentValue(SKIP_DATABASE_STEP_ARG)) {
            return;
        }
        commandScope.provideDependency(Database.class, this.obtainDatabase(commandScope));
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
            String defaultSchemaName = commandScope.getArgumentValue(DEFAULT_SCHEMA_NAME_ARG);
            String defaultCatalogName = commandScope.getArgumentValue(DEFAULT_CATALOG_NAME_ARG);
            String driver = commandScope.getArgumentValue(DRIVER_ARG);
            String driverPropertiesFile = commandScope.getArgumentValue(DRIVER_PROPERTIES_FILE_ARG);
            Database database = createDatabaseObject(url, username, password, defaultSchemaName, defaultCatalogName, driver, driverPropertiesFile,
                    StringUtil.trimToNull(GlobalConfiguration.LIQUIBASE_CATALOG_NAME.getCurrentValue()),
                    StringUtil.trimToNull(GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentValue()));
            logMdc(url, database);
            return database;
        } else {
            return commandScope.getArgumentValue(DATABASE_ARG);
        }
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
