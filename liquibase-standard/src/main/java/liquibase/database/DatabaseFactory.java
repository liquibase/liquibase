package liquibase.database;

import liquibase.Scope;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.PrioritizedService;
import liquibase.util.StringUtil;
import liquibase.util.SystemUtil;
import liquibase.SingletonObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class DatabaseFactory implements SingletonObject {
    private static final Logger LOG = Scope.getCurrentScope().getLog(DatabaseFactory.class);
    private static DatabaseFactory instance;
    private final Map<String, Database> implementedDatabases = new ConcurrentHashMap<>();
    private final Map<String, Database> internalDatabases = new ConcurrentHashMap<>();

    private DatabaseFactory() {
        try {
            for (Database database : Scope.getCurrentScope().getServiceLocator().findInstances(Database.class)) {
                register(database);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static synchronized DatabaseFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseFactory();
        }
        return instance;
    }

    /**
     * Set singleton instance. Primarily used in testing
     */
    public static synchronized void setInstance(DatabaseFactory databaseFactory) {
        instance = databaseFactory;
    }

    public static synchronized void reset() {
        instance = new DatabaseFactory();
    }

    /**
     * Returns instances of all implemented database types.
     */
    public List<Database> getImplementedDatabases() {
        return new ArrayList<>(implementedDatabases.values());
    }

    /**
     * Returns instances of all "internal" database types.
     */
    public List<Database> getInternalDatabases() {
        return new ArrayList<>(internalDatabases.values());
    }

    public void register(Database database) {
        Map<String, Database> map;
        if (database instanceof InternalDatabase) {
            map = internalDatabases;
        } else {
            map = implementedDatabases;

        }

        map.compute(
                database.getShortName(),
                (name, existing) -> Stream
                        .of(existing, database)
                        .filter(Objects::nonNull)
                        .min(PrioritizedService.COMPARATOR)
                        .orElseThrow(IllegalStateException::new)
        );
    }

    public Database findCorrectDatabaseImplementation(DatabaseConnection connection) throws DatabaseException {

        SortedSet<Database> foundDatabases = new TreeSet<>(PrioritizedService.COMPARATOR);

        for (Database implementedDatabase : getImplementedDatabases()) {
            if (connection instanceof OfflineConnection) {
                if (((OfflineConnection) connection).isCorrectDatabaseImplementation(implementedDatabase)) {
                    foundDatabases.add(implementedDatabase);
                }
            } else {
                if (implementedDatabase.isCorrectDatabaseImplementation(connection)) {
                    foundDatabases.add(implementedDatabase);
                }
            }
        }

        if (foundDatabases.isEmpty()) {
            LOG.warning("Unknown database: " + connection.getDatabaseProductName());
            UnsupportedDatabase unsupportedDB = new UnsupportedDatabase();
            unsupportedDB.setConnection(connection);
            return unsupportedDB;
        }

        Database returnDatabase;
        try {
            returnDatabase = foundDatabases.iterator().next().getClass().getConstructor().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

        returnDatabase.setConnection(connection);
        return returnDatabase;
    }

    public Database openDatabase(String url,
                            String username,
                            String password,
                            String propertyProviderClass,
                            ResourceAccessor resourceAccessor) throws DatabaseException {
        return openDatabase(url, username, password, null, null, null, propertyProviderClass, resourceAccessor);
    }

    public Database openDatabase(String url,
                            String username,
                            String password,
                            String driver,
                            String databaseClass,
                            String driverPropertiesFile,
                            String propertyProviderClass,
                            ResourceAccessor resourceAccessor) throws DatabaseException {
        return this.findCorrectDatabaseImplementation(openConnection(url, username, password, driver, databaseClass, driverPropertiesFile, propertyProviderClass, resourceAccessor));
    }

    public Database openDatabase(String url,
                                 String username,
                                 String driver,
                                 String databaseClass,
                                 Properties driverProperties,
                                 ResourceAccessor resourceAccessor) throws DatabaseException {
        return this.findCorrectDatabaseImplementation(openConnection(url, username, driver, databaseClass, driverProperties, resourceAccessor));
    }

    public DatabaseConnection openConnection(String url,
                                             String username,
                                             String password,
                                             String propertyProvider,
                                             ResourceAccessor resourceAccessor) throws DatabaseException {

        return openConnection(url, username, password, null, null, null, propertyProvider, resourceAccessor);
    }

    public DatabaseConnection openConnection(String url,
                                             String username,
                                             String password,
                                             String driver,
                                             String databaseClass,
                                             String driverPropertiesFile,
                                             String propertyProviderClass,
                                             ResourceAccessor resourceAccessor) throws DatabaseException {
        Properties driverProperties;
        try {
            driverProperties = buildDriverProperties(username, password, driverPropertiesFile, propertyProviderClass);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
        return openConnection(url, username, driver, databaseClass, driverProperties, resourceAccessor);
    }

    @SuppressWarnings("java:S2095")
    public DatabaseConnection openConnection(String url,
                                             String username,
                                             String driver,
                                             String databaseClass,
                                             Properties driverProperties,
                                             ResourceAccessor resourceAccessor) throws DatabaseException {

        if (url.startsWith("offline:")) {
            OfflineConnection offlineConnection = new OfflineConnection(url, resourceAccessor);
            offlineConnection.setConnectionUserName(username);
            return offlineConnection;
        }

        DatabaseConnection databaseConnection;
        try {
            DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
            if (databaseClass != null) {
                databaseFactory.clearRegistry();
                databaseFactory.register((Database) Class.forName(databaseClass, true, Scope.getCurrentScope().getClassLoader()).getConstructor().newInstance());
            }

            String selectedDriverClass = findDriverClass(url, driver, databaseFactory);
            Driver driverObject = loadDriver(selectedDriverClass);

            if (driverObject instanceof LiquibaseExtDriver) {
                ((LiquibaseExtDriver) driverObject).setResourceAccessor(resourceAccessor);
            }

            if(selectedDriverClass.contains("oracle")) {
              driverProperties.put("remarksReporting", "true");
            } else if(selectedDriverClass.contains("mysql")) {
              driverProperties.put("useInformationSchema", "true");
            }

            LOG.fine("Connecting to the URL:'" + JdbcConnection.sanitizeUrl(url) + "' using driver:'" + driverObject.getClass().getName() + "'");
            databaseConnection = ConnectionServiceFactory.getInstance().create(url, driverObject, driverProperties);
            LOG.fine("Connection has been created");
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
        return databaseConnection;
    }

    /**
     * Returns the Java class name of the JDBC driver class (e.g. "org.mariadb.jdbc.Driver")
     * for the specified JDBC URL, if any Database class supports that URL.
     *
     * @param url the JDBC URL to analyse
     * @return a Database object supporting the URL. May also return null if the JDBC URL is unknown to all handlers.
     */
    public String findDefaultDriver(String url) {
        for (Database database : this.getImplementedDatabases()) {
            String defaultDriver = database.getDefaultDriver(url);
            if (defaultDriver != null) {
                return defaultDriver;
            }
        }

        return null;
    }

    /**
     * Removes all registered databases, even built in ones.  Useful for forcing a particular database implementation
     */
    public void clearRegistry() {
        implementedDatabases.clear();
    }

    public Database getDatabase(String shortName) {
        return implementedDatabases.get(shortName);

    }

    private String findDriverClass(String url, String driver, DatabaseFactory databaseFactory) {
        String selectedDriverClass = StringUtil.trimToNull(driver);
        if (selectedDriverClass == null) {
            selectedDriverClass = databaseFactory.findDefaultDriver(url);
        }

        if (selectedDriverClass == null) {
            throw new RuntimeException("Driver class was not specified and could not be determined from the url (" + url + ")");
        }
        return selectedDriverClass;
    }

    private Driver loadDriver(String driverClass) {
        Driver driverObject;
        try {
            driverObject = (Driver) Class.forName(driverClass, true, Scope.getCurrentScope().getClassLoader()).getConstructor().newInstance();
        } catch (java.lang.UnsupportedClassVersionError e) {
            throw new UnexpectedLiquibaseException(String.format("Your database driver %s is not compatible with Java version %s. " +
                    "You will need to either upgrade your Java version or install a different driver jar file.", driverClass, SystemUtil.getJavaVersion()), e);
        } catch (Exception e) {
            throw new RuntimeException("Cannot find database driver: " + e.getMessage());
        }
        return driverObject;
    }

    private Properties buildDriverProperties(String username, String password, String driverPropertiesFile, String propertyProviderClass) {
        Properties driverProperties;
        try {
            if (propertyProviderClass == null) {
                driverProperties = new Properties();
            } else {
                driverProperties = (Properties) Class.forName(propertyProviderClass, true, Scope.getCurrentScope().getClassLoader()).getConstructor().newInstance();
            }

            if (username != null) {
                driverProperties.put("user", username);
            }
            if (password != null) {
                driverProperties.put("password", password);
            }
            if (null != driverPropertiesFile) {
                    PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
                    Resource driverProperty = pathHandlerFactory.getResource(driverPropertiesFile);
                    if (driverProperty.exists()) {
                        try (InputStream stream = driverProperty.openInputStream()) {
                            LOG.fine(
                                    "Loading properties from the file:'" + driverPropertiesFile + "'"
                            );
                            driverProperties.load(stream);
                        }
                    } else {
                        throw new RuntimeException("Can't open JDBC Driver specific properties from the file: '"
                                + driverPropertiesFile + "'");
                    }
                }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | IOException e) {
            throw new RuntimeException("Exception opening JDBC Driver specific properties from the file: '"
                    + driverPropertiesFile + "'", e);
        }


        LOG.fine("Properties:");
        for (Map.Entry<Object, Object> entry : driverProperties.entrySet()) {
            if (entry.getKey().toString().toLowerCase().contains("password")) {
                Scope.getCurrentScope().getLog(getClass()).fine("Key:'" + entry.getKey().toString() + "' Value:'**********'");
            } else {
                LOG.fine("Key:'" + entry.getKey().toString() + "' Value:'" + entry.getValue().toString() + "'");
            }
        }
        return driverProperties;
    }
}
