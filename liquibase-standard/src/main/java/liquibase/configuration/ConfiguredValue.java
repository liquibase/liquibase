package liquibase.configuration;

import liquibase.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This wraps all the {@link ProvidedValue}s to return the overall value returned from the collection of {@link ConfigurationValueProvider}s.
 * Returned by {@link LiquibaseConfiguration#getCurrentConfiguredValue(ConfigurationValueConverter, ConfigurationValueObfuscator, String...)}
 */
public class ConfiguredValue<DataType> {

    private static final NoValueProvider NO_VALUE_PROVIDER = new NoValueProvider();

    private final List<ProvidedValue> providedValues = new ArrayList<>();
    private final String key;
    private final ConfigurationValueObfuscator<DataType> valueObfuscator;
    private final ConfigurationValueConverter<DataType> valueConverter;

    protected ConfiguredValue(String key, ConfigurationValueConverter<DataType> converter, ConfigurationValueObfuscator<DataType> obfuscator) {
        this.key = key;
        this.valueObfuscator = obfuscator;

        if (converter == null) {
            this.valueConverter = (value -> (DataType) value);
        } else {
            this.valueConverter = converter;
        }
    }

    public DataType getValue() {
        final ProvidedValue providedValue = getProvidedValue();
        if (providedValue == null) {
            return null;
        }
        return
           ConfigurationValueUtils.convertDataType(providedValue.getActualKey(), (DataType)providedValue.getValue(), valueConverter);
    }

    public DataType getValueObfuscated() {
        final DataType rawValue = getValue();
        if (valueObfuscator != null) {
            return valueObfuscator.obfuscate(rawValue);
        }
        return rawValue;
    }

    /**
     * Returns the "winning" value across all the possible {@link ConfigurationValueProvider}.
     * A {@link ProvidedValue} is always returned, even if the value was not configured.
     *
     * @see #found()
     */
    public ProvidedValue getProvidedValue() {
        return getProvidedValues().get(0);
    }

    /**
     *
     * Return true if a default value was the "winning" value
     *
     * @return   boolean
     *
     */
    public boolean wasDefaultValueUsed() {
        ProvidedValue winningProvidedValue = getProvidedValue();
        return winningProvidedValue != null && winningProvidedValue.getProvider() instanceof ConfigurationDefinition.DefaultValueProvider;
    }

    /**
     * Replaces the current configured value with a higher-precedence one.
     * If a null value is passed, do nothing.
     */
    public void override(ProvidedValue details) {
        if (details == null) {
            return;
        }
        this.providedValues.add(0, details);
    }

    /**
     * Modifies the current configured value. If a new provider is overriding the old value, use {@link #override(ProvidedValue)}.
     * This is for changing the value outside the "provider" infrastructure.
     */
    public void override(Object newValue, String sourceDescription) {
        ProvidedValue thisValue = this.getProvidedValue();
        this.providedValues.add(0, new ProvidedValue(thisValue.getRequestedKey(), thisValue.getActualKey(), newValue, sourceDescription, thisValue.getProvider()));
    }

    /**
     * @return a full list of where the configuration value was set and/or overridden.
     */
    public List<ProvidedValue> getProvidedValues() {
        if (providedValues.size() == 0) {
            return Collections.singletonList(NO_VALUE_PROVIDER.getProvidedValue(new String[] {key}));
        }

        return Collections.unmodifiableList(providedValues);
    }

    /**
     * @return true if a value was found across the providers.
     */
    public boolean found() {
        return providedValues.size() > 0;
    }

    /**
     * Used to track configuration with no value set
     */
    private static final class NoValueProvider extends AbstractConfigurationValueProvider {
        @Override
        public int getPrecedence() {
            return -1;
        }

        @Override
        public ProvidedValue getProvidedValue(String... keyAndAliases) {
            return new ProvidedValue(keyAndAliases[0], keyAndAliases[0], null, "No configured value found", this);
        }
    }
}
