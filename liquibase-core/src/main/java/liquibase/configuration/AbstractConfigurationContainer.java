package liquibase.configuration;

import liquibase.exception.UnexpectedLiquibaseException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for configuration classes used by {@link liquibase.configuration.LiquibaseConfiguration}.
 * Implementations must have a no-arg constructor for LiquibaseConfiguration to initialize them as needed.
 * <p>
 * AbstractConfigurationContainer implementations contain a "namespace" which can be used as the prefix to system properties or cases where there may be name conflicts.
 * <p>
 * Properties can be accessed by name using the {@link #getValue(String, Class)} method, but implementation should implement standard get/set methods for easier use.
 */
public abstract class AbstractConfigurationContainer implements ConfigurationContainer {

    private ConfigurationContainer configurationContainer;

    /**
     * Subclasses must call this constructor passing the namespace, but must themselves provide a no-arg public constructor.
     */
    protected AbstractConfigurationContainer(String namespace) {
        this.configurationContainer = new ConfigurationContainer(namespace);
    }

    protected ConfigurationContainer getContainer() {
        return configurationContainer;
    }

    /**
     * Return the ConfigurationProperty object for the given property name.
     * Normally {@link #getValue(String, Class)} is the easiest method to call.
     */
    @Override
    public ConfigurationProperty getProperty(String propertyName) {
        return getContainer().getProperty(propertyName);
    }

    /**
     * Return all available properties.
     */
    @Override
    public Set<ConfigurationProperty> getProperties() {
        return new HashSet<>(getContainer().properties.values());
    }

    /**
     * Returns the value for the given property cast to the passed returnType.
     * If the type of the property and the given return type are not compatible an exception will be thrown.
     * If the passed propertyName is not a defined property, an exception is thrown.
     */
    @Override
    public <T> T getValue(String propertyName, Class<T> returnType) {
        return getContainer().getValue(propertyName, returnType);
    }


    /**
     * Override default values for properties with the given ConfigurationProviders.
     */
    @Override
    public void init(ConfigurationValueProvider... configurationValueProviders) {
        if (configurationValueProviders != null) {
            for (ConfigurationProperty property : getContainer().properties.values()) {
                property.init(configurationValueProviders);
            }
        }
    }
    
    @Override
    public void setValue(String propertyName, Object value) {
      getContainer().setValue(propertyName, value);
    }

    /**
     * Like a java.util.Map, but with extra logic for working with ConfigurationProperties.
     * Used to define and hold available properties. Methods return "this" to allow easy chaining.
     */
    protected static class ConfigurationContainer {

        private final String namespace;
        private final Map<String, ConfigurationProperty> properties = new HashMap<>();

        protected ConfigurationContainer(String namespace) {
            this.namespace = namespace;
        }


        /**
         * Adds a property definition to this configuration.
         */
        public ConfigurationProperty addProperty(String propertyName, Class type) {
            ConfigurationProperty property = new ConfigurationProperty(namespace, propertyName, type);
            properties.put(propertyName, property);

            return property;
        }

        /**
         * Returns the ConfigurationProperty object with the given name. If the property was not defined, an exception is thrown.
         */
        public ConfigurationProperty getProperty(String propertyName) {
            ConfigurationProperty property = properties.get(propertyName);
            if (property == null) {
                throw new UnexpectedLiquibaseException("Unknown property on "+getClass().getName()+": "+propertyName);
            }

            return property;
        }

        /**
         * Returns the value for the given property. If the property was not defined, an exception is thrown.
         */
        public <T> T getValue(String propertyName, Class<T> returnType) {
            ConfigurationProperty property = getProperty(propertyName);

            if (!property.getType().isAssignableFrom(returnType)) {
                throw new UnexpectedLiquibaseException("Property "+propertyName+" on "+getClass().getName()+" is of type "+property.getType().getName()+", not "+returnType.getName());
            }

            return (T) property.getValue();
        }

        /**
         * Sets the value for the given property.
         * Any value set through this method will overwrite any default values found by the configured ConfigurationPropertyProviders.
         * If the property was not defined, an exception is thrown.
         */
        public void setValue(String propertyName, Object value) {
            ConfigurationProperty property = properties.get(propertyName);

            if (property == null) {
                throw new UnexpectedLiquibaseException("Unknown property on "+getClass().getName()+": "+propertyName);
            }

            property.setValue(value);

        }
    }

}
