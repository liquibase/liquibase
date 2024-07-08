package liquibase.extension.testing.testsystem;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.JdbcDatabaseWrapper;
import liquibase.extension.testing.util.DownloadUtil;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import liquibase.util.CollectionUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

/**
 * Base class for {@link TestSystem}s for databases.
 */
public abstract class DatabaseTestSystem extends TestSystem {

    private final SortedSet<String> configurationKeys = new TreeSet<>(Arrays.asList("username", "password", "setup.username", "setup.password", "driverJar",
            "catalog", "altCatalog", "schema", "altSchema", "altTablespace", "version", "imageName", "url"));

    protected DatabaseWrapper wrapper;

    private final Map<String, Connection> connections = new HashMap<>();

    public DatabaseTestSystem(String shortName) {
        super(shortName);
    }

    public DatabaseTestSystem(Definition definition) {
        super(definition);
    }

    @NotNull
    protected DatabaseWrapper createWrapper() throws Exception {
        String url = getConfiguredValue("url", String.class);
        String imageName = getImageName();

        if (url == null && imageName == null) {
            throw new IllegalArgumentException("Either url or imageName must be configured for " + getDefinition());
        }
        if (url != null && imageName != null) {
            throw new IllegalArgumentException("Only url OR imageName must be configured for " + getDefinition() + ". Not both.");
        }

        final Logger log = Scope.getCurrentScope().getLog(getClass());
        if (url != null) {
            log.fine("Creating JDBC wrapper for " + url);
            return createJdbcWrapper(url);
        }

        return createContainerWrapper();
    }

    @NotNull
    protected JdbcDatabaseWrapper createJdbcWrapper(String url) throws SQLException {
        return new JdbcDatabaseWrapper(url, getUsername(), getPassword());
    }

    protected abstract DatabaseWrapper createContainerWrapper() throws Exception;

    /**
     * Default implementation uses {@link #createWrapper()} to manage the external system.
     * Multiple calls to start() will be no-ops.
     * Calls {@link #setup()} after starting the wrapper.
     */
    @Override
    public void start() throws Exception {
        if (wrapper != null) {
            return;
        }

        wrapper = createWrapper();

        Scope.getCurrentScope().getUI().sendMessage("Starting database '" + this.getDefinition() + "'");

        wrapper.start();

        Scope.getCurrentScope().getLog(getClass()).info("Configuring database '" + this.getDefinition() + "'");
        setup();

        Scope.getCurrentScope().getUI().sendMessage("Database '" + getDefinition() + "' details:\n" + StringUtil.indent(wrapper.describe()));

        Scope.getCurrentScope().getUI().sendMessage("Database '" + getDefinition() + "' connection information:\n" +
                "    url: " + this.getConnectionUrl() + "\n" +
                "    username: " + this.getUsername() + "\n" +
                "    password: " + this.getPassword() + "\n"
        );
    }

    /**
     * Default implementation uses {@link #createWrapper()} to manage the external system, and calls {@link DatabaseWrapper#stop()}
     */
    @Override
    public void stop() throws Exception {
        if (wrapper == null) {
            wrapper = createWrapper();
        }

        Scope.getCurrentScope().getUI().sendMessage("Stopping database wrapper: " + wrapper);
        Scope.getCurrentScope().getLog(getClass()).info("Stopping database wrapper: " + wrapper);
        wrapper.stop();
        Scope.getCurrentScope().getLog(getClass()).info("Stopped database wrapper: " + wrapper);
    }

    /**
     * Returns the driver library to use. Supports maven-style coordinate, like "com.h2database:h2:1.4.200".
     * Default implementation uses the "driverJar" testSystem configuration value.
     */
    public String getDriverJar() {
        return getConfiguredValue("driverJar", String.class);
    }

    /**
     * Opens a connection to the given url, username, and password. This is not an end-user facing function because the url to use should be
     * managed by the DatabaseWrapper, not the user.
     */
    protected Connection getConnection(String url, String username, String password) throws SQLException {
        Driver driver = getDriver(url);

        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        return driver.connect(url, properties);
    }

