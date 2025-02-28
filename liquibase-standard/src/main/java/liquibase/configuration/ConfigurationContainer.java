package liquibase.configuration;

import java.util.Set;

/**
 * @deprecated interface from old style configuration code. Use {@link ConfigurationDefinition} and {@link AutoloadedConfigurations} now.
 */
public interface ConfigurationContainer {

    /**
     * @deprecated
     */
    ConfigurationProperty getProperty(String propertyName);

    /**
     * @deprecated
     */
    Set<ConfigurationProperty> getProperties();

    /**
     * @deprecated
     */
    <T> T getValue(String propertyName, Class<T> returnType);

    /**
     * @deprecated
     */
    void setValue(String propertyName, Object value);

    /**
     * @deprecated
     */
    String getNamespace();
}
