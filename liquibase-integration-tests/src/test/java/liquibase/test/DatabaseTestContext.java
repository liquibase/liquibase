package liquibase.test;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.JdbcConnection;
import liquibase.database.example.ExampleCustomDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.MockDatabase;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.exception.DatabaseException;

import java.util.*;
import java.sql.SQLException;
import java.sql.Driver;
import java.sql.Connection;
import java.io.File;
import java.net.*;

public class DatabaseTestContext {
    private static DatabaseTestContext instance = new DatabaseTestContext();

    private Set<Database> availableDatabases = new HashSet<Database>();
    private Set<Database> allDatabases;
    private Set<DatabaseConnection> availableConnections;

    private final String[] DEFAULT_TEST_URLS = new String[]{
            "jdbc:Cache://127.0.0.1:1972/liquibase",
            "jdbc:db2://localhost:50000/liquibas",
            "jdbc:derby:liquibase;create=true",
            "jdbc:firebirdsql:localhost/3050:c:\\firebird\\liquibase.fdb",
            "jdbc:h2:mem:liquibase",
            "jdbc:hsqldb:mem:liquibase",
            "jdbc:jtds:sqlserver://localhost;databaseName=liquibase",
//            "jdbc:sqlserver://localhost;databaseName=liquibase",
            "jdbc:mysql://localhost/liquibase",
            "jdbc:oracle:thin:@localhost/XE",
            "jdbc:postgresql://localhost/liquibase",
//            "jdbc:jtds:sybase://localhost/nathan:5000",
//            "jdbc:sybase:Tds:"+ InetAddress.getLocalHost().getHostName()+":5000/liquibase",
            "jdbc:sapdb://localhost/liquibas",
            "jdbc:sqlite:/liquibase.db",
            "jdbc:sybase:Tds:localhost:9810/servicename=prior",
    };

    private Map<String, DatabaseConnection> connectionsByUrl = new HashMap<String, DatabaseConnection>();
    private Map<String, Boolean> connectionsAttempted = new HashMap<String, Boolean>();
    public static final String ALT_SCHEMA = "LIQUIBASEB";
    public static final String ALT_TABLESPACE = "LIQUIBASE2";
    private static final String TEST_DATABASES_PROPERTY = "test.databases";
    private ResourceAccessor resourceAccessor;

