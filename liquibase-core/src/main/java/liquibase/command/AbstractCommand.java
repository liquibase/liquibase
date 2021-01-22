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

    public final T execute() throws CommandExecutionException {
        this.validate();
        try {
            return this.run();
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else {
                throw new CommandExecutionException(e);
            }
        }
    }

    protected abstract T run() throws Exception;

    @Override
    public SortedSet<CommandArgument> getArguments() {
        return new TreeSet<>();
    }
}
