package liquibase.command;

import liquibase.Contexts;
import liquibase.GlobalConfiguration;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.CommandValidationException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.MissingRequiredArgumentException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.lockservice.LockServiceFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Convenience base class for {@link CommandStep} implementations.
 */
public abstract class AbstractCommandStep implements CommandStep {

    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    private Database database;

    protected Database getDatabase() {
        return database;
    }

    private void setDatabase(Database database) {
        this.database = database;
    }

    /**
     * Try to retrieve and set the database object from the command scope, otherwise creates a new one .
     *
     * @param commandScope current command scope
     * @param databaseCommandArgument the CommandArgumentDefinition identifying the expected database object
     * @throws DatabaseException Thrown when there is a connection error
     */
    protected void setOrCreateDatabase(CommandScope commandScope, CommandArgumentDefinition<Database> databaseCommandArgument) throws DatabaseException, CommandValidationException {
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
            createDatabaseObject(url, username, password, defaultSchemaName, defaultCatalogName, driver, driverPropertiesFile);
        } else {
            setDatabase(commandScope.getArgumentValue(databaseCommandArgument));
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
    protected void createDatabaseObject(String url,
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
        database =
                CommandLineUtils.createDatabaseObject(resourceAccessor,
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
    }

    /**
     * Closes database connection.
     *
     * @param rollback should we rollback the connection?
     */
    protected void closeDatabase(boolean rollback) {
        try {
            if (database != null) {
                if (rollback) {
                    database.rollback();
                } else {
                    database.commit();
                }
                database.close();
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning(
                    coreBundle.getString("problem.closing.connection"), e);
        }
    }

    protected void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                     Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService =
                ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(getDatabase());
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(getDatabase()).init();
    }

    /**
     * @return {@link #ORDER_DEFAULT} if the command scope's name matches {@link #defineCommandNames()}. Otherwise {@link #ORDER_NOT_APPLICABLE}
     */
    @Override
    public int getOrder(CommandDefinition commandDefinition) {
        final String[][] definedCommandNames = defineCommandNames();
        if (definedCommandNames != null) {
            for (String[] thisCommandName : definedCommandNames) {
                if ((thisCommandName != null) && StringUtil.join(Arrays.asList(thisCommandName), " ").equalsIgnoreCase(StringUtil.join(Arrays.asList(commandDefinition.getName()), " "))) {
                    return ORDER_DEFAULT;
                }
            }
        }
        return ORDER_NOT_APPLICABLE;
    }

    /**
     * Default implementation does no additional validation.
     */
    @Override
    public void validate(CommandScope commandScope) throws CommandValidationException {
    }

    /**
     * Default implementation makes no changes
     */
    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {

    }
}
