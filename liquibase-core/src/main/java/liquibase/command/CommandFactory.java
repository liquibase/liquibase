package liquibase.command;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages {@link LiquibaseCommand} implementations.
 */
public class CommandFactory  {

    private static CommandFactory instance;

    private List<LiquibaseCommand> commands;

    private CommandFactory() {
        commands = new ArrayList<>();
        try {
            for (LiquibaseCommand command : Scope.getCurrentScope().getServiceLocator().findInstances(LiquibaseCommand.class)) {
                register(command);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public static synchronized CommandFactory getInstance() {
        if (instance == null) {
            instance = new CommandFactory();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = new CommandFactory();
    }


    public LiquibaseCommand getCommand(final String commandName) {

        Comparator<LiquibaseCommand> commandComparator = new Comparator<LiquibaseCommand>() {
            @Override
            public int compare(LiquibaseCommand o1, LiquibaseCommand o2) {
                return Integer.valueOf(o2.getPriority(commandName)).compareTo(o1.getPriority(commandName));
            }
        };


        List<LiquibaseCommand> sortedCommands = new ArrayList<>(commands);
        Collections.sort(sortedCommands, commandComparator);
        if (sortedCommands.isEmpty()) {
            throw new UnexpectedLiquibaseException("Could not find command class for "+commandName);
        }
        try {
            LiquibaseCommand command = sortedCommands.iterator().next().getClass().newInstance();

            if (command.getPriority(commandName) <= 0) {
                throw new UnexpectedLiquibaseException("Could not find command class for "+commandName);
            }
            return command;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void register(LiquibaseCommand command) {
        commands.add(command);
    }

    public void unregister(LiquibaseCommand command) {
        commands.remove(command);
    }

}
