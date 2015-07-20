package liquibase.command;

import liquibase.Scope;

public abstract class AbstractCommand<T extends CommandResult> implements LiquibaseCommand<T> {

    public final T execute(Scope scope) throws CommandExecutionException {
        this.validate();
        try {
            return this.run(scope);
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else {
                throw new CommandExecutionException(e);
            }
        }
    }

    protected abstract T run(Scope scope) throws Exception;
}
