package liquibase.command.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.DatabaseUtils;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Internal command step to be used on CommandStep pipeline to manage the database connection.
 */
public class DbUrlConnectionCommandStep extends AbstractCommandStep implements CleanUpCommandStep {

    protected static final String[] COMMAND_NAME = {"dbUrlConnectionCommandStep"};
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;

    public static final int ORDER = 100;

    private Database database;

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
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
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
    private Database obtainDatabase(CommandScope commandScope) throws DatabaseException {
        if (commandScope.getArgumentValue(DATABASE_ARG) == null) {
            String url = commandScope.getArgumentValue(URL_ARG);
            String username = commandScope.getArgumentValue(USERNAME_ARG);
            String password = commandScope.getArgumentValue(PASSWORD_ARG);
            String defaultSchemaName = commandScope.getArgumentValue(DEFAULT_SCHEMA_NAME_ARG);
            String defaultCatalogName = commandScope.getArgumentValue(DEFAULT_CATALOG_NAME_ARG);
            String driver = commandScope.getArgumentValue(DRIVER_ARG);
            String driverPropertiesFile = commandScope.getArgumentValue(DRIVER_PROPERTIES_FILE_ARG);
            this.database = createDatabaseObject(url, username, password, defaultSchemaName, defaultCatalogName, driver, driverPropertiesFile);
            return this.database;
        } else {
            return commandScope.getArgumentValue(DATABASE_ARG);
        }
    }

    @SuppressWarnings("java:S2095")
    /**
     *
     * Method to create a Database object given these parameters
     *
     * @param  url                       URL to connect to
     * @param  username                  Username credential
     * @param  password                  Password credential
     * @param  defaultSchemaName         Default schema for connection
     * @param  defaultCatalogName        Default catalog for connection
     * @param  driver                    Driver class
     * @param  driverPropertiesFile      Additional driver properties
     * @throws DatabaseException         Thrown when there is a connection error
     *
     */
    private Database createDatabaseObject(String url,
                                        String username,
                                        String password,
                                        String defaultSchemaName,
                                        String defaultCatalogName,
                                        String driver,
                                        String driverPropertiesFile)
            throws DatabaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        String databaseClassName = null;
        Class<?> databaseClass = LiquibaseCommandLineConfiguration.DATABASE_CLASS.getCurrentValue();
        if (databaseClass != null) {
            databaseClassName = databaseClass.getCanonicalName();
        }
        String propertyProviderClass = null;
        Class<?> clazz = LiquibaseCommandLineConfiguration.PROPERTY_PROVIDER_CLASS.getCurrentValue();
        if (clazz != null) {
            propertyProviderClass = clazz.getName();
        }
        String liquibaseCatalogName = StringUtil.trimToNull(GlobalConfiguration.LIQUIBASE_CATALOG_NAME.getCurrentValue());
        String liquibaseSchemaName = StringUtil.trimToNull(GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentValue());
        String databaseChangeLogTablespaceName = StringUtil.trimToNull(GlobalConfiguration.LIQUIBASE_TABLESPACE_NAME.getCurrentValue());
        String databaseChangeLogLockTableName = StringUtil.trimToNull(GlobalConfiguration.DATABASECHANGELOGLOCK_TABLE_NAME.getCurrentValue());
        String databaseChangeLogTableName = StringUtil.trimToNull(GlobalConfiguration.DATABASECHANGELOG_TABLE_NAME.getCurrentValue());

        try {
            defaultCatalogName = StringUtil.trimToNull(defaultCatalogName);
            defaultSchemaName = StringUtil.trimToNull(defaultSchemaName);

            database = DatabaseFactory.getInstance().openDatabase(url, username, password, driver,
                    databaseClassName, driverPropertiesFile, propertyProviderClass, resourceAccessor);

            if (!database.supportsSchemas()) {
                if ((defaultSchemaName != null) && (defaultCatalogName == null)) {
                    defaultCatalogName = defaultSchemaName;
                }
                if ((liquibaseSchemaName != null) && (liquibaseCatalogName == null)) {
                    liquibaseCatalogName = liquibaseSchemaName;
                }
            }

            defaultCatalogName = StringUtil.trimToNull(defaultCatalogName);
            defaultSchemaName = StringUtil.trimToNull(defaultSchemaName);

            database.setDefaultCatalogName(defaultCatalogName);
            database.setDefaultSchemaName(defaultSchemaName);
            database.setOutputDefaultCatalog(true);
            database.setOutputDefaultSchema(true);
            database.setLiquibaseCatalogName(liquibaseCatalogName);
            database.setLiquibaseTablespaceName(databaseChangeLogTablespaceName);
            database.setLiquibaseSchemaName(liquibaseSchemaName);
            if (databaseChangeLogTableName != null) {
                database.setDatabaseChangeLogTableName(databaseChangeLogTableName);
                if (databaseChangeLogLockTableName != null) {
                    database.setDatabaseChangeLogLockTableName(databaseChangeLogLockTableName);
                } else {
                    database.setDatabaseChangeLogLockTableName(databaseChangeLogTableName + "LOCK");
                }
            }
            DatabaseUtils.initializeDatabase(defaultCatalogName, defaultSchemaName, database);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        return database;
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        if (commandDefinition.getPipeline().size() == 1) {
            commandDefinition.setInternal(true);
        }
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        if (database != null) {
            try {
                database.close();
                database = null;
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning(coreBundle.getString("problem.closing.connection"), e);
            }
        }
    }
}
