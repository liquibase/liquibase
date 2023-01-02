package liquibase.hub;

import liquibase.Scope;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Configuration container for global properties.
 */
public class HubConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<String> LIQUIBASE_HUB_API_KEY;
    public static final ConfigurationDefinition<String> LIQUIBASE_HUB_URL;
    public static final ConfigurationDefinition<HubMode> LIQUIBASE_HUB_MODE;
    public static final ConfigurationDefinition<Level> LIQUIBASE_HUB_LOGLEVEL;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.hub");

        LIQUIBASE_HUB_API_KEY = builder.define("apiKey", String.class)
                .setDescription("Liquibase Hub API key for operations")
                .setValueObfuscator(value -> {
                    if (value == null) {
                        return null;
                    }
                    if (value.length() < 5) {
                        return value;
                    }
                    return value.substring(0, 4) + "****";
                })
                .build();

        LIQUIBASE_HUB_URL = builder.define("url", String.class)
                .setDescription("Liquibase Hub URL for operations")
                .setDefaultValue("https://hub.liquibase.com")
                .setValueHandler(value -> {
                    if (value == null) {
                        return null;
                    }
                    return value.toString().replaceFirst("(https?://[^/]+).*", "$1");
                })
                .build();
        LIQUIBASE_HUB_MODE = builder.define("mode", HubMode.class)
                .setDescription("Content to send to Liquibase Hub during operations. Values can be 'all', 'meta', or 'off'")
                .setDefaultValue(HubMode.ALL)
                .build();

        LIQUIBASE_HUB_LOGLEVEL = builder.define("logLevel", Level.class)
                .setDescription("Log level for filtering log messages to send to Liquibase Hub during operations. Values can be any acceptable log level.")
                .setDefaultValue(Level.INFO)
                .setValueHandler(value -> {
                    if (value == null) {
                        return null;
                    }

                    if (value instanceof String) {
                        final List<String> validValues = Arrays.asList("OFF", "FINE", "WARN", "ERROR", "INFO");
                        if (!validValues.contains(((String) value).toUpperCase())) {
                            Level logLevel = Level.INFO;
                            try {
                                logLevel = Level.parse(((String) value).toUpperCase());
                            } catch (IllegalArgumentException e) {
                                String message = "An invalid liquibase.hub.logLevel value of " + value + " detected. Acceptable values are " + StringUtil.join(validValues, ",");
                                Scope.getCurrentScope().getLog(liquibase.hub.HubConfiguration.class).warning(message);
                            }
                            value = logLevel.toString();
                        }

                    }

                    return Level.parse(value.toString());
                })
                .build();
    }

    public enum HubMode {
        OFF,
        META,
        ALL,
    }
}
