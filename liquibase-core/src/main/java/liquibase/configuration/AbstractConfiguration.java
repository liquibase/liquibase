package liquibase.configuration;

import liquibase.exception.UnexpectedLiquibaseException;

import java.math.BigDecimal;
import java.util.*;

public abstract class AbstractConfiguration {

    private ConfigurationContainer configurationContainer;

    public AbstractConfiguration(String namespace) {
        this.configurationContainer = new ConfigurationContainer(namespace);
    }

    public ConfigurationContainer getContainer() {
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

            if (!property.type.isAssignableFrom(returnType)) {
                throw new UnexpectedLiquibaseException("Property "+propertyName+" on "+getClass().getName()+" is of type "+property.type.getName()+", not "+returnType.getName());
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

    public static class ConfigurationProperty {

        private final String namespace;
        private final String name;
        private final Class type;
        private List<String> aliases = new ArrayList<String>();

        private Object value;
        private String description;
        private Object defaultValue;
        private boolean wasSet = false;

        private ConfigurationProperty(String namespace, String propertyName, Class type) {
            this.namespace = namespace;
            this.name = propertyName;
            this.type = type;
        }

        protected void init(ConfigurationProvider[] valueContainers) {
            Object containerValue = null;

            for (ConfigurationProvider container : valueContainers) {
                containerValue = container.getValue(namespace, name);
                for (String alias : aliases) {
                    if (containerValue != null) {
                        break;
                    }
                    containerValue = container.getValue(namespace, alias);
                }
            }

            if (containerValue == null) {
                value = defaultValue;
            } else {
                try {
                    value = valueOf(containerValue);
                    wasSet = true;
                } catch (NumberFormatException e) {
                    throw new UnexpectedLiquibaseException("Error parsing "+containerValue+" as a "+type.getSimpleName());
                }
            }
        }


        public String getName() {
            return name;
        }

        public String getNamespace() {
            return namespace;
        }

        protected Object valueOf(Object value) {
            if (value == null) {
                return value;
            } else if (type.isAssignableFrom(value.getClass())) {
                return value;
            } else if (value instanceof String) {
                if (type.equals(Boolean.class)) {
                    return Boolean.valueOf((String) value);
                } else if (type.equals(Integer.class)) {
                    return Integer.valueOf((String) value);
                } else if (type.equals(BigDecimal.class)) {
                    return new BigDecimal((String) value);
                } else {
                    throw new UnexpectedLiquibaseException("Cannot parse property "+type.getSimpleName()+" to a "+type.getSimpleName());
                }
            } else {
                throw new UnexpectedLiquibaseException("Could not convert "+value.getClass().getSimpleName()+" to a "+type.getSimpleName());
            }
        }

        public Object getValue() {
            return value;
        }

        public <T> T getValue(Class<T> type) {
            if (!this.type.isAssignableFrom(type)) {
                throw new UnexpectedLiquibaseException("Property "+name+" on is of type "+this.type.getSimpleName()+", not "+type.getSimpleName());
            }

            return (T) value;
        }

        public void setValue(Object value) {
            if (value != null && !type.isAssignableFrom(value.getClass())) {
                throw new UnexpectedLiquibaseException("Property "+name+" on is of type "+type.getSimpleName()+", not "+value.getClass().getSimpleName());
            }

            this.value = value;
            wasSet = true;
        }

        public ConfigurationProperty addAlias(String... aliases) {
            if (aliases != null) {
                this.aliases.addAll(Arrays.asList(aliases));
            }

            return this;
        }

        public String getDescription() {
            return description;
        }

        public ConfigurationProperty setDescription(String description) {
            this.description = description;
            return this;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public ConfigurationProperty setDefaultValue(Object defaultValue) {
            if (defaultValue != null && !type.isAssignableFrom(defaultValue.getClass())) {
                if (type == Long.class && defaultValue instanceof Integer) {
                    return setDefaultValue(((Integer) defaultValue).longValue());
                }
                throw new UnexpectedLiquibaseException("Property "+name+" on is of type "+type.getSimpleName()+", not "+defaultValue.getClass().getSimpleName());
            }

            this.defaultValue = defaultValue;

            return this;
        }

        public boolean wasSet() {
            return wasSet;
        }
    }
}
