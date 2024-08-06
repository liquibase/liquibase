package liquibase.configuration;

import lombok.Getter;

/**
 * Describes a value found from a provider. This is the most basic level at which a configuration value is defined.
 */
@Getter
public class ProvidedValue {
    /**
     * -- GETTER --
     *  The configuration key the code asked the provider for.
     *  May be different than
     *  if the provider does fuzzy matching such as case-insensitive lookups or . -> _ conversions etc.
     */
    private final String requestedKey;
    /**
     * -- GETTER --
     *  The actual key/source for the value.
     *  This may be different than
     *  if the provider does fuzzy matching such as case-insensitive lookups or . -> _ conversions etc.
     */
    private final String actualKey;
    /**
     * -- GETTER --
     *  A description of where the value came from.
     */
    private final String sourceDescription;
    /**
     * -- GETTER --
     *  The provider for this value
     */
    private final ConfigurationValueProvider provider;
    /**
     * -- GETTER --
     *  The value found by the provider.
     */
    private final Object value;

    public ProvidedValue(String requestedKey, String actualKey, Object value, String sourceDescription, ConfigurationValueProvider provider) {
        this.requestedKey = requestedKey;
        this.actualKey = actualKey;
        this.value = value;
        this.sourceDescription = sourceDescription;
        this.provider = provider;
    }

    public String describe() {
        return sourceDescription + " '" + actualKey + "'";
    }
}
