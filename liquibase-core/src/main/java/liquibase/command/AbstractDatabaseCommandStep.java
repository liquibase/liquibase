package liquibase.command;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.resource.ResourceAccessor;

import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Convenience base class for {@link CommandStep} implementations which need a Database object.
 */
public abstract class AbstractDatabaseCommandStep extends AbstractCommandStep {
    protected Database database;
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

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
    public void createDatabaseObject(String url,
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
        boolean outputDefaultSchema = GlobalConfiguration.OUTPUT_DEFAULT_SCHEMA.getCurrentValue();
        boolean outputDefaultCatalog = GlobalConfiguration.OUTPUT_DEFAULT_CATALOG.getCurrentValue();
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
                outputDefaultCatalog,
                outputDefaultSchema,
                databaseClassName,
                driverPropertiesFile,
                propertyProviderClass,
                liquibaseCatalogName, liquibaseSchemaName,
                databaseChangeLogTableName,
                databaseChangeLogLockTableName);
        database.setLiquibaseTablespaceName(databaseChangeLogTablespaceName);
    }

    public void closeDatabase() {
        try {
            if (database != null) {
                database.rollback();
                database.close();
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning(
                coreBundle.getString("problem.closing.connection"), e);
        }
    }
}
