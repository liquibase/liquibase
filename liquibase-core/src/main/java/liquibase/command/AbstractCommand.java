package liquibase.command;

import liquibase.servicelocator.PrioritizedService;

import java.util.SortedSet;
import java.util.TreeSet;

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
