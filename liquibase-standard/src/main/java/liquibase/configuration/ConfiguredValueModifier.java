package liquibase.configuration;

import liquibase.plugin.Plugin;

/**
 * Provides a way for {@link LiquibaseConfiguration} to modify configured values.
 * After all the {@link ConfigurationValueProvider}s have been checked for a value, all registered {@link ConfiguredValueModifier}s are called in {@link #getOrder()} order.
 */
public interface ConfiguredValueModifier<DataType> extends Plugin {

    /**
     * Returns the order in which modifiers should be run. Modifiers with a higher order will run after modifiers with a lower order value.
     *
     * @return int
     */
    int getOrder();

    /**
     * Called to potentially override the given {@link ConfiguredValue}.
     * Implementations can use any information from the passed {@link ConfiguredValue}, including calling getProvidedValue() to determine keys used, format of the value, etc.
     * If an implementation wants to modify the value, it should call {@link ConfiguredValue#override(ProvidedValue)}
     */
    void override(ConfiguredValue<DataType> object);

    /**
     * Called to potentially override the given value.
     * @param value value to override
     * @return the overridden value if it was overridden, or the provided <code>value</code> otherwise
     */
    default String override(String value) {
        // create a simple ConfiguredValue wrapper
        ConfiguredValue valueWrapper = new ConfiguredValue(value, null, null);
        // set the current value in the wrapper
        valueWrapper.override(value, "argument");
        // call the value modifier to replace this value if appropriate
        override(valueWrapper);
        return String.valueOf(valueWrapper.getValue());
    }
}
