package liquibase.test;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.example.ExampleCustomDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.listener.SqlListener;
import liquibase.logging.LogType;
import liquibase.resource.ResourceAccessor;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.*;

public class DatabaseTestContext {
    public static final String ALT_CATALOG = "LIQUIBASEC";
    public static final String ALT_SCHEMA = "LIQUIBASEB";
    public static final String ALT_TABLESPACE = "LIQUIBASE2";
    private static final String TEST_DATABASES_PROPERTY = "test.databases";
    private static DatabaseTestContext instance = new DatabaseTestContext();
    private final DatabaseTestURL[] DEFAULT_TEST_DATABASES = new DatabaseTestURL[]{
            /* @todo Extract all remaining connection string examples into liquibase.integrationtest.properties, then delete this code block. */
            /*
                    new DatabaseTestURL("Cache","jdbc:Cache://"+AbstractIntegrationTest.getDatabaseServerHostname("Cache")+":1972/liquibase"),
                    new DatabaseTestURL("DB2","jdbc:db2://"+AbstractIntegrationTest.getDatabaseServerHostname("DB2")+":50000/liquibas"),
                    new DatabaseTestURL("Derby","jdbc:derby:liquibase;create=true"),
                    new DatabaseTestURL("FireBird","jdbc:firebirdsql:"+AbstractIntegrationTest.getDatabaseServerHostname("Firebird")+"/3050:c:\\firebird\\liquibase.fdb"),
                    new DatabaseTestURL("H2","jdbc:h2:mem:liquibase"),
                    new DatabaseTestURL("Hsql","jdbc:hsqldb:mem:liquibase"),
                    new DatabaseTestURL("MssqlJtds","jdbc:jtds:sqlserver://"+AbstractIntegrationTest.getDatabaseServerHostname("MSSQL")+";databaseName=liquibase"),
        //            "jdbc:sqlserver://localhost;databaseName=liquibase",
                    new DatabaseTestURL("MySQL","jdbc:mysql://"+AbstractIntegrationTest.getDatabaseServerHostname("mysql")+"/liquibase"),
                    new DatabaseTestURL("Oracle","jdbc:oracle:thin:@"+AbstractIntegrationTest.getDatabaseServerHostname("oracle")+"/XE"),
        //            "jdbc:jtds:sybase://localhost/nathan:5000",
        //            "jdbc:sybase:Tds:"+ InetAddress.getLocalHost().getHostName()+":5000/liquibase",
                    new DatabaseTestURL("SAPDB","jdbc:sapdb://"+AbstractIntegrationTest.getDatabaseServerHostname("sapdb")+"/liquibas"),
                    new DatabaseTestURL("SQLite","jdbc:sqlite:/liquibase.db"),
                    new DatabaseTestURL("SybaseJtds","jdbc:sybase:Tds:"+AbstractIntegrationTest.getDatabaseServerHostname("sybase")+":9810/servicename=prior")
                    */
    };
    private Set<Database> availableDatabases = new HashSet<Database>();
    private Set<Database> allDatabases;
    private Set<DatabaseConnection> availableConnections;
    private Map<String, DatabaseConnection> connectionsByUrl = new HashMap<String, DatabaseConnection>();
    private Map<String, Boolean> connectionsAttempted = new HashMap<String, Boolean>();
    private ResourceAccessor resourceAccessor;

    public static DatabaseTestContext getInstance() {
        return instance;
    }

    /**
     * Makes a best effort to gracefully shut down a (possible open) databaseConnection and ignores any
     * errors that happen during that process.
     *
     * @param databaseConnection
     */
    private static void shutdownConnection(JdbcConnection databaseConnection) {
        try {
            try {
                if (!databaseConnection.getUnderlyingConnection().getAutoCommit()) {
                    databaseConnection.getUnderlyingConnection().rollback();
                }
            } catch (SQLException e) {
                // Ignore. If rollback fails or is impossible, there is nothing we can do about it.
            }

            // Close the JDBC connection
            databaseConnection.getUnderlyingConnection().close();
        } catch (SQLException e) {
            Scope.getCurrentScope().getLog(DatabaseTestContext.class).warning("Could not close the following connection: " + databaseConnection.getURL(), e);
        }
    }

    /**
     * Returns a DatabaseConnection for a givenUrl is one is already open. If not, attempts to create it, but only
     * if a previous attempt at creating the connection has NOT failed (to prevent unnecessary connection attempts
     * during the integration tests).
     *
     * @param givenUrl The JDBC URL to connect to
     * @param username the user name to use to log in to the instance (may be null, esp. for embedded DBMS)
     * @param password the password for the username (may be null)
     * @return a DatabaseConnection if one has been established or fetched from the cache successfully, null otherwise
     * @throws Exception if an error occurs while trying to get the connection
     */
    private DatabaseConnection openConnection(final String givenUrl,
                                              final String username, final String password) throws Exception {
        // Insert the temp dir path and ensure our replacement ends with /
        String tempDir = System.getProperty("java.io.tmpdir");
        if (!tempDir.endsWith(System.getProperty("file.separator")))
            tempDir += System.getProperty("file.separator");

        String tempUrl = givenUrl.replace("***TEMPDIR***/", tempDir);
        final String url = tempUrl;

        if (connectionsAttempted.containsKey(url)) {
            JdbcConnection connection = (JdbcConnection) connectionsByUrl.get(url);
            if (connection == null) {
                return null;
            } else if (connection.getUnderlyingConnection().isClosed()) {
                connectionsByUrl.put(url, openDatabaseConnection(url, username, password));
            }
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
            } else {
                System.out.println("Will be tested against " + url);
            }
        }

