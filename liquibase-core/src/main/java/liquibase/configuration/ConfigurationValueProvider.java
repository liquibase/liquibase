package liquibase.configuration;

import liquibase.command.CommandScope;
import liquibase.configuration.core.DefaultsFileValueProvider;

/**
 * Defines a way for {@link LiquibaseConfiguration} to find configured values.
 */
public interface ConfigurationValueProvider {

    /**
     * Returns the precedence of values returned by this provider. Higher a provider with higher precedence overrides values from lower precedence providers.
     * <br><br>
     * Standard provider precedence:
     * <ul>
     *     <li>400 {@link liquibase.configuration.core.ScopeValueProvider}</li>
     *     <li>350 {@link liquibase.configuration.core.DeprecatedConfigurationValueProvider}</li>
     *     <li>250 Integration specific providers</li>
     *     <li>300: TODO JNDI attributes</li>
     *     <li>250: TODO Servlet Context</li>
     *     <li>200 {@link liquibase.configuration.core.SystemPropertyValueProvider}</li>
     *     <li>150 EnvironmentValueProvider</li>
     *     <li>100: TODO profile/context specific properties files</li>
     *     <li>50: {@link DefaultsFileValueProvider}</li>
     * </ul>
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

    /**
     * Perform any validation of keys/values stored in this provider for the given commandScope.
     * For example, check for keys that do not match anything expected.
     */
    void validate(CommandScope commandScope) throws IllegalArgumentException;
}
