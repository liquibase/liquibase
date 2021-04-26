package liquibase.extension.testing;

import liquibase.exception.LiquibaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestDatabaseConnections {

    private Map<String, ConnectionStatus> openConnections = new HashMap<>();

    private static TestDatabaseConnections instance;

    public static TestDatabaseConnections getInstance() {
        if (instance == null) {
            instance = new TestDatabaseConnections();
        }

        return instance;
    }

    private TestDatabaseConnections() {
    }

    public ConnectionStatus getConnection(String shortName) throws IOException, LiquibaseException {
        if (!openConnections.containsKey(shortName)) {
            // Get the integration test properties for both global settings and (if applicable) local overrides.
            Properties integrationTestProperties;
            integrationTestProperties = new Properties();
            integrationTestProperties.load(
                    Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("liquibase/liquibase.integrationtest.properties"));
            InputStream localProperties =
                    Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("liquibase/liquibase.integrationtest.local.properties");
            if (localProperties != null)
                integrationTestProperties.load(localProperties);

            // Login username
            String username = integrationTestProperties.getProperty("integration.test." + shortName + ".username");
            if (username == null)
                username = integrationTestProperties.getProperty("integration.test.username");

            // Login password
            String password = integrationTestProperties.getProperty("integration.test." + shortName + ".password");
            if (password == null)
                password = integrationTestProperties.getProperty("integration.test.password");

            // JDBC URL (no global default so all databases!)
            String url = integrationTestProperties.getProperty("integration.test." + shortName + ".url");
            if ((url == null) || ((url.length()) == 0)) {
                this.openConnections.put(shortName, new ConnectionStatus("No JDBC URL found for integration test of database type " + shortName));
                return openConnections.get(shortName);
            }


            Properties info = new Properties();
            info.put("user", username);
            if (password != null) {
                info.put("password", password);
            }
            info.put("retrieveMessagesFromServerOnGetMessage", "true"); //for db2


            Connection connection;
            try {
                connection = DriverManager.getConnection(url, info);
            } catch (SQLException e) {
                this.openConnections.put(shortName, new ConnectionStatus("Could not connect to " + url + ": Will not test against.  " + e.getMessage()));
                return openConnections.get(shortName);
            }
            if (connection == null) {
                this.openConnections.put(shortName, new ConnectionStatus("Connection could not be created to " + url + ".  Possibly no driver set up?"));
                return openConnections.get(shortName);
            }

            this.openConnections.put(shortName, new ConnectionStatus(connection, url, username, password));
        }

        return openConnections.get(shortName);
    }

    public static class ConnectionStatus {

        public Connection connection;
        public String errorMessage;
        public String url;
        public String username;
        public String password;

        public ConnectionStatus(Connection connection, String url, String username, String password) {
            this.connection = connection;
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public ConnectionStatus(String errorMessage) {
            this.errorMessage = errorMessage;
        }

    }

}
