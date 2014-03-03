package liquibase.configuration;

/**
 * Interface for classes that are able to lookup overriding default LiquibaseConfiguration values.
 * For example, {@link liquibase.configuration.SystemPropertyProvider} can look up property values in system properties.
 */
public interface ConfigurationValueProvider {

    /**
     * Return the value for a given namespace and property. Returns null if this provider does not have a value for this property.
     */
    Object getValue(String namespace, String property);

    /**
     * Generates a human consumable description of how the configured ConfigurationValueProvider(s) will attempt to set a default value.
     * See {@link LiquibaseConfiguration#describeValueLookupLogic(ConfigurationProperty)}
     */
    String describeValueLookupLogic(ConfigurationProperty property);
}
