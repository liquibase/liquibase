package liquibase.command;

import com.sun.deploy.util.StringUtils;
import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.exception.CommandExecutionException;

import java.util.*;

/**
 * Manages {@link LiquibaseCommand} implementations.
 */
public class CommandFactory implements SingletonObject {

    protected CommandFactory() {
    }

    public void execute(CommandScope commandScope) throws CommandExecutionException {
        SortedSet<LiquibaseCommand> commands = new TreeSet<>();

        for (LiquibaseCommand command : Scope.getCurrentScope().getServiceLocator().findInstances(LiquibaseCommand.class)) {
            if (command.getOrder(commandScope) > 0) {
                commands.add(command);
            }
        }

        if (commands.size() == 0) {
            throw new IllegalArgumentException("Unknown command: "+ StringUtils.join(Arrays.asList(commandScope.getCommand()), " "));
        }

        List<CommandArgumentDefinition> finalArgumentDefinitions = new ArrayList<>();
        for (LiquibaseCommand command : commands) {
            finalArgumentDefinitions.addAll(command.getArguments());
        }

        for (CommandArgumentDefinition definition : finalArgumentDefinitions) {
            CommandValidationErrors errors = definition.validate(commandScope);

            if (errors != null) {
                throw new IllegalArgumentException(errors.getError());
            }
        }

        try {
            for (LiquibaseCommand command : commands) {
                command.run(commandScope);
            }
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else {
                throw new CommandExecutionException(e);
            }
        }
    }
}
