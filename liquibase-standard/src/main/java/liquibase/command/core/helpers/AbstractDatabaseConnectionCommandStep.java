package liquibase.command.core.helpers;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CleanUpCommandStep;
import liquibase.command.CommandResultsBuilder;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.DatabaseUtils;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.license.LicenseTrack;
import liquibase.license.LicenseTrackingArgs;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Abstract CommandStep providing database connectivity.
 */
public abstract class AbstractDatabaseConnectionCommandStep extends AbstractHelperCommandStep implements CleanUpCommandStep {

    public static final String[] COMMAND_NAME = {"abstractDatabaseConnectionCommandStep"};
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    private Database database;


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
    protected Database createDatabaseObject(String url,
                                        String username,
                                        String password,
                                        String defaultSchemaName,
                                        String defaultCatalogName,
                                        String driver,
                                        String driverPropertiesFile,
                                        String liquibaseCatalogName,
                                        String liquibaseSchemaName) throws DatabaseException {
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
        String databaseChangeLogTablespaceName = StringUtil.trimToNull(GlobalConfiguration.LIQUIBASE_TABLESPACE_NAME.getCurrentValue());
        String databaseChangeLogLockTableName = StringUtil.trimToNull(GlobalConfiguration.DATABASECHANGELOGLOCK_TABLE_NAME.getCurrentValue());
        String databaseChangeLogTableName = StringUtil.trimToNull(GlobalConfiguration.DATABASECHANGELOG_TABLE_NAME.getCurrentValue());

        try {
            defaultCatalogName = StringUtil.trimToNull(defaultCatalogName);
            defaultSchemaName = StringUtil.trimToNull(defaultSchemaName);

            database = DatabaseFactory.getInstance().openDatabase(url, username, password, driver,
                    databaseClassName, driverPropertiesFile, propertyProviderClass, resourceAccessor);

            if (!database.supports(Schema.class)) {
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
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        // this class only closes a database that it created
        if (database != null) {
            try {
                database.close();
                database = null;
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning(coreBundle.getString("problem.closing.connection"), e);
            }
        }
    }

    protected void logLicenseUsage(String url, Database database, boolean isTarget, boolean isReference) {
        if (Boolean.TRUE.equals(LicenseTrackingArgs.ENABLED.getCurrentValue())) {
            try {
                DatabaseConnection connection = database.getConnection();
                Connection underlyingConnection = connection.getUnderlyingConnection();
                String schema = underlyingConnection == null ? null : underlyingConnection.getSchema();
                String catalog = underlyingConnection == null ? null : underlyingConnection.getCatalog();
                String databaseName = database.getDatabaseProductName();
                Scope.getCurrentScope().getLicenseTrackList().getLicenseTracks().add(new LicenseTrack(removeQueryParameters(JdbcConnection.sanitizeUrl(url, true)), schema, catalog, databaseName, isReference, isTarget));
            } catch (SQLException | URISyntaxException e) {
                Scope.getCurrentScope().getLog(getClass()).severe("Failed to handle license tracking event", e);
            }
        }
    }

    private static String removeQueryParameters(String jdbcUrl) throws URISyntaxException {
        String jdbcPrefix = "jdbc:";
        boolean startsWithJdbc = jdbcUrl.startsWith(jdbcPrefix);
        URI uri = new URI(jdbcUrl.replaceFirst(jdbcPrefix, StringUtils.EMPTY));
        String cleanedUri = uri.toString();
        try {
            // This the more robust (IMO) way of stripping parameters, but doesn't work for all path formats.
            cleanedUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment()).toString();
        } catch (Exception e) {
            // Remove the query parameters manually.
            cleanedUri = cleanedUri.split("\\?")[0];
        }
        // Neither of the methods for cleansing the parameters handle SQL Server type parameters, so these must be handled manually.
        cleanedUri = cleanedUri.split("\\;")[0];

        if (startsWithJdbc) {
            cleanedUri = jdbcPrefix + cleanedUri;
        }
        // Remove trailing slash
        if (cleanedUri.endsWith("/")) {
            cleanedUri = cleanedUri.substring(0, cleanedUri.length() - 1);
        }
        return cleanedUri;
    }
}