    private DatabaseConnection openConnection(final String url) throws Exception {
        if (connectionsAttempted.containsKey(url)) {
            JdbcConnection connection = (JdbcConnection) connectionsByUrl.get(url);
            if (!connection.getUnderlyingConnection().isClosed()) {
                return connectionsByUrl.get(url);
            }
        }
        connectionsAttempted.put(url, Boolean.TRUE);

        if (System.getProperty(TEST_DATABASES_PROPERTY) != null) {
            boolean shouldTest = false;
            String[] databasesToTest = System.getProperty(TEST_DATABASES_PROPERTY).split("\\s*,\\s*");
            for (String database : databasesToTest) {
                if (url.indexOf(database) >= 0) {
                    shouldTest = true;
                }
            }
            if (!shouldTest) {
                System.out.println("test.databases system property forbids testing against " + url);
                return null;
            } else {
                System.out.println("Will be tested against " + url);
            }
        }

        DatabaseConnection connection = openDatabaseConnection(url);
        if (connection == null) {
            return null;
        }

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        final DatabaseConnection databaseConnection = database.getConnection();

        if (databaseConnection.getAutoCommit()) {
            databaseConnection.setAutoCommit(false);
        }

        try {
            if (url.startsWith("jdbc:hsql")) {
                ((JdbcConnection) databaseConnection).getUnderlyingConnection().createStatement().execute("CREATE SCHEMA " + ALT_SCHEMA + " AUTHORIZATION DBA");
            } else if (url.startsWith("jdbc:sqlserver")
                    || url.startsWith("jdbc:postgresql")
                    || url.startsWith("jdbc:h2")) {
                ((JdbcConnection) databaseConnection).getUnderlyingConnection().createStatement().execute("CREATE SCHEMA " + ALT_SCHEMA);
            }
            if (!databaseConnection.getAutoCommit()) {
                databaseConnection.commit();
            }
        } catch (SQLException e) {
//            e.printStackTrace();
            ; //schema already exists
        } finally {
            databaseConnection.rollback();
        }

        connectionsByUrl.put(url, databaseConnection);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                try {
                    try {
                        if (!((JdbcConnection) databaseConnection).getUnderlyingConnection().getAutoCommit()) {
                            ((JdbcConnection) databaseConnection).getUnderlyingConnection().rollback();
                        }
                    } catch (SQLException e) {
                        ;
                    }


                    ((JdbcConnection) databaseConnection).getUnderlyingConnection().close();
                } catch (SQLException e) {
                    System.out.println("Could not close " + url);
                    e.printStackTrace();
                }
            }
        }));

        return databaseConnection;
    }

    public DatabaseConnection openDatabaseConnection(String url) throws Exception {
        String username = getUsername(url);
        String password = getPassword(url);


        JUnitJDBCDriverClassLoader jdbcDriverLoader = JUnitJDBCDriverClassLoader.getInstance();
        final Driver driver;
        try {
            driver = (Driver) Class.forName(DatabaseFactory.getInstance().findDefaultDriver(url), true, jdbcDriverLoader).newInstance();
        } catch (ClassNotFoundException e) {
            System.out.println("Could not connect to " + url + ": Will not test against.  " + e.getMessage());
            return null; //could not connect
        }

        Properties info = new Properties();
        info.put("user", username);
        if (password != null) {
            info.put("password", password);
        }

        Connection connection;
        try {
            connection = driver.connect(url, info);
        } catch (SQLException e) {
            System.out.println("Could not connect to " + url + ": Will not test against.  " + e.getMessage());
            return null; //could not connect
        }
        if (connection == null) {
            throw new DatabaseException("Connection could not be created to " + url + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
        }

        return new JdbcConnection(connection);
    }

    private String getUsername(String url) {
        if (url.startsWith("jdbc:hsqldb")) {
            return "sa";
        }
        return "liquibase";
    }

    private String getPassword(String url) {
        if (url.startsWith("jdbc:hsqldb")) {
            return "";
        }
        return "liquibase";
    }

    public static DatabaseTestContext getInstance() {
        return instance;
    }


    public String[] getTestUrls() {
        return DEFAULT_TEST_URLS;
    }

    public Set<Database> getAllDatabases() {
        if (allDatabases == null) {
            allDatabases = new HashSet<Database>();

            allDatabases.addAll(DatabaseFactory.getInstance().getImplementedDatabases());

            List<Database> toRemove = new ArrayList<Database>();
            for (Database database : allDatabases) {
                if (database instanceof SQLiteDatabase //todo: re-enable sqlite testing
                        || database instanceof MockDatabase
                        || database instanceof ExampleCustomDatabase) {
                    toRemove.add(database);
                }
            }
            allDatabases.removeAll(toRemove);
        }
        return allDatabases;
    }

    public Set<Database> getAvailableDatabases() throws Exception {
        if (availableDatabases.size() == 0) {
            for (DatabaseConnection conn : getAvailableConnections()) {
                availableDatabases.add(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
            }
        }
        return availableDatabases;
    }


    public Set<DatabaseConnection> getAvailableConnections() throws Exception {
        if (availableConnections == null) {
            availableConnections = new HashSet<DatabaseConnection>();
            for (String url : getTestUrls()) {
//                if (url.indexOf("jtds") >= 0) {
//                    continue;
//                }
                DatabaseConnection connection = openConnection(url);
                if (connection != null) {
                    availableConnections.add(connection);
                }
            }
        }

        return availableConnections;
    }

    public DatabaseConnection getConnection(String url) throws Exception {
        return openConnection(url);
    }

    public String getTestUrl(Database database) {
        for (String url : getTestUrls()) {
            if (database.getDefaultDriver(url) != null) {
                return url;
            }
        }
        throw new RuntimeException("Could not find url for " + database);
    }
}
