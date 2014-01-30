package liquibase.configuration;

import liquibase.exception.UnexpectedLiquibaseException;

import java.util.*;

public abstract class AbstractConfiguration {

    private ConfigurationContainer configurationContainer;

    public AbstractConfiguration(String namespace) {
        this.configurationContainer = new ConfigurationContainer(namespace);
    }

    protected ConfigurationContainer getContainer() {
        return configurationContainer;
    }

    public ConfigurationProperty getProperty(String propertyName) {
        return getContainer().getProperty(propertyName);
    }

    public <T> T getValue(String propertyName, Class<T> returnType) {
        return getContainer().getValue(propertyName, returnType);
    }


    protected void init(ConfigurationProvider... valueContainers) {
        for (ConfigurationProperty property : getContainer().properties.values()) {
            property.init(valueContainers);
        }
    }

    public static class ConfigurationContainer {

        private final String namespace;
        private final Map<String, ConfigurationProperty> properties = new HashMap<String, ConfigurationProperty>();

        protected ConfigurationContainer(String namespace) {
            this.namespace = namespace;
        }

        public ConfigurationProperty addProperty(String propertyName, Class type) {
            ConfigurationProperty property = new ConfigurationProperty(namespace, propertyName, type);
            properties.put(propertyName, property);

            return property;
        }

        public ConfigurationProperty getProperty(String propertyName) {
            ConfigurationProperty property = properties.get(propertyName);
            if (property == null) {
                throw new UnexpectedLiquibaseException("Unknown property on "+getClass().getName()+": "+propertyName);
            }

            return property;
        }

        public <T> T getValue(String propertyName, Class<T> returnType) {
            ConfigurationProperty property = getProperty(propertyName);

            if (!property.getType().isAssignableFrom(returnType)) {
                throw new UnexpectedLiquibaseException("Property "+propertyName+" on "+getClass().getName()+" is of type "+property.getType().getName()+", not "+returnType.getName());
            }

            return (T) property.getValue();
        }

        public void setValue(String propertyName, Object value) {
            ConfigurationProperty property = properties.get(propertyName);

            if (property == null) {
                throw new UnexpectedLiquibaseException("Unknown property on "+getClass().getName()+": "+propertyName);
            }

            property.setValue(value);

        }
    }

}
