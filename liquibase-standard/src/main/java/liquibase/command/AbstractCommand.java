package liquibase.command;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @deprecated Implement commands with {@link liquibase.command.CommandStep} and call them with {@link liquibase.command.CommandFactory#getCommandDefinition(String...)}.
 */
public abstract class AbstractCommand<T extends CommandResult> implements LiquibaseCommand<T> {

    @Override
    public int getPriority(String commandName) {
        if ((commandName != null) && commandName.equalsIgnoreCase(getName())) {
            return PRIORITY_DEFAULT;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public SortedSet<CommandArgument> getArguments() {
        return new TreeSet<>();
    }
}
