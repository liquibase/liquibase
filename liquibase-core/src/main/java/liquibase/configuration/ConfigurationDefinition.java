package liquibase.configuration;

import liquibase.Scope;
import liquibase.util.ObjectUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * A higher-level/detailed definition to provide type-safety, metadata, default values, etc..
 * Any code that is working with configurations should be using an instance of this class, rather than the lower-level, generic {@link LiquibaseConfiguration}
 * <p>
 * ConfigurationDefinitions that are registered with {@link LiquibaseConfiguration#registerDefinition(ConfigurationDefinition)} will
 * be available in generated help etc.
 * <p>
 * These objects are immutable, so to construct definitions, use {@link Builder}
 * <p>
 * The definition keys should be dot-separated, camelCased names, using a unique "namespace" as part of it.
 * For example: <pre>yourCorp.yourProperty</pre> or <pre>yourCorp.sub.otherProperty</pre>.
 * Liquibase uses "liquibase" as the base namespace like <pre>liquibase.shouldRun</pre>
 */
public class ConfigurationDefinition<DataType> implements Comparable<ConfigurationDefinition<DataType>> {

    private final String key;
    private final Set<String> aliasKeys = new TreeSet<>();
    private final Class<DataType> type;
    private String description;
    private DataType defaultValue;
    private boolean commonlyUsed;
    private ConfigurationValueConverter<DataType> valueHandler;
    private ConfigurationValueObfuscator<DataType> valueObfuscator;

    private static final Pattern ALLOWED_KEY_PATTERN = Pattern.compile("[a-zA-Z0-9.]+");

    /**
     * @return if the given {@link ConfiguredValue} was set by a default value
     */
    public static boolean wasDefaultValueUsed(ConfiguredValue<?> configuredValue) {
        for (ProvidedValue providedValue : configuredValue.getProvidedValues()) {
            if (providedValue.getProvider() != null && providedValue.getProvider() instanceof ConfigurationDefinition.DefaultValueProvider) {
                return true;
            }
        }

        return false;
    }

    /**
     * Constructor private to force {@link Builder} usage
     *
     * @throws IllegalArgumentException if an invalid key is specified.
     */
    private ConfigurationDefinition(String key, Class<DataType> type) throws IllegalArgumentException {
        if (!ALLOWED_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid key format: " + key);
        }

        this.key = key;
        this.type = type;
        this.valueHandler = value -> ObjectUtil.convert(value, type);
    }

    /**
     * Convenience method around {@link #getCurrentConfiguredValue()} to return the value.
     */
    public DataType getCurrentValue() {
        final Object value = getCurrentConfiguredValue().getProvidedValue().getValue();
        try {
            return (DataType) value;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The current value of " + key + " not the expected type: " + e.getMessage(), e);
        }
    }

    /**
     * Convenience method around {@link #getCurrentConfiguredValue()} to return the obfuscated version of the value.
     *
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
     * Will always return a {@link ConfiguredValue},
     */
    public ConfiguredValue<DataType> getCurrentConfiguredValue() {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        List<String> keyList = new ArrayList<>();
        keyList.add(this.getKey());
        keyList.addAll(this.getAliasKeys());

        ConfiguredValue<?> configurationValue = liquibaseConfiguration.getCurrentConfiguredValue(keyList.toArray(new String[0]));

        if (!configurationValue.found()) {
            defaultValue = this.getDefaultValue();
            if (defaultValue != null) {
                configurationValue.override(new DefaultValueProvider(this.getDefaultValue()).getProvidedValue(key));
            }
        }

        final ProvidedValue providedValue = configurationValue.getProvidedValue();
        final Object originalValue = providedValue.getValue();
        final DataType finalValue = valueHandler.convert(originalValue);
        if (originalValue != finalValue) {
            configurationValue.override(new ConvertedValueProvider<DataType>(finalValue, providedValue).getProvidedValue(key));
        }

        return (ConfiguredValue<DataType>) configurationValue;
    }

    /**
     * The standard configuration key for this definition. See the {@link ConfigurationDefinition} class-level docs on key format.
     */
    public String getKey() {
        return key;
    }

    /**
     * @return alternate configuration keys to check for values. Used for backwards compatibility.
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
     * This will be exposed to end-users in generated help.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The default value used by this definition if no value is currently configured.
     * <p>
     * NOTE: this is only used if none of the {@link ConfigurationValueProvider}s have a configuration for the property.
     * Even if some return "null", that is still considered a provided value to use rather than this default.
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
    public int compareTo(ConfigurationDefinition o) {
        return this.getKey().compareTo(o.getKey());
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
     * Return true if the given key matches this definition.
     */
    public boolean equalsKey(String key) {
        if (key == null) {
            return false;
        }

        if (getKey().equalsIgnoreCase(key)) {
            return true;
        }

        for (String alias : getAliasKeys()) {
            if (alias.equalsIgnoreCase(key)) {
                return true;
            }
        }

        return false;
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
            if (!ALLOWED_KEY_PATTERN.matcher(defaultKeyPrefix).matches()) {
                throw new IllegalArgumentException("Invalid prefix format: " + defaultKeyPrefix);
            }

            this.defaultKeyPrefix = defaultKeyPrefix;
        }

        /**
         * Starts a new definition with the given key. Always adds the  defaultKeyPrefix.
         */
        public <T> Building<T> define(String key, Class<T> dataType) {
            final ConfigurationDefinition<T> definition = new ConfigurationDefinition<>(defaultKeyPrefix + "." + key, dataType);

            return new Building<>(definition);
        }
    }

    public static class Building<DataType> {

        private final ConfigurationDefinition<DataType> definition;

        private Building(ConfigurationDefinition<DataType> definition) {
            this.definition = definition;
        }

        public Building<DataType> addAliasKey(String alias) {
            if (!ALLOWED_KEY_PATTERN.matcher(alias).matches()) {
                throw new IllegalArgumentException("Invalid alias format: " + alias);
            }

            definition.aliasKeys.add(alias);

            return this;
        }

        public Building<DataType> setDescription(String description) {
            definition.description = description;
            return this;
        }

        public Building<DataType> setDefaultValue(DataType defaultValue) {
            definition.defaultValue = defaultValue;
            return this;
        }

        public Building<DataType> setValueHandler(ConfigurationValueConverter<DataType> handler) {
            definition.valueHandler = handler;

            return this;
        }

        public Building<DataType> setValueObfuscator(ConfigurationValueObfuscator<DataType> handler) {
            definition.valueObfuscator = handler;

            return this;
        }

        public Building<DataType> setCommonlyUsed(boolean commonlyUsed) {
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

    /**
     * Used to track configuration values set by a default
     */
    private static final class DefaultValueProvider implements ConfigurationValueProvider {

        private final Object value;

        public DefaultValueProvider(Object value) {
            this.value = value;
        }

        @Override
        public int getPrecedence() {
            return -1;
        }

        @Override
        public ProvidedValue getProvidedValue(String... keyAndAliases) {
            return new ProvidedValue(keyAndAliases[0], keyAndAliases[0], value, "Default value", this);
        }
    }

    /**
     * Used to track configuration values converted by a handler
     */
    private static final class ConvertedValueProvider<DataType> implements ConfigurationValueProvider {

        private final DataType value;
        private final String originalSource;
        private final String actualKey;

        public ConvertedValueProvider(DataType value, ProvidedValue originalProvidedValue) {
            this.value = value;
            this.actualKey = originalProvidedValue.getActualKey();
            this.originalSource = originalProvidedValue.getSourceDescription();
        }

        @Override
        public int getPrecedence() {
            return -1;
        }

        @Override
        public ProvidedValue getProvidedValue(String... keyAndAliases) {
            return new ProvidedValue(keyAndAliases[0], actualKey, value, originalSource, this);
        }
    }
}

