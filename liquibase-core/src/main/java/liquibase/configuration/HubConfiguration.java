package liquibase.configuration;

import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration container for global properties.
 */
public class HubConfiguration extends AbstractConfigurationContainer {

    public static final String LIQUIBASE_HUB_API_KEY = "apiKey";
    public static final String LIQUIBASE_HUB_URL = "url";
    public static final String LIQUIBASE_HUB_MODE = "mode";

    public HubConfiguration() {
        super("liquibase.hub");

        getContainer().addProperty(LIQUIBASE_HUB_API_KEY, String.class)
                .setDescription("Liquibase Hub API key for operations");
        getContainer().addProperty(LIQUIBASE_HUB_URL, String.class)
                .setDescription("Liquibase Hub URL for operations")
                .setValueHandler(value -> {
                    if (value == null) {
                        return null;
                    }
                    return value.toString().replaceFirst("(https?://[^/]+).*", "$1");
                });
        getContainer().addProperty(LIQUIBASE_HUB_MODE, String.class)
                .setDescription("Content to send to Liquibase Hub during operations. Values can be 'all', 'meta', or 'off'")
                .setDefaultValue("all");
    }

    @Override
    public void setValue(String propertyName, Object value) {
        super.setValue(propertyName, value);
    }

    /**
     * Output {@link #getLiquibaseHubApiKey()} but in a way that is secure for message output.
     *
     */
    public String getLiquibaseHubApiKeySecureDescription() {
        final String key = getContainer().getValue(LIQUIBASE_HUB_API_KEY, String.class);
        if (key == null) {
            return null;
        }
        return key.substring(0,6) + "************";
    }

    public String getLiquibaseHubApiKey() {
        return getContainer().getValue(LIQUIBASE_HUB_API_KEY, String.class);
    }

    public HubConfiguration setLiquibaseHubApiKey(String liquibaseHubApiKey) {
        getContainer().setValue(LIQUIBASE_HUB_API_KEY, liquibaseHubApiKey);
        return this;
    }

    public HubConfiguration setLiquibaseHubUrl(String liquibaseHubUrl) {
        getContainer().setValue(LIQUIBASE_HUB_URL, liquibaseHubUrl);
        return this;
    }

    public String getLiquibaseHubUrl() {
        String hubUrl = getContainer().getValue(LIQUIBASE_HUB_URL, String.class);
        if (hubUrl == null || hubUrl.isEmpty()) {
            return "https://hub.liquibase.com";
        }
        return hubUrl;
    }

    public HubConfiguration setLiquibaseHubMode(String liquibaseHubMode) {
        getContainer().setValue(LIQUIBASE_HUB_MODE, liquibaseHubMode);
        return this;
    }

    public String getLiquibaseHubMode() {
        final String value = getContainer().getValue(LIQUIBASE_HUB_MODE, String.class);

        final List<String> validValues = Arrays.asList("off", "meta", "all");
        if (!validValues.contains(value.toLowerCase())) {
            throw new RuntimeException(" An invalid liquibase.hub.mode value of "+value+" detected. Acceptable values are "+StringUtil.join(validValues, ", "));
        }
        return value;
    }
}