        DatabaseConnection connection = openDatabaseConnection(url, username, password);
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
                String sql = "CREATE SCHEMA " + ALT_SCHEMA + " AUTHORIZATION DBA";
                for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                    listener.writeSqlWillRun(sql);
                }
                ((JdbcConnection) databaseConnection).getUnderlyingConnection().createStatement().execute(sql);
            } else if (url.startsWith("jdbc:sqlserver")
                    || url.startsWith("jdbc:postgresql")
                    || url.startsWith("jdbc:h2")) {
                String sql = "CREATE SCHEMA " + ALT_SCHEMA;
                for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                    listener.writeSqlWillRun(sql);
                }
                ((JdbcConnection) databaseConnection).getUnderlyingConnection().createStatement().execute(sql);
            }
            if (!databaseConnection.getAutoCommit()) {
                databaseConnection.commit();
            }
        } catch (SQLException e) {
            // schema already exists
        } finally {
            try {
                databaseConnection.rollback();
            } catch (DatabaseException e) {
                if (database instanceof AbstractDb2Database) {
//                    expected, there is a problem with it
                } else {
                    throw e;
                }
            }
        }

        connectionsByUrl.put(url, databaseConnection);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                shutdownConnection((JdbcConnection) databaseConnection);
            }
        }));

        return databaseConnection;
    }

    /**
     * Ensures that the next attempt to call openConnection for the given JDBC URL returns a fresh connection.
     *
     * @param url The JDBC connection URL to remove from the cache pool.
     */
    public void closeConnection(String url) {
        JdbcConnection conn = (JdbcConnection) connectionsByUrl.get(url);
        if (conn != null) {
            shutdownConnection(conn);
            connectionsByUrl.remove(url);
            connectionsAttempted.remove(url);
        }
    }

    public DatabaseConnection openDatabaseConnection(String url,
                                                     String username, String password) throws Exception {

        JUnitJDBCDriverClassLoader jdbcDriverLoader = JUnitJDBCDriverClassLoader.getInstance();
        final Driver driver;
        try {
            driver = (Driver) Class.forName(DatabaseFactory.getInstance().findDefaultDriver(url), true, jdbcDriverLoader).getConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("Could not connect to " + url + ": Will not test against.  " + e.getMessage());
            return null; //could not connect
        }

        Properties info = new Properties();
        info.put("user", username);
        if (password != null) {
            info.put("password", password);
        }
        info.put("retrieveMessagesFromServerOnGetMessage", "true"); //for db2


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

    public DatabaseTestURL[] getTestUrls() {
        return DEFAULT_TEST_DATABASES;
    }

    public Set<Database> getAllDatabases() {
        if (allDatabases == null) {
            allDatabases = new HashSet<Database>();

            allDatabases.addAll(DatabaseFactory.getInstance().getImplementedDatabases());

            List<Database> toRemove = new ArrayList<Database>();
            for (Database database : allDatabases) {
                if ((database instanceof SQLiteDatabase) //todo: re-enable sqlite testing
                        || (database instanceof MockDatabase) || (database instanceof ExampleCustomDatabase)) {
                    toRemove.add(database);
                }
            }
            allDatabases.removeAll(toRemove);
        }
        return allDatabases;
    }

    public Set<Database> getAvailableDatabases() throws Exception {
        if (availableDatabases.isEmpty()) {
            for (DatabaseConnection conn : getAvailableConnections()) {
                availableDatabases.add(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
            }
        }
        //Check to don't return closed databases
        Iterator<Database> iter = availableDatabases.iterator();
        while (iter.hasNext()) {
            Database database = iter.next();
            if (database.getConnection().isClosed())
                iter.remove();
        }


        return availableDatabases;
    }


    public Set<DatabaseConnection> getAvailableConnections() throws Exception {
        if (availableConnections == null) {
            availableConnections = new HashSet<DatabaseConnection>();
            for (DatabaseTestURL url : getTestUrls()) {
                DatabaseConnection connection = openConnection(url.getUrl(), url.getUsername(), url.getPassword());

                if (connection != null) {
                    availableConnections.add(connection);
                }
            }
        }

        //Check to don't return closed connections
        Iterator<DatabaseConnection> iter = availableConnections.iterator();
        while (iter.hasNext()) {
            DatabaseConnection connection = iter.next();
            if (connection.isClosed())
                iter.remove();
        }

        return availableConnections;
    }

    public DatabaseConnection getConnection(String url, String username, String password) throws Exception {
        return openConnection(url, username, password);
    }

    public String getTestUrl(Database database) throws Exception {
        for (DatabaseTestURL turl : getTestUrls()) {
            String url = turl.getUrl();
            if (database.getDefaultDriver(url) != null) {
                return url;
            }
        }
        throw new RuntimeException("Could not find url for " + database);
    }
}
