package liquibase.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to read database configuration from liquibase.sdk.local.yaml
 * and provide database connections without requiring environment variables.
 * 
 * This replaces the need for SNOWFLAKE_URL, SNOWFLAKE_USER, SNOWFLAKE_PASSWORD
 * environment variables by reading from the standard configuration file.
 */
public class TestDatabaseConfigUtil {

    private static final String CONFIG_FILE = "/liquibase.sdk.local.yaml";
    private static Map<String, String> configCache = null;

    /**
     * Reads the configuration from liquibase.sdk.local.yaml file
     */
    public static Map<String, String> getSnowflakeConfig() {
        if (configCache != null) {
            return configCache;
        }

        configCache = new HashMap<>();
        
        try (InputStream is = TestDatabaseConfigUtil.class.getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new RuntimeException("Configuration file not found: " + CONFIG_FILE + 
                    ". Please ensure liquibase.sdk.local.yaml exists in src/test/resources/");
            }

            Scanner scanner = new Scanner(is);
            boolean inSnowflakeSection = false;
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip comments and empty lines
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                
                // Look for snowflake section
                if (line.equals("snowflake:")) {
                    inSnowflakeSection = true;
                    continue;
                }
                
                // Exit snowflake section if we hit another section at same level
                if (inSnowflakeSection && line.endsWith(":") && !line.startsWith(" ")) {
                    break;
                }
                
                // Parse key-value pairs in snowflake section
                if (inSnowflakeSection && line.contains(":")) {
                    Pattern pattern = Pattern.compile("^\\s*(\\w+):\\s*\"?(.+?)\"?\\s*$");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);
                        configCache.put(key, value);
                    }
                }
            }
            scanner.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read configuration file: " + CONFIG_FILE, e);
        }

        // Validate required configuration
        String[] requiredKeys = {"url", "username", "password"};
        for (String key : requiredKeys) {
            if (!configCache.containsKey(key) || configCache.get(key).isEmpty()) {
                throw new RuntimeException("Missing required configuration key: " + key + 
                    " in " + CONFIG_FILE);
            }
        }

        return configCache;
    }

    /**
     * Gets a database connection using the configuration from liquibase.sdk.local.yaml
     */
    public static Connection getSnowflakeConnection() throws Exception {
        Map<String, String> config = getSnowflakeConfig();
        String url = config.get("url");
        String username = config.get("username");
        String password = config.get("password");
        
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Gets the database URL from configuration
     */
    public static String getSnowflakeUrl() {
        return getSnowflakeConfig().get("url");
    }

    /**
     * Gets the username from configuration
     */
    public static String getSnowflakeUsername() {
        return getSnowflakeConfig().get("username");
    }

    /**
     * Gets the password from configuration
     */
    public static String getSnowflakePassword() {
        return getSnowflakeConfig().get("password");
    }

    /**
     * Gets the catalog/database from configuration
     */
    public static String getSnowflakeCatalog() {
        return getSnowflakeConfig().get("catalog");
    }

    /**
     * Gets the schema from configuration
     */
    public static String getSnowflakeSchema() {
        return getSnowflakeConfig().get("schema");
    }

    /**
     * Gets the altSchema from configuration
     */
    public static String getSnowflakeAltSchema() {
        return getSnowflakeConfig().get("altSchema");
    }
}