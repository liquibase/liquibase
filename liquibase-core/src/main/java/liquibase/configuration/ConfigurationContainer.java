package liquibase.configuration;

import java.util.Set;

public interface ConfigurationContainer {
    ConfigurationProperty getProperty(String propertyName);

    Set<ConfigurationProperty> getProperties();

    <T> T getValue(String propertyName, Class<T> returnType);

    void init(ConfigurationValueProvider... configurationValueProviders);
}
