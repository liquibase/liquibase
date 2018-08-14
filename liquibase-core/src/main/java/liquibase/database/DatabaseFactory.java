package liquibase.database;

import liquibase.Scope;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.util.*;

public class DatabaseFactory {
    private static final Logger LOG = LogService.getLog(DatabaseFactory.class);
    private static DatabaseFactory instance;
    private Map<String, SortedSet<Database>> implementedDatabases = new HashMap<>();
    private Map<String, SortedSet<Database>> internalDatabases = new HashMap<>();

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
        List<Database> returnList = new ArrayList<>();
        for (SortedSet<Database> set : implementedDatabases.values()) {
            returnList.add(set.iterator().next());
        }
        return returnList;
    }

    /**
     * Returns instances of all "internal" database types.
     */
    public List<Database> getInternalDatabases() {
        List<Database> returnList = new ArrayList<>();
        for (SortedSet<Database> set : internalDatabases.values()) {
            returnList.add(set.iterator().next());
        }
        return returnList;
    }

    public void register(Database database) {
        Map<String, SortedSet<Database>> map = null;
        if (database instanceof InternalDatabase) {
            map = internalDatabases;
        } else {
            map = implementedDatabases;

        }

        if (!map.containsKey(database.getShortName())) {
            map.put(database.getShortName(), new TreeSet<>(new TreeSet<>(new DatabaseComparator())));
        }
        map.get(database.getShortName()).add(database);
    }

    public Database findCorrectDatabaseImplementation(DatabaseConnection connection) throws DatabaseException {

        SortedSet<Database> foundDatabases = new TreeSet<>(new DatabaseComparator());

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
            LOG.warning(LogType.LOG, "Unknown database: " + connection.getDatabaseProductName());
            UnsupportedDatabase unsupportedDB = new UnsupportedDatabase();
            unsupportedDB.setConnection(connection);
            return unsupportedDB;
        }

        Database returnDatabase;
        try {
            returnDatabase = foundDatabases.iterator().next().getClass().newInstance();
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

        if (url.startsWith("offline:")) {
            OfflineConnection offlineConnection = new OfflineConnection(url, resourceAccessor);
            offlineConnection.setConnectionUserName(username);
            return offlineConnection;
        }

        driver = StringUtil.trimToNull(driver);
        if (driver == null) {
            driver = DatabaseFactory.getInstance().findDefaultDriver(url);
        }

        try {
            Driver driverObject;
            DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
            if (databaseClass != null) {
                databaseFactory.clearRegistry();
                databaseFactory.register((Database) Class.forName(databaseClass, true, resourceAccessor.toClassLoader()).newInstance());
            }

            try {
                if (driver == null) {
                    driver = databaseFactory.findDefaultDriver(url);
                }

                if (driver == null) {
                    throw new RuntimeException("Driver class was not specified and could not be determined from the url (" + url + ")");
                }

                driverObject = (Driver) Class.forName(driver, true, resourceAccessor.toClassLoader()).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot find database driver: " + e.getMessage());
            }

            if (driverObject instanceof LiquibaseExtDriver) {
                ((LiquibaseExtDriver)driverObject).setResourceAccessor(resourceAccessor);
            }

            Properties driverProperties;
            if (propertyProviderClass == null) {
                driverProperties = new Properties();
            } else {
                driverProperties = (Properties) Class.forName(propertyProviderClass, true, resourceAccessor.toClassLoader()).newInstance();
            }

            if (username != null) {
                driverProperties.put("user", username);
            }
            if (password != null) {
                driverProperties.put("password", password);
            }
            if (null != driverPropertiesFile) {
                File propertiesFile = new File(driverPropertiesFile);
                if (propertiesFile.exists()) {
                    LOG.debug(
                            LogType.LOG, "Loading properties from the file:'" + driverPropertiesFile + "'"
                    );
                    FileInputStream inputStream = new FileInputStream(propertiesFile);
                    try {
                        driverProperties.load(inputStream);
                    } finally {
                        inputStream.close();
                    }
                } else {
                    throw new RuntimeException("Can't open JDBC Driver specific properties from the file: '"
                            + driverPropertiesFile + "'");
                }
            }


            LOG.debug(LogType.LOG, "Properties:");
            for (Map.Entry entry : driverProperties.entrySet()) {
                LOG.debug(LogType.LOG, "Key:'" + entry.getKey().toString() + "' Value:'" + entry.getValue().toString() + "'");
            }


            LOG.debug(LogType.LOG, "Connecting to the URL:'" + url + "' using driver:'" + driverObject.getClass().getName() + "'");
            Connection connection = driverObject.connect(url, driverProperties);
            LOG.debug(LogType.LOG, "Connection has been created");
            if (connection == null) {
                throw new DatabaseException("Connection could not be created to " + url + " with driver " + driverObject.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
            }

            return new JdbcConnection(connection);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
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
        if (!implementedDatabases.containsKey(shortName)) {
            return null;
        }
        return implementedDatabases.get(shortName).iterator().next();

    }

    private static class DatabaseComparator implements Comparator<Database> {
        @Override
        public int compare(Database o1, Database o2) {
            return -1 * Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
        }
    }
}
