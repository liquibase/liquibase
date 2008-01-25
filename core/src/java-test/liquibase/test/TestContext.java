package liquibase.test;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.*;

/**
 * Controls the database connections for running tests.
 * For times we aren't wanting to run the database-hitting tests, set the "test.databases" system property
 * to be a comma-separated list of the databses we want to test against.  The string is checked against the database url.  
 */
public class TestContext {
    private static TestContext instance = new TestContext();

    private Set<Database> availableDatabases = new HashSet<Database>();
    private Set<Database> allDatabases;
    private Set<DatabaseConnection> availableConnections;

    private final String[] DEFAULT_TEST_URLS = new String[]{
            "jdbc:Cache://127.0.0.1:1972/liquibase",
            "jdbc:db2://localhost:50000/liquibas",
//            "jdbc:derby:liquibase;create=true",
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
    };

    private Map<String, DatabaseConnection> connectionsByUrl = new HashMap<String, DatabaseConnection>();
    private Map<String, Boolean> connectionsAttempted = new HashMap<String, Boolean>();
    public static final String ALT_SCHEMA = "LIQUIBASEB";
    public static final String ALT_TABLESPACE = "LIQUIBASE2";
    private static final String TEST_DATABASES_PROPERTY = "test.databases";

    private DatabaseConnection openConnection(final String url) throws Exception {
        if (connectionsAttempted.containsKey(url)) {
            return connectionsByUrl.get(url);
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
            }
        }

        String username = getUsername(url);
        String password = getPassword(url);

        JUnitJDBCDriverClassLoader jdbcDriverLoader = JUnitJDBCDriverClassLoader.getInstance();
        final Driver driver = (Driver) Class.forName(DatabaseFactory.getInstance().findDefaultDriver(url), true, jdbcDriverLoader).newInstance();

        Properties info = new Properties();
        info.put("user", username);
        if (password != null) {
            info.put("password", password);
        }

        Connection connection;
        try {
            connection = driver.connect(url, info);
        } catch (SQLException e) {
            System.out.println("Could not connect to " + url + ": Will not test against");
            return null; //could not connect
        }
        if (connection == null) {
            throw new JDBCException("Connection could not be created to " + url + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
        }

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        final DatabaseConnection databaseConnection = database.getConnection();

        databaseConnection.setAutoCommit(false);

        try {
            if (url.startsWith("jdbc:hsql")) {
                databaseConnection.createStatement().execute("CREATE SCHEMA " + ALT_SCHEMA + " AUTHORIZATION DBA");
            } else if (url.startsWith("jdbc:sqlserver")
                    || url.startsWith("jdbc:postgresql")
                    || url.startsWith("jdbc:h2")) {
                databaseConnection.createStatement().execute("CREATE SCHEMA " + ALT_SCHEMA);
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

//        Migrator migrator = new Migrator(null, null);
//        migrator.init(databaseConnection);
//        migrator.dropAll();
//        if (migrator.getDatabase().supportsSchemas()) {
//            migrator.dropAll(TestContext.ALT_SCHEMA);
//        }

        connectionsByUrl.put(url, databaseConnection);

//        Migrator migrator = new Migrator(null, null);
//        migrator.init(connection);
//        if (database.supportsSchemas()) {
//            migrator.dropAll(ALT_SCHEMA, database.getDefaultSchemaName());
//        } else {
//            migrator.dropAll(new String[]{null});
//        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                try {
                    try {
                        if (!databaseConnection.getAutoCommit()) {
                            databaseConnection.rollback();
                        }
                    } catch (SQLException e) {
                        ;
                    }

//                    if (connection.getMetaData().getURL().startsWith("jdbc:derby")) {
//                        try {
//                            driver.connect("jdbc:derby:liquibase;shutdown=true", new Properties());
//                        } catch (SQLException e) {
//                            ;//clean shutdown throws exception.//NOPMD
//                        }
//                    } else if (connection.getMetaData().getURL().startsWith("jdbc:hsqldb")) {
//                        try {
//                            Statement statement = connection.createStatement();
//                            statement.execute("SHUTDOWN");
//                            statement.close();
//                        } catch (SQLException e) {
//                            ;
//                        }
//
//                    }

                    databaseConnection.close();
//                    System.out.println(url+" closed successfully");
                } catch (SQLException e) {
                    System.out.println("Could not close " + url);
                    e.printStackTrace();
                }
            }
        }));

        return databaseConnection;
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

    public static TestContext getInstance() {
        return instance;
    }


    public String[] getTestUrls() {
        return DEFAULT_TEST_URLS;
    }

    public Set<Database> getAllDatabases() {
        if (allDatabases == null) {
            allDatabases = new HashSet<Database>();

            allDatabases.addAll(DatabaseFactory.getInstance().getImplementedDatabases());

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
        throw new RuntimeException("Could not find url for "+database);
    }
}
