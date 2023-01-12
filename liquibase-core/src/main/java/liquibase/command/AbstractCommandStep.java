package liquibase.command;

import liquibase.exception.CommandValidationException;

import java.util.Collections;
import java.util.List;

/**
 * Convenience base class for {@link CommandStep} implementations.
 */
public abstract class AbstractCommandStep implements CommandStep {

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.emptyList();
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.emptyList();
    }

    /**
     * Default implementation does no additional validation.
     */
    @Override
    public void validate(CommandScope commandScope) throws CommandValidationException {
    }

    /**
     * Default implementation makes no changes
     */
    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {

    }
}
