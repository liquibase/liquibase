package liquibase.configuration;

/**
 * Describes a value found from a provider. This is the most basic level at which a configuration value is defined.
 */
public class ProvidedValue {
    private final String requestedKey;
    private final String actualKey;
    private final String sourceDescription;
    private final ConfigurationValueProvider provider;
    private final Object value;

    public ProvidedValue(String requestedKey, String actualKey, Object value, String sourceDescription, ConfigurationValueProvider provider) {
        this.requestedKey = requestedKey;
        this.actualKey = actualKey;
        this.value = value;
        this.sourceDescription = sourceDescription;
        this.provider = provider;
    }

    /**
     * The value found by the provider.
     */
    public Object getValue() {
        return value;
    }

    /**
     * The configuration key the code asked the provider for.
     * May be different than {@link #getActualKey()} if the provider does fuzzy matching such as case-insensitive lookups or . -> _ conversions etc.
     */
    public String getRequestedKey() {
        return requestedKey;
    }

    /**
     * The actual key/source for the value.
     * This may be different than {@link #getRequestedKey()} if the provider does fuzzy matching such as case-insensitive lookups or . -> _ conversions etc.
     */
    public String getActualKey() {
        return actualKey;
    }

    /**
     * A description of where the value came from.
     */
    public String getSourceDescription() {
        return sourceDescription;
    }

    /**
     * The provider for this value
     */
    public ConfigurationValueProvider getProvider() {
        return provider;
    }

    public String describe() {
        return sourceDescription + " '" + actualKey + "'";
    }
}
