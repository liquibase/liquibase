package liquibase.configuration;

/**
 * Defines a way for {@link LiquibaseConfiguration} to find configured values.
 */
public interface ConfigurationValueProvider {

    /**
     * Returns the precedence of values returned by this provider. Higher a provider with higher precedence overrides values from lower precedence providers.
     */
    int getPrecedence();

    /**
     * Lookup the given key(s) in this source.
     * It is up to the implementation to provide any "smoothing" or translation of key names.
     * For example, an EnvironmentValueProvider will look check environment variables containing _'s rather than .'s.
     *
     * @param keyAndAliases an array of keys to check, where the first element is the canonical key name, any aliases for that key as later elements.
     *
     * @return null if the key is not defined in this provider.
     */
    ProvidedValue getProvidedValue(String... keyAndAliases);
}
