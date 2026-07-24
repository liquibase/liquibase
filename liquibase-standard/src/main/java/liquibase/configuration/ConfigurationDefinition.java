package liquibase.configuration;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;
import liquibase.util.ValueHandlerUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * A higher-level/detailed definition to provide type-safety, metadata, default values, etc.
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
    private final Class<DataType> dataType;
    private String description;
    private DataType defaultValue;
    private String defaultValueDescription;
    private boolean commonlyUsed;
    private boolean internal;
    private ConfigurationValueConverter<DataType> valueConverter;
    private ConfigurationValueObfuscator<DataType> valueObfuscator;
    private String referenceKey;

    private static final String ALLOWED_KEY_REGEX = "[a-zA-Z0-9._]+";
    private static final Pattern ALLOWED_KEY_PATTERN = Pattern.compile(ALLOWED_KEY_REGEX);

    /**
     * Scope key set to {@link Boolean#TRUE} while the reference connection of a two-connection command
     * (e.g. {@code diff} / {@code diff-changelog}) is being opened. Reference-scoped definitions
     * (see {@link Builder.Building#referenceScoped()}) resolve from their {@code .reference.} sibling
     * key while this is set. Unset (the primary connection and every single-connection command) preserves
     * the standard resolution.
     */
    public static final String IS_REFERENCE_CONNECTION_SCOPE_KEY = "liquibase.connection.isReference";

    /**
     * Value of a {@code .reference.} sibling key that explicitly opts the reference connection out of an
     * inherited mechanism, resolving the definition as if unset (i.e. to its default). Case-insensitive.
     */
    public static final String REFERENCE_DEFAULT_SENTINEL = "DEFAULT";

    private boolean loggedUsingDefault = false;
    private boolean hidden = false;

    /**
     * Constructor private to force {@link Builder} usage
     *
     * @throws IllegalArgumentException if an invalid key is specified.
     */
    private ConfigurationDefinition(String key, Class<DataType> dataType) throws IllegalArgumentException {
        if (!ALLOWED_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid key format: " + key);
        }

        this.key = key;
        this.dataType = dataType;
        this.valueConverter = value -> ObjectUtil.convert(value, dataType);
    }

    /**
     * Convenience method around {@link #getCurrentConfiguredValue(ConfigurationValueProvider...)} to return the value.
     */
    public DataType getCurrentValue() {
        final Object value = getCurrentConfiguredValue().getProvidedValue().getValue();
        try {
            return (DataType) value;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The current value of " + key + " not the expected type: " + e.getMessage(), e);
        }
    }

    public ConfigurationValueConverter<DataType> getValueConverter() {
        return valueConverter;
    }

    /**
     * Convenience method around {@link #getCurrentConfiguredValue(ConfigurationValueProvider...)} to return the obfuscated version of the value.
     *
     * @return the obfuscated value, or the plain-text value if no obfuscator is defined for this definition.
     */
    public DataType getCurrentValueObfuscated() {
        return getCurrentConfiguredValue().getValueObfuscated();
    }

    /**
     * @return Full details on the current value for this definition.
     * Will always return a {@link ConfiguredValue},
     */
    public ConfiguredValue<DataType> getCurrentConfiguredValue() {
        return getCurrentConfiguredValue(new ConfigurationValueProvider[]{});
    }

    /**
     * @return Full details on the current value for this definition.
     * Will always return a {@link ConfiguredValue},
     *
     * @param additionalValueProviders additional {@link ConfigurationValueProvider}s to use with higher priority than the ones registered in {@link LiquibaseConfiguration}. The higher the array index, the higher the priority.
     */
    public ConfiguredValue<DataType> getCurrentConfiguredValue(ConfigurationValueProvider... additionalValueProviders) {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        // Reference-scoped resolution: while the reference connection of a two-connection command is being
        // opened, a flagged definition resolves from its `.reference.` sibling. This lets diff/diff-changelog
        // use a different auth mechanism on the reference side than on the primary. The value still flows
        // through this definition, so its type conversion, default, and obfuscator all apply unchanged.
        if (referenceKey != null && isReferenceConnectionScope()) {
            ConfiguredValue<?> referenceValue = liquibaseConfiguration.getCurrentConfiguredValue(valueConverter, valueObfuscator, additionalValueProviders, referenceKey);
            if (referenceValue.found()) {
                if (isReferenceDefaultSentinel(referenceValue.getProvidedValue().getValue())) {
                    // Explicit opt-out: resolve as if unset for this connection (→ this definition's default).
                    return applyDefaultAndConvert(new ConfiguredValue<>(key, valueConverter, valueObfuscator));
                }
                return applyDefaultAndConvert(referenceValue);
            }
            // No `.reference.` value set → fall through and inherit the primary value below.
        }

        List<String> keyList = new ArrayList<>();
        keyList.add(this.getKey());
        keyList.addAll(this.getAliasKeys());

        ConfiguredValue<?> configurationValue = liquibaseConfiguration.getCurrentConfiguredValue(valueConverter, valueObfuscator, additionalValueProviders, keyList.toArray(new String[0]));

        return applyDefaultAndConvert(configurationValue);
    }

    private static boolean isReferenceConnectionScope() {
        return Boolean.TRUE.equals(Scope.getCurrentScope().get(IS_REFERENCE_CONNECTION_SCOPE_KEY, Boolean.FALSE));
    }

    private static boolean isReferenceDefaultSentinel(Object value) {
        return value != null && REFERENCE_DEFAULT_SENTINEL.equalsIgnoreCase(String.valueOf(value).trim());
    }

    /**
     * Applies this definition's default value (when nothing was configured) and its value converter to a
     * resolved {@link ConfiguredValue}, returning the final typed value. Shared by the standard and the
     * reference-scoped resolution paths.
     */
    private ConfiguredValue<DataType> applyDefaultAndConvert(ConfiguredValue<?> configurationValue) {
        if (!configurationValue.found()) {
            // Use a local rather than writing the shared instance field on this read path.
            final DataType resolvedDefault = this.getDefaultValue();
            if (resolvedDefault != null) {
                DataType obfuscatedValue;
                if (valueObfuscator == null) {
                    obfuscatedValue = resolvedDefault;
                } else {
                    obfuscatedValue = valueObfuscator.obfuscate(resolvedDefault);
                }
                if (!loggedUsingDefault) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Configuration " + key + " is using the default value of " + obfuscatedValue);
                    loggedUsingDefault = true;
                }
                configurationValue.override(new DefaultValueProvider(resolvedDefault).getProvidedValue(key));
            }
        }

        final ProvidedValue providedValue = configurationValue.getProvidedValue();
        final Object originalValue = providedValue.getValue();
        try {
            final DataType finalValue =
               ConfigurationValueUtils.convertDataType(providedValue.getActualKey(), (DataType)originalValue, valueConverter);
            if (originalValue != finalValue) {
                configurationValue.override(new ConvertedValueProvider<>(finalValue, providedValue).getProvidedValue(key));
            }
            return (ConfiguredValue<DataType>) configurationValue;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("An invalid " + (providedValue.getSourceDescription().toLowerCase() + " value " + providedValue.getActualKey() + " detected: " + StringUtil.lowerCaseFirst(e.getMessage())), e);
        }
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
    public Class<DataType> getDataType() {
        return dataType;
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
     * A description of the default value. Defaults to {@link String#valueOf(Object)} of {@link #getDefaultValue()} but
     * can be explicitly with {@link CommandArgumentDefinition.Building#defaultValue(Object, String)}.
     */
    public String getDefaultValueDescription() {
        return defaultValueDescription;
    }

    /**
     * Returns true if this is configuration users are often interested in setting.
     * Used to simplify generated help by hiding less commonly used settings.
     */
    public boolean getCommonlyUsed() {
        return commonlyUsed;
    }

    /**
     * Return true if this configuration is for internal and/or programmatic use only.
     * End-user facing integrations should not expose internal configurations directly.
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * Return true if this configuration should not be printed to the console for any help command.
     */
    public boolean isHidden() {
        return hidden;
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

            return new Building<>(definition, defaultKeyPrefix);
        }
    }

    public static class Building<DataType> {

        private final ConfigurationDefinition<DataType> definition;
        private final String defaultKeyPrefix;

        private Building(ConfigurationDefinition<DataType> definition, String defaultKeyPrefix) {
            this.definition = definition;
            this.defaultKeyPrefix = defaultKeyPrefix;
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

        public Building<DataType> setDefaultValue(DataType defaultValue, String defaultValueDescription) {
            definition.defaultValue = defaultValue;
            definition.defaultValueDescription = defaultValueDescription;

            if (defaultValue != null && defaultValueDescription == null) {
                definition.defaultValueDescription = String.valueOf(defaultValue);
            }
            return this;

        }

        public Building<DataType> setDefaultValue(DataType defaultValue) {
            definition.defaultValue = defaultValue;
            return this;
        }

        public Building<DataType> setValueHandler(ConfigurationValueConverter<DataType> handler) {
            definition.valueConverter = handler;

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

        public Building<DataType> setInternal(boolean internal) {
            definition.internal = internal;

            return this;
        }

        public Building<DataType> setHidden(boolean hidden) {
            definition.hidden = hidden;

            return this;
        }

        /**
         * Marks this definition reference-scopable: while the reference connection of a two-connection command
         * (e.g. {@code diff}) is opening, it resolves from its {@code <namespace>.reference.<suffix>} sibling
         * instead of the primary key. An unset sibling inherits the primary value; a sibling of
         * {@link #REFERENCE_DEFAULT_SENTINEL} opts out (resolves as if unset). No effect on single-connection
         * commands or the primary connection.
         * <p>
         * Caveats: the reference value resolves only while the reference connection is opening, so read it
         * synchronously in {@code DatabaseConnection.open(...)} (a later/async read sees the primary value);
         * only the canonical key is scoped, not aliases; and it does not apply to auth activated by the
         * credential value itself (e.g. a {@code referencePassword} resolved by a {@link ConfiguredValueModifier}),
         * which is already per-connection. Intended for {@code String}-typed selectors: the
         * {@link #REFERENCE_DEFAULT_SENTINEL} opt-out is matched on the raw sibling value before type
         * conversion, so a non-{@code String} {@code DataType} (e.g. an enum) would not see the sentinel honored.
         */
        public Building<DataType> referenceScoped() {
            final String fullKey = definition.getKey();
            // define() always prepends "defaultKeyPrefix.", so the sibling is prefix + ".reference." + suffix.
            // Assert that invariant explicitly so a future change fails loudly instead of deriving a wrong key.
            final String expectedPrefix = defaultKeyPrefix + ".";
            if (!fullKey.startsWith(expectedPrefix)) {
                throw new IllegalStateException("referenceScoped() requires a key under the builder prefix '"
                        + defaultKeyPrefix + "', but was '" + fullKey + "'.");
            }
            final String suffix = fullKey.substring(expectedPrefix.length());
            definition.referenceKey = defaultKeyPrefix + ".reference." + suffix;

            return this;
        }

        public Building<DataType> addAliases(Collection<String> aliases) {
            for (String alias : aliases) {
                if (!alias.contains(".")) {
                    alias = defaultKeyPrefix + "." + alias;

                    addAliasKey(alias);
                }
            }
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
    static final class DefaultValueProvider extends AbstractConfigurationValueProvider {

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
    private static final class ConvertedValueProvider<DataType> extends AbstractConfigurationValueProvider {

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

