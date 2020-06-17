package liquibase.integration.ant.type;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.tools.ant.util.LoaderUtils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseType extends DataType {
    private static final String USER_PROPERTY_NAME = "user";
    // SONAR thinks this is a hard-coded password, but actually it is only the
    // property name for the password
    @SuppressWarnings("squid:S2068")
    private static final String PASSWORD_PROPERTY_NAME = "password";

    private String driver;
    private String url;
    private String user;
    private String password;
    private ConnectionProperties connectionProperties;
    private String defaultSchemaName;
    private String defaultCatalogName;
    private String currentDateTimeFunction;
    private boolean outputDefaultSchema = true;
    private boolean outputDefaultCatalog = true;
    private String liquibaseSchemaName;
    private String liquibaseCatalogName;
    private String databaseClass;
    private String databaseChangeLogTableName;
    private String databaseChangeLogLockTableName;
    private String liquibaseTablespaceName;

    public DatabaseType(Project project) {
        setProject(project);
    }

    public Database createDatabase() {
        ClassLoader contextClassLoader = LoaderUtils.getContextClassLoader();
        return createDatabase(contextClassLoader);
    }

    public Database createDatabase(ClassLoader classLoader) {
        logParameters();
        validateParameters();
        try {
            DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
            if(databaseClass != null) {
                Database databaseInstance = (Database) ClasspathUtils.newInstance(databaseClass, classLoader, Database.class);
                databaseFactory.register(databaseInstance);
            }

            DatabaseConnection jdbcConnection;
            if (getUrl().startsWith("offline:")) {
                jdbcConnection = new OfflineConnection(getUrl(), new ClassLoaderResourceAccessor(classLoader));
            } else {
                Driver jdbcDriver = (Driver) ClasspathUtils.newInstance(getDriver(), classLoader, Driver.class);
                if(jdbcDriver == null) {
                    throw new BuildException("Unable to create Liquibase Database instance. Could not instantiate the" +
                     " JDBC driver.");
                }
                Properties connectionProps = new Properties();
                String connectionUserName = getUser();
                if((connectionUserName != null) && !connectionUserName.isEmpty()) {
                    connectionProps.setProperty(USER_PROPERTY_NAME, connectionUserName);
                }
                String connectionPassword = getPassword();
                if((connectionPassword != null) && !connectionPassword.isEmpty()) {
                    connectionProps.setProperty(PASSWORD_PROPERTY_NAME, connectionPassword);
                }
                ConnectionProperties dbConnectionProperties = getConnectionProperties();
                if(dbConnectionProperties != null) {
                    connectionProps.putAll(dbConnectionProperties.buildProperties());
                }

                Connection connection = jdbcDriver.connect(getUrl(), connectionProps);
                if(connection == null) {
                    throw new BuildException("Unable to create Liquibase database instance. Could not connect to the " +
                     "database.");
                }
                jdbcConnection = new JdbcConnection(connection);
            }

            Database database = databaseFactory.findCorrectDatabaseImplementation(jdbcConnection);

            String schemaName = getDefaultSchemaName();
            if (schemaName != null) {
                database.setDefaultSchemaName(schemaName);
            }
            String catalogName = getDefaultCatalogName();
            if (catalogName != null) {
                database.setDefaultCatalogName(catalogName);
            }
            String dbmsCurrentDateTimeFunction = getCurrentDateTimeFunction();
            if(dbmsCurrentDateTimeFunction != null) {
                database.setCurrentDateTimeFunction(dbmsCurrentDateTimeFunction);
            }

            database.setOutputDefaultSchema(isOutputDefaultSchema());
            database.setOutputDefaultCatalog(isOutputDefaultCatalog());

            String connLiquibaseSchemaName = getLiquibaseSchemaName();
            if (liquibaseSchemaName != null) {
                database.setLiquibaseSchemaName(connLiquibaseSchemaName);
            }
            
            String connLiquibaseCatalogName = getLiquibaseCatalogName();
            if(connLiquibaseCatalogName != null) {
                database.setLiquibaseCatalogName(connLiquibaseCatalogName);
            }

            String connDatabaseChangeLogTableName = getDatabaseChangeLogTableName();
            if(connDatabaseChangeLogTableName != null) {
                database.setDatabaseChangeLogTableName(connDatabaseChangeLogTableName);
            }
            
            String connDatabaseChangeLogLockTableName = getDatabaseChangeLogLockTableName();
            if(connDatabaseChangeLogLockTableName != null) {
                database.setDatabaseChangeLogLockTableName(connDatabaseChangeLogLockTableName);
            }
            
            String connLiquibaseTablespaceName = getLiquibaseTablespaceName();
            if(connLiquibaseTablespaceName != null) {
                database.setLiquibaseTablespaceName(connLiquibaseTablespaceName);
            }

            return database;
        } catch (SQLException e) {
            throw new BuildException("Unable to create Liquibase database instance. A JDBC error occurred. " + e
            .toString(), e);
        } catch (DatabaseException e) {
            throw new BuildException("Unable to create Liquibase database instance. " + e.toString(), e);
        }
    }

    private void validateParameters() {
        if(getUrl() == null) {
            throw new BuildException("JDBC URL is required.");
        }
        if((getDriver() == null) && !getUrl().startsWith("offline:")) {
            throw new BuildException("JDBC driver is required.");
        }
    }

    private void logParameters() {
        log("Creating Liquibase Database", Project.MSG_DEBUG);
        log("JDBC driver: " + driver, Project.MSG_DEBUG);
        log("JDBC URL: " + url, Project.MSG_DEBUG);
        log("JDBC username: " + user, Project.MSG_DEBUG);
        log("Default catalog name: " + defaultCatalogName, Project.MSG_DEBUG);
        log("Default schema name: " + defaultSchemaName, Project.MSG_DEBUG);
        log("Liquibase catalog name: " + liquibaseCatalogName, Project.MSG_DEBUG);
        log("Liquibase schema name: " + liquibaseSchemaName, Project.MSG_DEBUG);
        log("Liquibase tablespace name: " + liquibaseTablespaceName, Project.MSG_DEBUG);
        log("Database changelog table name: " + databaseChangeLogTableName, Project.MSG_DEBUG);
        log("Database changelog lock table name: " + databaseChangeLogLockTableName, Project.MSG_DEBUG);
        log("Output default catalog: " + outputDefaultCatalog, Project.MSG_DEBUG);
        log("Output default schema: " + outputDefaultSchema, Project.MSG_DEBUG);
        log("Current date/time function: " + currentDateTimeFunction, Project.MSG_DEBUG);
        log("Database class: " + databaseClass, Project.MSG_DEBUG);
    }

    @Override
    public void setRefid(Reference ref) {
        if((driver != null) || (url != null) || (user != null) || (password != null) || (defaultSchemaName != null)
            || (defaultCatalogName != null) || (currentDateTimeFunction != null) || (databaseClass != null) ||
            (liquibaseSchemaName != null) || (liquibaseCatalogName != null) || (databaseChangeLogTableName != null)
            || (databaseChangeLogLockTableName != null) || (liquibaseTablespaceName != null)) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    public String getDriver() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDriver() : driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getUrl() : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getUser() : user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getPassword() : password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionProperties getConnectionProperties() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getConnectionProperties() : connectionProperties;
    }

    public void addConnectionProperties(ConnectionProperties connectionProperties) {
        if(this.connectionProperties != null) {
            throw new BuildException("Only one <connectionProperties> element is allowed.");
        }
        this.connectionProperties = connectionProperties;
    }

    public String getDefaultSchemaName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDefaultSchemaName() : defaultSchemaName;
    }

    public void setDefaultSchemaName(String defaultSchemaName) {
        this.defaultSchemaName = defaultSchemaName;
    }

    public String getDefaultCatalogName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDefaultCatalogName() : defaultCatalogName;
    }

    public void setDefaultCatalogName(String defaultCatalogName) {
        this.defaultCatalogName = defaultCatalogName;
    }

    public String getCurrentDateTimeFunction() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getCurrentDateTimeFunction() : currentDateTimeFunction;
    }

    public void setCurrentDateTimeFunction(String currentDateTimeFunction) {
        this.currentDateTimeFunction = currentDateTimeFunction;
    }

    public boolean isOutputDefaultSchema() {
        return isReference() ? ((DatabaseType) getCheckedRef()).isOutputDefaultSchema() : outputDefaultSchema;
    }

    public void setOutputDefaultSchema(boolean outputDefaultSchema) {
        this.outputDefaultSchema = outputDefaultSchema;
    }

    public boolean isOutputDefaultCatalog() {
        return isReference() ? ((DatabaseType) getCheckedRef()).isOutputDefaultCatalog() : outputDefaultCatalog;
    }

    public void setOutputDefaultCatalog(boolean outputDefaultCatalog) {
        this.outputDefaultCatalog = outputDefaultCatalog;
    }

    public String getDatabaseClass() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDatabaseClass() : databaseClass;
    }

    public void setDatabaseClass(String databaseClass) {
        this.databaseClass = databaseClass;
    }

    public String getLiquibaseSchemaName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getLiquibaseSchemaName() : liquibaseSchemaName;
    }

    public void setLiquibaseSchemaName(String liquibaseSchemaName) {
        this.liquibaseSchemaName = liquibaseSchemaName;
    }

    public String getLiquibaseCatalogName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getLiquibaseCatalogName() : liquibaseCatalogName;
    }

    public void setLiquibaseCatalogName(String liquibaseCatalogName) {
        this.liquibaseCatalogName = liquibaseCatalogName;
    }

    public String getDatabaseChangeLogTableName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDatabaseChangeLogTableName() : databaseChangeLogTableName;
    }

    public void setDatabaseChangeLogTableName(String databaseChangeLogTableName) {
        this.databaseChangeLogTableName = databaseChangeLogTableName;
    }

    public String getDatabaseChangeLogLockTableName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDatabaseChangeLogLockTableName() : databaseChangeLogLockTableName;
    }

    public void setDatabaseChangeLogLockTableName(String databaseChangeLogLockTableName) {
        this.databaseChangeLogLockTableName = databaseChangeLogLockTableName;
    }

    public String getLiquibaseTablespaceName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getLiquibaseTablespaceName() : liquibaseTablespaceName;
    }

    public void setLiquibaseTablespaceName(String liquibaseTablespaceName) {
        this.liquibaseTablespaceName = liquibaseTablespaceName;
    }
}
