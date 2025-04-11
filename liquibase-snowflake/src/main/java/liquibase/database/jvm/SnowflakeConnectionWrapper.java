package liquibase.database.jvm;

import liquibase.exception.DatabaseException;

import java.sql.Driver;
import java.util.Properties;

/**
 * A connection wrapper for Snowflake that captures the original URL with parameters
 * and handles OAuth authentication appropriately.
 * <p>
 * This wrapper is registered as a high-priority DatabaseConnection implementation
 * to intercept Snowflake connections before they are processed by the standard
 * JdbcConnection.
 */
public class SnowflakeConnectionWrapper extends JdbcConnection {

    private boolean isOAuth = false;

    public SnowflakeConnectionWrapper() {
        super();
    }

    @Override
    public boolean supports(String url) {
        return url != null && url.startsWith("jdbc:snowflake:");
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 100;
    }

    @Override
    public void open(String url, Driver driverObject, Properties driverProperties) throws DatabaseException {
        this.isOAuth = url.contains("authenticator=oauth");
        super.open(url, driverObject, driverProperties);
    }
    
    @Override
    public String getConnectionUserName() {
        String standardUsername = super.getConnectionUserName();

        if (this.isOAuth && standardUsername == null) {
            return "oauth-authenticated-user";
        }

        return standardUsername;
    }
}