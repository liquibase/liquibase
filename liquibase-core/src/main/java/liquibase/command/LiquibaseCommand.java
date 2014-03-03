package liquibase.command;

public interface LiquibaseCommand {

    String getName();

    CommandValidationErrors validate();

    Object execute() throws CommandExecutionException;
}
