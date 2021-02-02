package liquibase.configuration;

import liquibase.Scope;
import liquibase.util.ObjectUtil;

import java.util.*;

/**
 * A higher-level definition of a configuration.
 * Provides type-safety, metadata, default values, etc. vs. what is available in the lower-level {@link LiquibaseConfiguration}.
 * ConfigurationDefinitions that are registered with {@link LiquibaseConfiguration#registerDefinition(ConfigurationDefinition)} will
 * be available in generated help etc.
 * <p>
 * These objects are immutable, so to construct definitions, use {@link Builder}
 *
 */
public class ConfigurationDefinition<DataType> implements Comparable {

    private String key;
    private Set<String> aliasKeys = new TreeSet<>();
    private Class<DataType> type;
    private String description;
    private DataType defaultValue;
    private boolean commonlyUsed;
    private ConfigurationValueHandler<DataType> valueHandler;
    private ConfigurationValueObfuscator<DataType> valueObfuscator;

    private ConfigurationDefinition(String key, Class<DataType> type) {
        this.key = key;
        this.type = type;
        this.valueHandler = value -> ObjectUtil.convert(value, type);
    }

    /**
     * Convenience method around {@link #getCurrentValueDetails()} to return the value.
     */
    public DataType getCurrentValue() {
        return getCurrentValueDetails().getValue();
    }

    /**
     * Convenience method around {@link #getCurrentValueDetails()} to return the obfuscated version of the value.
     * @return the obfuscated value, or the plain-text value if no obfuscator is defined for this definition.
     */
    public DataType getCurrentValueObfuscated() {
        final DataType currentValue = getCurrentValue();

        if (this.valueObfuscator == null) {
            return currentValue;
        }

        return this.valueObfuscator.obfuscate(currentValue);
    }

    /**
     * @return Full details on the current value for this definition.
     */
    public CurrentValue<DataType> getCurrentValueDetails() {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        CurrentValueDetails configurationValue = liquibaseConfiguration.getCurrentValue(this.getKey());
        for (String alias : this.aliasKeys) {
            if (configurationValue != null) {
                break;
            }
            configurationValue = liquibaseConfiguration.getCurrentValue(alias);
        }

        DataType finalValue = null;
        List<CurrentValueSourceDetails> sourceHistory = new ArrayList<>();
        if (configurationValue != null) {
            sourceHistory.addAll(configurationValue.getSourceHistory());

            finalValue = valueHandler.convert(configurationValue.getValue());
        }


        boolean defaultValueUsed = false;
        if (finalValue == null) {
            finalValue = this.getDefaultValue();

            if (finalValue == null) {
                sourceHistory.add(0, new CurrentValueSourceDetails(this.getDefaultValue(), "No configuration or default value found for", key));
                defaultValueUsed = false;
            } else {
                sourceHistory.add(0, new CurrentValueSourceDetails(this.getDefaultValue(), "Default value for", key));
                defaultValueUsed = true;
            }
        }

        return new CurrentValue<>(finalValue, sourceHistory, defaultValueUsed);
    }

    /**
     * The standard configuration key for this definition.
     */
    public String getKey() {
        return key;
    }

    /**
     * @return alternate configuration keys to check for values.
     */
    public Set<String> getAliasKeys() {
        return aliasKeys;
    }

    /**
     * @return the type of data this definition returns.
     */
    public Class<DataType> getType() {
        return type;
    }

    /**
     * A user-friendly description of this definition.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The default value used by this definition of no value is currently configured.
     */
    public DataType getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns true if this is configuration users are often interested in setting.
     * Used to simplify generated help by hiding less commonly used settings.
     */
    public boolean getCommonlyUsed() {
        return commonlyUsed;
    }

    @Override
    public int compareTo(Object o) {
        return this.getKey().compareTo(((ConfigurationDefinition) o).getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationDefinition<?> that = (ConfigurationDefinition<?>) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    /**
     * Used to construct new {@link ConfigurationDefinition} instances.
     */
    public static class Builder {
        private final String defaultKeyPrefix;

        /**
         * @param defaultKeyPrefix the prefix to add to new keys that are not fully qualified
         */
        public Builder(String defaultKeyPrefix) {
            this.defaultKeyPrefix = defaultKeyPrefix;
        }

        /**
         * Starts a new definition with the given key. Always adds the  defaultKeyPrefix.
         */
        public <T> NewDefinition<T> define(String key, Class<T> dataType) {
            final ConfigurationDefinition<T> definition = new ConfigurationDefinition<>(defaultKeyPrefix + "." + key, dataType);

            return new NewDefinition<>(definition);
        }

        public static class NewDefinition<DataType> {

            private final ConfigurationDefinition<DataType> definition;

            private NewDefinition(ConfigurationDefinition<DataType> definition) {
                this.definition = definition;
            }

            public NewDefinition<DataType> addAliasKey(String alias) {
                definition.aliasKeys.add(alias);

                return this;
            }

            public NewDefinition<DataType> setDescription(String description) {
                definition.description = description;
                return this;
            }

            public NewDefinition<DataType> setDefaultValue(DataType defaultValue) {
                definition.defaultValue = defaultValue;
                return this;
            }

            public NewDefinition<DataType> setValueHandler(ConfigurationValueHandler<DataType> handler) {
                definition.valueHandler = handler;

                return this;
            }

            public NewDefinition<DataType> setValueObfuscator(ConfigurationValueObfuscator<DataType> handler) {
                definition.valueObfuscator = handler;

                return this;
            }

            public NewDefinition<DataType> setCommonlyUsed(boolean commonlyUsed) {
                definition.commonlyUsed = commonlyUsed;

                return this;
            }

            /**
             * Finishes building this definition AND registers it with {@link LiquibaseConfiguration#registerDefinition(ConfigurationDefinition)}.
             * To not register this definition, use {@link #buildTemporary()}
             */
            public ConfigurationDefinition<DataType> build() {
                Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).registerDefinition(definition);

                return definition;
            }

            /**
             * Finishes building this definition WITHOUT registering it with {@link LiquibaseConfiguration#registerDefinition(ConfigurationDefinition)}.
             * To automatically register this definition, use {@link #build()}
             */
            public ConfigurationDefinition<DataType> buildTemporary() {
                return definition;
            }
        }
    }
}
