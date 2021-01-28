package liquibase.configuration;

import liquibase.configuration.core.SystemPropertyProvider;

/**
 * Interface for classes that are able to lookup overriding default LiquibaseConfiguration values.
 * For example, {@link SystemPropertyProvider} can look up property values in system properties.
 */
public interface ConfigurationValueProvider {

    int getPrecedence();

    Object getValue(String property);

    /**
     * Generates a human consumable description of how the configured ConfigurationValueProvider(s) will attempt to set a default value.
     */
    String describeValueLookupLogic(String property);
}
