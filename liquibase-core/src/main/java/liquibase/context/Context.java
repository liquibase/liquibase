package liquibase.context;

import liquibase.exception.UnexpectedLiquibaseException;

import java.math.BigDecimal;
import java.util.*;

public abstract class Context {

    private ContextState contextState;

    public Context(String contextPrefix) {
        this.contextState = new ContextState(contextPrefix);
    }

    public ContextState getState() {
        return contextState;
    }

    public ContextProperty getProperty(String propertyName) {
        return getState().getProperty(propertyName);
    }

    public <T> T getValue(String propertyName, Class<T> returnType) {
        return getState().getValue(propertyName, returnType);
    }


    protected void init(ContextValueContainer... valueContainers) {
        for (ContextProperty property : getState().properties.values()) {
            property.init(valueContainers);
        }
    }

    public static class ContextState {

        private final String contextPrefix;
        private final Map<String, ContextProperty> properties = new HashMap<String, ContextProperty>();

        protected ContextState(String contextPrefix) {
            this.contextPrefix = contextPrefix;
        }

        protected ContextProperty addProperty(String propertyName, Class type) {
            ContextProperty property = new ContextProperty(contextPrefix, propertyName, type);
            properties.put(propertyName, property);

            return property;
        }

        public ContextProperty getProperty(String propertyName) {
            ContextProperty property = properties.get(propertyName);
            if (property == null) {
                throw new UnexpectedLiquibaseException("Unknown property on "+getClass().getName()+": "+propertyName);
            }

            return property;
        }

        public <T> T getValue(String propertyName, Class<T> returnType) {
            ContextProperty property = getProperty(propertyName);

            if (!property.type.isAssignableFrom(returnType)) {
                throw new UnexpectedLiquibaseException("Property "+propertyName+" on "+getClass().getName()+" is of type "+property.type.getName()+", not "+returnType.getName());
            }

            return (T) property.getValue();
        }

        public void setValue(String propertyName, Object value) {
            ContextProperty property = properties.get(propertyName);

            if (property == null) {
                throw new UnexpectedLiquibaseException("Unknown property on "+getClass().getName()+": "+propertyName);
            }

            property.setValue(value);

        }
    }

    public static class ContextProperty {

        private final String contextPrefix;
        private final String name;
        private final Class type;
        private List<String> aliases = new ArrayList<String>();

        private Object value;
        private String description;
        private Object defaultValue;
        private boolean wasSet = false;

        private ContextProperty(String contextPrefix, String propertyName, Class type) {
            this.contextPrefix = contextPrefix;
            this.name = propertyName;
            this.type = type;
        }

        protected void init(ContextValueContainer[] valueContainers) {
            Object containerValue = null;

            for (ContextValueContainer container : valueContainers) {
                containerValue = container.getValue(contextPrefix, name);
                for (String alias : aliases) {
                    if (containerValue != null) {
                        break;
                    }
                    containerValue = container.getValue(contextPrefix, alias);
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

        public String getContextPrefix() {
            return contextPrefix;
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

        protected ContextProperty addAlias(String... aliases) {
            if (aliases != null) {
                this.aliases.addAll(Arrays.asList(aliases));
            }

            return this;
        }

        public String getDescription() {
            return description;
        }

        protected ContextProperty setDescription(String description) {
            this.description = description;
            return this;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        protected ContextProperty setDefaultValue(Object defaultValue) {
            if (defaultValue != null && !type.isAssignableFrom(defaultValue.getClass())) {
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
