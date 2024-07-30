package liquibase.command.core.helpers;

import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;
import liquibase.util.HelpUtil;

import java.util.List;

public abstract class AbstractArgumentCommandStep extends AbstractCommandStep {

    @Override
    public final void adjustCommandDefinition(CommandDefinition commandDefinition) {
        HelpUtil.hideCommandNameInHelpView(commandDefinition);
    }

    @Override
    public abstract List<Class<?>> providedDependencies();
}
