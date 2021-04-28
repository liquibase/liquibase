package liquibase.command;

import liquibase.plugin.Plugin;

import java.util.SortedSet;

/**
 * @deprecated Define command with the new {@link CommandStep} interface
 */
public interface LiquibaseCommand<T extends CommandResult> extends Plugin {

    String getName();

    int getPriority(String commandName);

    SortedSet<CommandArgument> getArguments();

    CommandValidationErrors validate();

    /**
     * Function that performs the actual logic. This should not be called directly by any code,
     * only by {@link CommandFactory#execute(LiquibaseCommand)}
     */
    T run() throws Exception;
}
