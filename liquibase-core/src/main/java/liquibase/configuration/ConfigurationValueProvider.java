package liquibase.configuration;

import liquibase.configuration.core.SystemEnvironmentValueProvider;

/**
 * Defines a way for {@link LiquibaseConfiguration} to find configured values.
 */
public interface ConfigurationValueProvider {

    /**
     * Returns the precedence of values returned by this provider. Higher a provider with higher precedence overrides values from lower precedence providers.
     */
    int getPrecedence();

    /**
     * Lookup the given key in this source.
     * It is up to the implementation to provide any "smoothing" or translation of key names.
     * For example, {@link SystemEnvironmentValueProvider} will look check environment variables containing _'s rather than .'s.
     *
     * @return null if the key is not defined in this provider.
     */
    CurrentValueSourceDetails getValue(String key);
}
