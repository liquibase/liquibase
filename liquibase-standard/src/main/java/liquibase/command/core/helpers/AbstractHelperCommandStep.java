package liquibase.command.core.helpers;

import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;

/**
 * Class to hold all methods that are common to helper classes
 */
public abstract class AbstractHelperCommandStep extends AbstractCommandStep {

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        // Some helper classes require other helpers (as dependencies). We only want to hide entire pipelines where all the commands are helpers.
        boolean allHelpers = commandDefinition.getPipeline().stream().allMatch(cs -> cs instanceof AbstractHelperCommandStep);

        if (allHelpers) {
            commandDefinition.setHidden(true);
        }
    }

}
