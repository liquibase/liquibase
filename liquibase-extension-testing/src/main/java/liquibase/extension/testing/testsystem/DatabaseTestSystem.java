package liquibase.extension.testing.testsystem;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.logging.Logger;
import liquibase.util.CollectionUtil;
import liquibase.util.ObjectUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public abstract class DatabaseTestSystem extends TestSystem {

    protected DatabaseWrapper wrapper;

    private Map<String, Connection> connections = new HashMap<>();
    private final String shortName;

    public DatabaseTestSystem(String shortName) {
        this.shortName = shortName;
    }


    @Override
    public String getDefinition() {
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

    protected Connection openSetupConnection() throws SQLException {
        return DriverManager.getConnection(wrapper.getUrl(), getSetupUsername(), getSetupPassword());
    }

    public Connection openConnection() throws SQLException {
        return openConnection(getUsername(), getPassword());
    }

    public Connection openConnection(String username, String password) throws SQLException {
        final String key = username + ":" + password;
        Connection connection = connections.get(key);
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(getUrl(), username, password);
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
        return getTestSystemProperty("version", String.class, true);
    }

    protected String getImageName() {
        return getTestSystemProperty("imageName", String.class, true);
    }

    protected void setup() throws SQLException {
        final Logger log = Scope.getCurrentScope().getLog(getClass());
        try (final Connection connection = openSetupConnection();
             final Statement statement = connection.createStatement()) {
            for (String sql : CollectionUtil.createIfNull(getSetupSql())) {
                log.info("Running setup SQL: "+sql);
                statement.execute(sql);
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
    }

    protected abstract String[] getSetupSql();


}
