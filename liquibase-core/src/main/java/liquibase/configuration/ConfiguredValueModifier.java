package liquibase.configuration;

import liquibase.command.CommandArgumentDefinition;
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
     * Called to potentially override the given {@link ConfiguredValue}. This method will be called twice, once during the validation phase of a command step from {@link CommandArgumentDefinition#validate(liquibase.command.CommandScope)}, and once during the execution phase of a command step.
     * Implementations can use any information from the passed {@link ConfiguredValue}, including calling getProvidedValue() to determine keys used, format of the value, etc.
     * If an implementation wants to modify the value, it should call {@link ConfiguredValue#override(ProvidedValue)}
     */
    void override(ConfiguredValue<DataType> object);
}
