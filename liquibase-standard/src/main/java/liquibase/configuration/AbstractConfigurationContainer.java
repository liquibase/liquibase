package liquibase.configuration;

import liquibase.Scope;
import liquibase.configuration.core.DeprecatedConfigurationValueProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated Use new {@link ConfigurationDefinition} style
 */
public abstract class AbstractConfigurationContainer implements ConfigurationContainer {

    private final String namespace;
    private final ConfigurationContainer container;
    private final Map<String, ConfigurationProperty> properties = new HashMap<>();

    public AbstractConfigurationContainer(String namespace) {
        this.namespace = namespace;
        this.container = new ConfigurationContainer();
    }

    /**
     * @deprecated
     */
    protected ConfigurationContainer getContainer() {
        return container;
    }

    /**
     * @deprecated
     */
    @Override
    public ConfigurationProperty getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * @deprecated
     */
    @Override
    public Set<ConfigurationProperty> getProperties() {
        return new HashSet<>(properties.values());
    }

    /**
     * @deprecated
     */
    @Override
    public <T> T getValue(String propertyName, Class<T> returnType) {
        final ConfiguredValue<Object> currentValue = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(null, null, namespace + "." + propertyName);
        return (T) currentValue.getValue();
    }

    /**
     * @deprecated
     */
    @Override
    public void setValue(String propertyName, Object value) {
        DeprecatedConfigurationValueProvider.setData(namespace + "." + propertyName, value);
    }

    /**
     * @deprecated
     */
    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * @deprecated
     */
    protected class ConfigurationContainer {

        /**
         * @deprecated
         */
        public ConfigurationProperty addProperty(String propertyName, Class type) {
            final ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder(namespace);
            final ConfigurationDefinition.Building newDefinition = builder.define(propertyName, type);

            final ConfigurationProperty property = new ConfigurationProperty(namespace, newDefinition);

            properties.put(propertyName, property);
            return property;
        }

        public ConfigurationProperty getProperty(String propertyName) {
            return AbstractConfigurationContainer.this.getProperty(propertyName);
        }

        public <T> T getValue(String propertyName, Class<T> returnType) {
            return AbstractConfigurationContainer.this.getValue(propertyName, returnType);
        }

        public void setValue(String propertyName, Object value) {
            AbstractConfigurationContainer.this.setValue(propertyName, value);
        }
    }

    protected static class DelegatedConfigurationContainer extends AbstractConfigurationContainer {
        public DelegatedConfigurationContainer(String namespace) {
            super(namespace);
        }
    }
}
