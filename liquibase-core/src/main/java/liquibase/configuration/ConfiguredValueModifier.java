package liquibase.configuration;

import liquibase.plugin.Plugin;

/**
 * Provides a way for {@link LiquibaseConfiguration} to modify configured values.
 */
public interface ConfiguredValueModifier extends Plugin {

    /**
     * Returns the priority in which values should be modified. Modifiers with a higher priority will overwrite values
     * from lower priority modifiers.
     *
     * @return int
     */
    int getPriority();

    /**
     * Modify a Configuration Value
     *
     * @param object
     * @return
     */
    Object modify(ProvidedValue object);
}
