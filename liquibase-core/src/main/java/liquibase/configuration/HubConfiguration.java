package liquibase.configuration;

/**
 * Configuration container for global properties.
 */
public class HubConfiguration extends AbstractConfigurationContainer {

    public static final String LIQUIBASE_HUB_API_KEY="apiKey";

    public HubConfiguration() {
        super("liquibase.hub");

        getContainer().addProperty(LIQUIBASE_HUB_API_KEY, String.class)
                      .setDescription("Liquibase Hub API key for operations");
    }

    @Override
    public void setValue(String propertyName, Object value) {
        super.setValue(propertyName, value);
    }

    public String getLiquibaseHubApiKey() {
        return getContainer().getValue(LIQUIBASE_HUB_API_KEY, String.class);
    }

    public HubConfiguration setLiquibaseHubApiKey(String liquibaseHubApiKey) {
        getContainer().setValue(LIQUIBASE_HUB_API_KEY, liquibaseHubApiKey);
        return this;
    }
}
