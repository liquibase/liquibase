package liquibase.command;

public abstract class AbstractCommand implements LiquibaseCommand {

    public final Object execute() throws CommandExecutionException {
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

    protected abstract Object run() throws Exception;
}
