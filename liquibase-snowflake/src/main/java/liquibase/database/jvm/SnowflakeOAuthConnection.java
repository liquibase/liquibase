package liquibase.database.jvm;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A JDBC connection wrapper for Snowflake that specifically handles OAuth authentication.
 * 
 * This class addresses the issue where JDBC connection metadata returns null for username when
 * using the OAuth authenticator with token in the URL, resulting in "null@jdbc:snowflake:..." in logs.
 */
public class SnowflakeOAuthConnection extends JdbcConnection {
    
    private static final Pattern AUTH_PATTERN = Pattern.compile("authenticator=(\\w+)");
    private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("client_id=([^&;]+)");
    
    public SnowflakeOAuthConnection(Connection connection) {
        super(connection);
    }
    
    @Override
    public String getConnectionUserName() {
        String standardUsername = super.getConnectionUserName();

        if (standardUsername != null && !standardUsername.trim().isEmpty()) {
            return standardUsername;
        }
        
        // Otherwise, try to determine the authentication method from the URL
        String url = getURL();
        String authenticator = extractAuthenticator(url);
        
        if ("oauth".equalsIgnoreCase(authenticator)) {
            String clientId = extractClientId(url);
            return clientId != null ? clientId : "oauth-authenticated-user";
        } else if (authenticator != null) {
            return authenticator + "-authenticated-user";
        }
        
        return "snowflake-user";
    }

    private String extractAuthenticator(String url) {
        if (url == null) {
            return null;
        }
        
        Matcher matcher = AUTH_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    private String extractClientId(String url) {
        if (url == null) {
            return null;
        }
        
        Matcher matcher = CLIENT_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
}