package liquibase.command;

import liquibase.Scope;

public interface LiquibaseCommand<T extends CommandResult> {

    String getName();

    CommandValidationErrors validate();

    T execute(Scope scope) throws CommandExecutionException;
}
