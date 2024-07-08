package liquibase.configuration;

import liquibase.command.CommandScope;

/**
 * Convenience base class for {@link ConfigurationValueProvider} implementations
 */
public abstract class AbstractConfigurationValueProvider implements ConfigurationValueProvider {

    /**
     * Default implementation does no checking
     */
    @Override
    public void validate(CommandScope commandScope) throws IllegalArgumentException {

    }
}