    /**
     * @return the Driver instance to use for the given url.
     * Default implementation uses the value in {@link #getDriverJar()} as needed and will download the library and manage the classloader as needed.
     */
    protected Driver getDriver(String url) throws SQLException {
        try {
            Scope.getCurrentScope().getLog(getClass()).fine("Loading driver for " + url);
            String driverJar = getDriverJar();

            if (driverJar == null) {
                return this.getDriverFromUrl(url);
            } else {
                Scope.getCurrentScope().getLog(getClass()).fine("Using driver from " + driverJar);
                Path driverPath = DownloadUtil.downloadMavenArtifact(driverJar);
                //
                // NOTE:
                // This call to construct the URLClassLoader is problematic in certain instances
                // It can cause the Class.forName call below to fail to find the DriverManager class
                // Removing the parent classloader argument of null seems to fix the issues, but we really need
                // to investigate more, so I am leaving the code the way it is.  If you do not define the
                // driverJar property in the configuration file, you will not hit this issue.
                //
                final URLClassLoader isolatedClassloader = new URLClassLoader(new URL[]{
                        driverPath.toUri().toURL(),
                }, this.getClass().getClassLoader());

                final Class<?> isolatedDriverManager = Class.forName(DriverManager.class.getName(), true, isolatedClassloader);
                final Method getDriverMethod = isolatedDriverManager.getMethod("getDriver", String.class);

                final Driver driverClass = (Driver) getDriverMethod.invoke(null, url);
                return (Driver) Class.forName(driverClass.getClass().getName(), true, isolatedClassloader).newInstance();
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private Driver getDriverFromUrl(String url) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Scope.getCurrentScope().getLog(getClass()).fine("Using driver from standard classloader");
        try {
            return DriverManager.getDriver(url);
        } catch (SQLException e) {
            Scope.getCurrentScope().getLog(getClass()).fine(String.format("Error '%s' while loading driver for url '%s', last try.", e.getMessage(), url));
            String driverClass = DatabaseFactory.getInstance().findDefaultDriver(url);
            return (Driver) Class.forName(driverClass).newInstance();
        }
    }

    /**
     * Opens a connection with valid permissions for the {@link #setup()} logic.
     */
    protected Connection openSetupConnection() throws SQLException {
        return getConnection(wrapper.getUrl(), getSetupUsername(), getSetupPassword());
    }

    /**
     * Returns the connection to this database. Will reuse a single connection rather than continually open new ones.
     * Convenience method for {@link #getConnection(String, String)} using {@link #getUsername()} and {@link #getPassword()}
     */
    public Connection getConnection() throws SQLException {
        return getConnection(getUsername(), getPassword());
    }

    /**
     * Returns the connection to this database. Will reuse a single connection for each username/password combo rather than continually open new ones.
     */
    public Connection getConnection(String username, String password) throws SQLException {
        final String key = username + ":" + password;

        Connection connection = connections.get(key);
        if (connection == null || connection.isClosed()) {
            connection = getConnection(getConnectionUrl(), username, password);
            connections.put(key, connection);
        }
        return connection;
    }

    /**
     * Return the url used to connect to this database.
     * NOTE: this may be different than the 'url' configured value because the TestSystem implementations are free to tweak and control this URL based on other settings.
     */
    public String getConnectionUrl() {
        return wrapper.getUrl();
    }

    /**
     * Standard username to use when connecting. Returns "username" test system configuration.
     */
    public String getUsername() {
        return getConfiguredValue("username", String.class);
    }

    /**
     * Standard password to use when connecting. Returns "password" test system configuration.
     */
    public String getPassword() {
        return getConfiguredValue("password", String.class);
    }

    /**
     * Standard "catalog" to use for testing. Returns "catalog" test system configuration.
     */
    public String getCatalog() {
        return getConfiguredValue("catalog", String.class);
    }

    /**
     * Standard alt catalog to use for testing. Returns "altCatalog" test system configuration.
     */
    public String getAltCatalog() {
        return getConfiguredValue("altCatalog", String.class);
    }

    /**
     * Standard alt schema to use for testing. Returns "altSchema" test system configuration.
     */
    public String getAltSchema() {
        return getConfiguredValue("altSchema", String.class);
    }

    /**
     * Standard alt tablespace to use for testing. Returns "username" test system configuration.
     */
    public String getAltTablespace() {
        return getConfiguredValue("altTablespace", String.class);
    }

    /**
     * "Privileged" username to use for {@link #setup()}. Returns "setup.username" or "username" test system configuration.
     */
    protected String getSetupUsername() {
        return ObjectUtil.defaultIfNull(getConfiguredValue("setup.username", String.class), getUsername());
    }

    /**
     * "Privileged" password to use for {@link #setup()}. Returns "setup.password" or "password" test system configuration.
     */
    protected String getSetupPassword() {
        return ObjectUtil.defaultIfNull(getConfiguredValue("setup.password", String.class), getPassword());
    }

    /**
     * Version of the database to test against."Privileged" username to use for {@link #setup()}. Returns "version" test system configuration.
     */
    protected String getVersion() {
        return getConfiguredValue("version", String.class);
    }

    /**
     * Docker image of the database to test against. Returns "imageName" test system configuration.
     */
    protected String getImageName() {
        return getConfiguredValue("imageName", String.class);
    }

    /**
     * Sets up any needed catalogs/schemas/usernames/etc.
     */
    protected void setup() throws SQLException {
        final Logger log = Scope.getCurrentScope().getLog(getClass());
        try (final Connection connection = openSetupConnection();
             final Statement statement = connection.createStatement()) {
            for (String sql : CollectionUtil.createIfNull(getSetupSql())) {
                log.info("Running setup SQL: " + sql);
                try {
                    statement.execute(sql);
                } catch (SQLException e) {
                    log.info("Error running setup SQL " + sql + ": " + e.getMessage() + ". Continuing on");
                    log.fine(e.getMessage(), e);

                    if (!connection.getAutoCommit()) {
                        connection.rollback();
                    }
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
    }

    /**
     * Define SQL to run by {@link #setup()}
     */
    protected abstract String[] getSetupSql();

    public boolean executeSql(String sql) throws SQLException {
        Connection connection = getConnection();
        try (Statement statement = connection.createStatement()) {
            boolean execute = statement.execute(sql);
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return execute;
        }
    }

    public void execute(SqlStatement sqlStatement) throws SQLException, DatabaseException {
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabaseFromFactory()).execute(sqlStatement);
    }

    public Database getDatabaseFromFactory() throws SQLException, DatabaseException {
        DatabaseConnection connection = new JdbcConnection(getConnection());
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
    }

    public void execute(Change change) throws SQLException, DatabaseException {
        Database database = getDatabaseFromFactory();
        SqlStatement[] statements = change.generateStatements(database);
        for (SqlStatement statement : statements) {
            execute(statement);
        }
    }

    public void executeInverses(Change change) throws SQLException, DatabaseException, RollbackImpossibleException {
        Database database = getDatabaseFromFactory();
        SqlStatement[] statements = change.generateRollbackStatements(database);
        for (SqlStatement statement : statements) {
            execute(statement);
        }
    }
}
