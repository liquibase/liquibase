package liquibase.command.core.helpers;

import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;

/**
 * Class to hold all methods that are common to helper classes
 */
public abstract class AbstractHelperCommandStep extends AbstractCommandStep {

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        if (commandDefinition.getPipeline().size() == 1) {
            commandDefinition.setInternal(true);
        }
    }

}
