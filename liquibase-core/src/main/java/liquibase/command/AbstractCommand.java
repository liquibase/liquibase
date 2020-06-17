package liquibase.command;

import liquibase.servicelocator.PrioritizedService;

public abstract class AbstractCommand<T extends CommandResult> implements LiquibaseCommand<T> {

    @Override
    public int getPriority(String commandName) {
        if ((commandName != null) && commandName.equalsIgnoreCase(getName())) {
            return PrioritizedService.PRIORITY_DEFAULT;
        } else {
            return -1;
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
}
