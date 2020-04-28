package liquibase.configuration;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains the definition and current value of a given configuration property.
 */
public class ConfigurationProperty {

    private final String namespace;
    private final String name;
    private final Class type;
    private List<String> aliases = new ArrayList<>();

    private Object value;
    private String description;
    private Object defaultValue;
    private boolean wasOverridden;

    public ConfigurationProperty(String namespace, String propertyName, Class type) {
        this.namespace = namespace;
        this.name = propertyName;
        this.type = type;
    }

    /**
     * Initialize this property with values in the given ConfigurationProvers. If the configurationValueProviders do not contain
     * a default value, the property is initialized with the value set by {@link #setDefaultValue(Object)}.
     * If multiple configurationValueProviders contain values, the first in the list wins.
     */
    protected void init(ConfigurationValueProvider[] configurationValueProviders) {
        Object containerValue = null;

        for (ConfigurationValueProvider container : configurationValueProviders) {
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
                wasOverridden = true;
            } catch (NumberFormatException e) {
                throw new UnexpectedLiquibaseException("Error parsing "+containerValue+" as a "+type.getSimpleName());
            }
        }
    }


    /**
     * Returns the property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the namespace used by this property's {@link ConfigurationContainer}
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the type of value stored in this property
     */
    public Class getType() {
        return type;
    }

    /**
     * Converts an object of a different type to the type used by this property. If types are not convertible, an exception is thrown.
     */
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
            } else if (type.equals(Long.class)) {
            	return Long.valueOf((String) value);
            } else if (type.equals(List.class)) {
                return StringUtil.splitAndTrim((String) value, ",");
            } else {
                throw new UnexpectedLiquibaseException("Cannot parse property "+value.getClass().getSimpleName()+" to a "+type.getSimpleName());
            }
        } else {
            throw new UnexpectedLiquibaseException("Could not convert "+value.getClass().getSimpleName()+" to a "+type.getSimpleName());
        }
    }

    /**
     * Returns the value currently stored in this property without any casting.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the value currently stored in this property cast to the given type.
     */
    public <T> T getValue(Class<T> type) {
        if (!this.type.isAssignableFrom(type)) {
            throw new UnexpectedLiquibaseException("Property "+name+" on is of type "+this.type.getSimpleName()+", not "+type.getSimpleName());
        }

        return (T) value;
    }

    /**
     * Overwrites the value currently stored in this property. It he passed type is not compatible with the defined type, an exception is thrown.
     */
    public void setValue(Object value) {
        if ((value != null) && !type.isAssignableFrom(value.getClass())) {
            throw new UnexpectedLiquibaseException("Property "+name+" on is of type "+type.getSimpleName()+", not "+value.getClass().getSimpleName());
        }

        this.value = value;
        wasOverridden = true;
    }

    /**
     * Adds an alias for this property. An alias is an alternate to the "name" field that can be used by the ConfigurationProvers to look up starting values.
     */
    public ConfigurationProperty addAlias(String... aliases) {
        if (aliases != null) {
            this.aliases.addAll(Arrays.asList(aliases));
        }

        return this;
    }

    /**
     * Returns a human-readable definition of this property
     */
    public String getDescription() {
        return description;
    }

    public ConfigurationProperty setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns the default value to use if no ConfigurationProviders override it.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value to use if no ConfigurationProviders override it. Throws an exception if the given object is not compatible with the defined type.
     */
    public ConfigurationProperty setDefaultValue(Object defaultValue) {
        if ((defaultValue != null) && !type.isAssignableFrom(defaultValue.getClass())) {
            if ((type == Long.class) && (defaultValue instanceof Integer)) {
                return setDefaultValue(((Integer) defaultValue).longValue());
            }
            throw new UnexpectedLiquibaseException("Property "+name+" on is of type "+type.getSimpleName()+", not "+defaultValue.getClass().getSimpleName());
        }

        this.defaultValue = defaultValue;

        return this;
    }

    /**
     * Returns true if the value has been set by a ConfigurationValueProvider or by {@link #setValue(Object)}
     */
    public boolean getWasOverridden() {
        return wasOverridden;
    }
}
