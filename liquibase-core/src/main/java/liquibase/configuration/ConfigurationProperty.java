package liquibase.configuration;

import liquibase.configuration.core.DeprecatedConfigurationValueProvider;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * @deprecated
 */
public class ConfigurationProperty {

    private final ConfigurationDefinition.Building definitionBuilder;
    private ConfigurationDefinition definition;
    private final String namespace;

    public ConfigurationProperty(String namespace, ConfigurationDefinition.Building definitionBuilder) {
        this.namespace = namespace;
        this.definitionBuilder = definitionBuilder;
        this.definition = definitionBuilder.buildTemporary();
    }

    /**
     * Returns the property name.
     *
     * @deprecated
     */
    public String getName() {
        return definition.getKey().replace(namespace+".", "");
    }

    /**
     * Returns the namespace used by this property's {@link ConfigurationContainer}
     * @deprecated
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the type of value stored in this property
     * @deprecated
     */
    public Class getType() {
        return definition.getDataType();
    }

    /**
     * Converts an object of a different type to the type used by this property. If types are not convertible, an exception is thrown.
     * @deprecated
     */
    protected Object valueOf(Object value) {
        Class type = definition.getDataType();
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
     * @deprecated
     */
    public Object getValue() {
        return definition.getCurrentValue();
    }

    /**
     * Returns the value currently stored in this property cast to the given type.
     * @deprecated
     */
    public <T> T getValue(Class<T> type) {
        if (!this.definition.getDataType().isAssignableFrom(type)) {
            throw new UnexpectedLiquibaseException("Property "+definition.getDataType()+" on is of type "+this.definition.getDataType().getSimpleName()+", not "+type.getSimpleName());
        }

        return (T) definition.getCurrentValue();
    }

    /**
     * Overwrites the value currently stored in this property. It he passed type is not compatible with the defined type, an exception is thrown.
     * @deprecated
     */
    public void setValue(Object value) {
        DeprecatedConfigurationValueProvider.setData(definition, value);
    }

    /**
     * Adds an alias for this property. An alias is an alternate to the "name" field that can be used by the ConfigurationProvers to look up starting values.
     * @deprecated
     */
    public ConfigurationProperty addAlias(String... aliases) {
        if (aliases != null) {
            for (String alias : aliases) {
                definitionBuilder.addAliasKey(alias);
            }

            definition = definitionBuilder.buildTemporary();
        }

        return this;
    }

    /**
     * Returns a human-readable definition of this property
     * @deprecated
     */
    public String getDescription() {
        return definition.getDescription();
    }

    /**
     * @deprecated
     */
    public ConfigurationProperty setDescription(String description) {
        this.definitionBuilder.setDescription(description);
        this.definition = definitionBuilder.buildTemporary();

        return this;
    }

    /**
     * Returns the default value to use if no ConfigurationProviders override it.
     * @deprecated
     */
    public Object getDefaultValue() {
        return definition.getDefaultValue();
    }

    /**
     * Sets the default value to use if no ConfigurationProviders override it. Throws an exception if the given object is not compatible with the defined type.
     * @deprecated
     */
    public ConfigurationProperty setDefaultValue(Object defaultValue) {
        this.definitionBuilder.setDefaultValue(defaultValue);
        this.definition = definitionBuilder.buildTemporary();

        return this;
    }

    /**
     * Returns true if the value has been set by a ConfigurationValueProvider or by {@link #setValue(Object)}
     * @deprecated
     */
    public boolean getWasOverridden() {
        return !this.definition.getCurrentConfiguredValue().wasDefaultValueUsed();
    }

    /**
     * @deprecated
     */
    public ConfigurationProperty setValueHandler(ConfigurationValueHandler handler) {
        this.definitionBuilder.setValueHandler(handler::convert);
        this.definition = definitionBuilder.buildTemporary();

        return this;
    }
}
