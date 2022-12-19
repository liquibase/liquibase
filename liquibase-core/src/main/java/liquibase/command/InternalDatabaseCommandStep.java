package liquibase.command;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;
import liquibase.exception.CommandValidationException;
import liquibase.exception.DatabaseException;
import liquibase.exception.MissingRequiredArgumentException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.LiquibaseService;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

@LiquibaseService(skip = true)
public class InternalDatabaseCommandStep extends AbstractCommandStep implements CleanUpCommandStep {

    public static final String[] COMMAND_NAME = {"databasePreStep"};

    private final static List<String[]> APPLICABLE_COMMANDS = new ArrayList<>();
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;

    private Database database;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        URL_ARG = builder.argument(CommonArgumentNames.URL, String.class).required().description("The JDBC database connection URL").build();
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
    }

    /**
     * Method that allows Commands to register themselves to be able to use this CommandStep.
     * @param commandName the command name.
     */
    public static void addApplicableCommand(String[] commandName) {
        APPLICABLE_COMMANDS.add(commandName);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        commandScope.addArgumentValue(DATABASE_ARG.getName(), this.obtainDatabase(commandScope, DATABASE_ARG));
    }

    /**
     * Try to retrieve and set the database object from the command scope, otherwise creates a new one .
     *
     * @param commandScope current command scope
     * @param databaseCommandArgument the CommandArgumentDefinition identifying the expected database object
     * @throws DatabaseException Thrown when there is a connection error
     */
    private Database obtainDatabase(CommandScope commandScope, CommandArgumentDefinition<Database> databaseCommandArgument) throws DatabaseException, CommandValidationException {
        if (commandScope.getArgumentValue(databaseCommandArgument) == null) {
            CommandBuilder builder = new CommandBuilder();
            String url = commandScope.getArgumentValue(builder.argument(CommonArgumentNames.URL, String.class).build());
            if (StringUtil.isEmpty(url)) {
                throw new CommandValidationException("url", "missing required argument", new MissingRequiredArgumentException("url"));
            }
            String username = commandScope.getArgumentValue(builder.argument(CommonArgumentNames.USERNAME, String.class).build());
            String password = commandScope.getArgumentValue(builder.argument(CommonArgumentNames.PASSWORD, String.class).build());
            String defaultSchemaName = commandScope.getArgumentValue(builder.argument("defaultSchemaName", String.class).build());
            String defaultCatalogName = commandScope.getArgumentValue(builder.argument("defaultCatalogName", String.class).build());
            String driver = commandScope.getArgumentValue(builder.argument("driver", String.class).build());
            String driverPropertiesFile = commandScope.getArgumentValue(builder.argument("driverPropertiesFile", String.class).build());
            this.database = createDatabaseObject(url, username, password, defaultSchemaName, defaultCatalogName, driver, driverPropertiesFile);
            return this.database;
        } else {
            return commandScope.getArgumentValue(databaseCommandArgument);
        }
    }

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
        Class databaseClass = LiquibaseCommandLineConfiguration.DATABASE_CLASS.getCurrentValue();
        if (databaseClass != null) {
            databaseClassName = databaseClass.getCanonicalName();
        }
        String propertyProviderClass = null;
        Class clazz = LiquibaseCommandLineConfiguration.PROPERTY_PROVIDER_CLASS.getCurrentValue();
        if (clazz != null) {
            propertyProviderClass = clazz.getName();
        }
        String liquibaseCatalogName = GlobalConfiguration.LIQUIBASE_CATALOG_NAME.getCurrentValue();
        String liquibaseSchemaName = GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentValue();
        String databaseChangeLogTablespaceName = GlobalConfiguration.LIQUIBASE_TABLESPACE_NAME.getCurrentValue();
        String databaseChangeLogLockTableName = GlobalConfiguration.DATABASECHANGELOGLOCK_TABLE_NAME.getCurrentValue();
        String databaseChangeLogTableName = GlobalConfiguration.DATABASECHANGELOG_TABLE_NAME.getCurrentValue();
        Database database = CommandLineUtils.createDatabaseObject(resourceAccessor,
                        url,
                        username,
                        password,
                        driver,
                        defaultCatalogName,
                        defaultSchemaName,
                        true,
                        true,
                        databaseClassName,
                        driverPropertiesFile,
                        propertyProviderClass,
                        liquibaseCatalogName, liquibaseSchemaName,
                        databaseChangeLogTableName,
                        databaseChangeLogLockTableName);
        database.setLiquibaseTablespaceName(databaseChangeLogTablespaceName);
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
        } else {
            commandDefinition.getArgument(InternalDatabaseCommandStep.DATABASE_ARG.getName()).hide();
            commandDefinition.getArgument(InternalDatabaseCommandStep.DATABASE_ARG.getName()).setRequired(false);
            //commandDefinition.getArgument(InternalDatabaseCommandStep.URL_ARG.getName()).setRequired(false);
        }
    }

    @Override
    public int getOrder(CommandDefinition commandDefinition) {
        for (String[] commandName : APPLICABLE_COMMANDS) {
            if (commandDefinition.is(commandName)) {
                return 500;
            }
        }
        return super.getOrder(commandDefinition);
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        if (database != null) {
            try {
                database.close();
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning(coreBundle.getString("problem.closing.connection"), e);
            }
        }
    }
}
