package liquibase.extension.testing.testsystem;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.util.DownloadUtil;
import liquibase.logging.Logger;
import liquibase.util.CollectionUtil;
import liquibase.util.ObjectUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class DatabaseTestSystem extends TestSystem {

    protected DatabaseWrapper wrapper;

    private Map<String, Connection> connections = new HashMap<>();
    private final String shortName;

    public DatabaseTestSystem(String shortName) {
        this.shortName = shortName;
    }


    @Override
    public String getName() {
        return shortName;
    }

    @Override
    public int getPriority(String definition) {
        if (definition.equals(shortName)) {
            return PRIORITY_DEFAULT;
        }

        return PRIORITY_NOT_APPLICABLE;
    }

    @NotNull
    protected abstract DatabaseWrapper createWrapper() throws Exception;

    @Override
    public void start(boolean keepRunning) throws Exception {
        if (wrapper != null) {
            return;
        }

        wrapper = createWrapper();

        wrapper.start(keepRunning);

        setup();
    }


    @Override
    public void stop() throws Exception {
        if (wrapper == null) {
            wrapper = createWrapper();
        }
        wrapper.stop();
        System.out.println(wrapper);
    }

    public String getDriver() {
        return getTestSystemProperty("driver", String.class);
    }

    protected Connection openConnection(String url, String username, String password) throws SQLException {
        try {
            String driverConfig = getDriver();
            Driver driver;

            if (driverConfig == null) {
                driver = DriverManager.getDriver(getUrl());
            } else {
                Path driverPath = DownloadUtil.downloadMavenArtifact(driverConfig);
                final URLClassLoader isolatedClassloader = new URLClassLoader(new URL[]{
                        driverPath.toUri().toURL(),
                }, null);

                final Class<?> isolatedDriverManager = Class.forName(DriverManager.class.getName(), true, isolatedClassloader);
                final Method getDriverMethod = isolatedDriverManager.getMethod("getDriver", String.class);

                final Driver driverClass = (Driver) getDriverMethod.invoke(null, url);
                driver = (Driver) Class.forName(driverClass.getClass().getName(), true, isolatedClassloader).newInstance();
            }

            Properties properties = new Properties();
            properties.put("user", username);
            properties.put("password", password);
            return driver.connect(url, properties);
        } catch (ReflectiveOperationException | MalformedURLException e) {
            throw new UnexpectedLiquibaseException(e.getMessage(), e);
        }
    }

    protected Connection openSetupConnection() throws SQLException {
        return openConnection(wrapper.getUrl(), getSetupUsername(), getSetupPassword());
    }

    public Connection openConnection() throws SQLException {
        return openConnection(getUsername(), getPassword());
    }

    public Connection openConnection(String username, String password) throws SQLException {
        final String key = username + ":" + password;
        Connection connection = connections.get(key);
        if (connection == null || connection.isClosed()) {
            connection = openConnection(getUrl(), username, password);
            connections.put(key, connection);
        }
        return connection;
    }

    public String getUrl() {
        return wrapper.getUrl();
    }

    public String getUsername() {
        return getTestSystemProperty("username", String.class);
    }

    public String getPassword() {
        return getTestSystemProperty("password", String.class);
    }

    public String getCatalog() {
        return getTestSystemProperty("catalog", String.class);
    }

    public String getAltCatalog() {
        return getTestSystemProperty("altCatalog", String.class);
    }

    public String getAltSchema() {
        return getTestSystemProperty("altSchema", String.class);
    }

    public String getAltTablespace() {
        return getTestSystemProperty("altTablespace", String.class);
    }

    protected String getSetupUsername() {
        return ObjectUtil.defaultIfNull(getTestSystemProperty("setup.username", String.class), getUsername());
    }

    protected String getSetupPassword() {
        return ObjectUtil.defaultIfNull(getTestSystemProperty("setup.password", String.class), getPassword());
    }

    protected String getVersion() {
        return getTestSystemProperty("version", String.class);
    }

    protected String getImageName() {
        return getTestSystemProperty("imageName", String.class);
    }

    protected void setup() throws SQLException {
        final Logger log = Scope.getCurrentScope().getLog(getClass());
        try (final Connection connection = openSetupConnection();
             final Statement statement = connection.createStatement()) {
            for (String sql : CollectionUtil.createIfNull(getSetupSql())) {
                log.info("Running setup SQL: " + sql);
                statement.execute(sql);
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
    }

    protected abstract String[] getSetupSql();


}
