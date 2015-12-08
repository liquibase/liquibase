package liquibase.database;

import liquibase.Scope;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.AbstractServiceFactory;
import liquibase.servicelocator.Service;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.Snapshot;
import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.util.*;

public class DatabaseFactory extends AbstractServiceFactory<Database> {
    private Logger log;

    protected DatabaseFactory(Scope scope) {
        super(scope);
        log = LoggerFactory.getLogger(DatabaseFactory.class);
    }

    @Override
    protected Class<Database> getServiceClass() {
        return Database.class;
    }

    @Override
    protected int getPriority(Database obj, Scope scope, Object... args) {
        String databaseName = (String) args[0];

        if (obj.getShortName().equals(databaseName)) {
            return obj.getPriority(scope);
        } else {
            return Service.PRIORITY_NOT_APPLICABLE;
        }
    }

    /**
     * Returns instances of all implemented database types.
     */
    public List<Database> getImplementedDatabases() {
        List<Database> returnList = new ArrayList<>();
        for (Database db : getRootScope().getSingleton(ServiceLocator.class).findAllServices(Database.class)) {
            if (!(db instanceof InternalDatabase)) {
                returnList.add(db);
            }
        }
        return returnList;
    }

    /**
     * Returns instances of all "internal" database types.
     */
    public List<Database> getInternalDatabases() {
        List<Database> returnList = new ArrayList<>();
        for (Database db : getRootScope().getSingleton(ServiceLocator.class).findAllServices(Database.class)) {
            if (db instanceof InternalDatabase) {
                returnList.add(db);
            }
        }
        return returnList;
    }

    public Database findCorrectDatabaseImplementation(DatabaseConnection connection) throws DatabaseException {

        List<Database> foundDatabases = new ArrayList<>();

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

        if (foundDatabases.size() == 0) {
            log.warn("Unknown database: " + connection.getDatabaseProductName());
            UnsupportedDatabase unsupportedDB = new UnsupportedDatabase();
            unsupportedDB.setConnection(connection);
            return unsupportedDB;
        }

        Database returnDatabase;
        try {
            Collections.sort(foundDatabases, new Comparator<Database>() {
                @Override
                public int compare(Database o1, Database o2) {
                    return Integer.valueOf(o1.getPriority(getRootScope())).compareTo(o2.getPriority(getRootScope()));
                }
            });

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
            return new OfflineConnection(url, resourceAccessor);
        }

        driver = StringUtils.trimToNull(driver);
        if (driver == null) {
            driver = this.findDefaultDriver(url);
        }

        try {
            Driver driverObject;
//            if (databaseClass != null) {
//                this.clearRegistry();
//                this.register((Database) Class.forName(databaseClass, true, resourceAccessor.toClassLoader()).newInstance());
//            }

            try {
                if (driver == null) {
                    driver = this.findDefaultDriver(url);
                }

                if (driver == null) {
                    throw new RuntimeException("Driver class was not specified and could not be determined from the url (" + url + ")");
                }

                driverObject = (Driver) Class.forName(driver, true, resourceAccessor.toClassLoader()).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot find database driver: " + e.getMessage());
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
//                    System.out.println("Loading properties from the file:'" + driverPropertiesFile + "'");
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


//            System.out.println("Properties:");
//            for (Map.Entry entry : driverProperties.entrySet()) {
//                System.out.println("Key:'"+entry.getKey().toString()+"' Value:'"+entry.getValue().toString()+"'");
//            }


//            System.out.println("Connecting to the URL:'"+url+"' using driver:'"+driverObject.getClass().getName()+"'");
            Connection connection = driverObject.connect(url, driverProperties);
//            System.out.println("Connection has been created");
            if (connection == null) {
                throw new DatabaseException("Connection could not be created to " + url + " with driver " + driverObject.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
            }

            return new JdbcConnection(connection);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public String findDefaultDriver(String url) {
        for (Database database : this.getImplementedDatabases()) {
            String defaultDriver = database.getDefaultDriver(url);
            if (defaultDriver != null) {
                return defaultDriver;
            }
        }

        return null;
    }

    public Database getDatabase(String shortName) {
        return getService(getRootScope(), shortName);

    }

    /**
     * Creates a new Database instance with an offline connection pointing to the given snapshot
     */
    public Database fromSnapshot(Snapshot snapshot) {
        Database database = snapshot.getScope().getDatabase();

        DatabaseConnection conn = new OfflineConnection("offline:" + database.getShortName(), snapshot, snapshot.getScope().getResourceAccessor());

        Database returnDatabase = null;
        try {
            returnDatabase = findCorrectDatabaseImplementation(conn);
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        returnDatabase.setConnection(conn);

        return returnDatabase;
    }

}
