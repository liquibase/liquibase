package liquibase.command;

import liquibase.exception.CommandValidationException;
import liquibase.util.StringUtil;

import java.util.Arrays;

/**
 * Convenience base class for {@link CommandStep} implementations.
 */
public abstract class AbstractCommandStep implements CommandStep {

    /**
     * @return {@link #ORDER_DEFAULT} if the command scope's name matches {@link #getName()}. Otherwise {@link #ORDER_NOT_APPLICABLE}
     */
    @Override
    public int getOrder(CommandDefinition commandDefinition) {
        final String[] thisCommandName = getName();

        if ((thisCommandName != null) && StringUtil.join(Arrays.asList(thisCommandName), " ").equalsIgnoreCase(StringUtil.join(Arrays.asList(commandDefinition.getName()), " "))) {
            return ORDER_DEFAULT;
        } else {
            return ORDER_NOT_APPLICABLE;
        }
    }

    /**
     * Default implementation does no validation.
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
