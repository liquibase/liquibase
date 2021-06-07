package liquibase.extension.testing;

import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtil;

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
            if (localProperties != null) {
                integrationTestProperties.load(localProperties);
            }

            String username = getCascadingProperty(integrationTestProperties, "integration.test." + shortName + ".username", "integration.test.username");
            String password = getCascadingProperty(integrationTestProperties, "integration.test." + shortName + ".password", "integration.test.password");

            // JDBC URL (no global default so all databases!)
            String url = getCascadingProperty(integrationTestProperties, "integration.test." + shortName + ".url");
            if (url == null) {
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

            String altUrl = getCascadingProperty(integrationTestProperties, "integration.test." + shortName + ".alt.url", "integration.test." + shortName + ".url");
            String altUsername = getCascadingProperty(integrationTestProperties, "integration.test." + shortName + ".alt.username", "integration.test." + shortName + ".username", "integration.test.username");
            String altPassword = getCascadingProperty(integrationTestProperties, "integration.test." + shortName + ".alt.username", "integration.test." + shortName + ".password", "integration.test.password");

            Connection altConnection = null;
            if (StringUtil.equalsIgnoreCaseAndEmpty(url, altUrl) && StringUtil.equalsIgnoreCaseAndEmpty(username, altUsername)) {
                System.out.println("No alt url and/or username defined for " + shortName);
            } else {
                Properties altInfo = new Properties();
                altInfo.put("user", altUsername);
                if (password != null) {
                    altInfo.put("password", altPassword);
                }
                altInfo.put("retrieveMessagesFromServerOnGetMessage", "true"); //for db2

                try {
                    altConnection = DriverManager.getConnection(altUrl, altInfo);
                } catch (SQLException throwables) {
                    System.out.println("Cannot connect to alt url " + altUrl + ": " + url);
                }
            }


            this.openConnections.put(shortName, new ConnectionStatus(connection, url, username, password, altConnection, altUrl, altUsername, altPassword));
        }

        return openConnections.get(shortName);
    }

    private String getCascadingProperty(Properties properties, String... propertyNames) {
        for (String property : propertyNames) {
            String value = (String) properties.get(property);
            if (value != null) {
                return value.trim();
            }
        }

        return null;
    }

    public static class ConnectionStatus {

        public Connection connection;
        public String errorMessage;
        public String url;
        public String username;
        public String password;

        public Connection altConnection;
        public String altUrl;
        public String altUsername;
        public String altPassword;

        public ConnectionStatus(Connection connection, String url, String username, String password, Connection altConnection, String altUrl, String altUsername, String altPassword) {
            this.connection = connection;
            this.url = url;
            this.username = username;
            this.password = password;

            this.altConnection = altConnection;
            this.altUrl = altUrl;
            this.altUsername = altUsername;
            this.altPassword = altPassword;
        }

        public ConnectionStatus(String errorMessage) {
            this.errorMessage = errorMessage;
        }

    }

}
