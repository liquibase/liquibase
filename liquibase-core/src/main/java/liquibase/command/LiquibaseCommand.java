package liquibase.command;

/**
 * Commands are higher-level functions. They may execute multiple {@link liquibase.change.Change}s and usually interact with the outside world.
 * Commands are different from Actions in that they implement end-user functionality rather than small pieces of logic.
 * We package functionaly as commands so that the command line interface as well as other integrations can all use the same business logic.
 */
public interface LiquibaseCommand<T extends CommandResult> {

    String getName();

    CommandValidationErrors validate();

    /**
     * Executes the command. Should call {@link #validate()} as part of this method and throw {@link CommandExecutionException} if validation fails or there are any errors executing the command.
     */
    T execute() throws CommandExecutionException;

    int getPriority(String commandName);

}