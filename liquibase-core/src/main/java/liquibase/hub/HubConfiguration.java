package liquibase.hub;

import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.ConfigurationDefinitionHolder;

/**
 * Configuration container for global properties.
 */
public class HubConfiguration implements ConfigurationDefinitionHolder {

    public static final ConfigurationDefinition<String> LIQUIBASE_HUB_API_KEY;
    public static final ConfigurationDefinition<String> LIQUIBASE_HUB_URL;
    public static final ConfigurationDefinition<String> LIQUIBASE_HUB_MODE;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.hub");

        LIQUIBASE_HUB_API_KEY = builder.define("apiKey", String.class)
                .setDescription("Liquibase Hub API key for operations")
                .setValueObfuscator(value -> {
                    if (value == null) {
                        return null;
                    }
                    return value.substring(0, 6) + "************";

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
        LIQUIBASE_HUB_MODE = builder.define("mode", String.class)
                .setDescription("Content to send to Liquibase Hub during operations. Values can be 'all', 'meta', or 'off'")
                .setDefaultValue("all")
                .build();
    }
}
