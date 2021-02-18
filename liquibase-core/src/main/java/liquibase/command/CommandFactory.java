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

    private Map<Class<? extends LiquibaseCommand>, SortedSet<CommandArgumentDefinition>> commandArgumentDefinitions = new HashMap<>();

    protected CommandFactory() {
    }

    public void execute(CommandScope commandScope) throws CommandExecutionException {
        SortedSet<LiquibaseCommand> commands = getCommands(commandScope);

        if (commands.size() == 0) {
            throw new CommandExecutionException("Unknown command: "+ StringUtils.join(Arrays.asList(commandScope.getCommand()), " "));
        }

        Set<CommandArgumentDefinition> finalArgumentDefinitions = getArguments(commands);

        for (CommandArgumentDefinition definition : finalArgumentDefinitions) {
            definition.validate(commandScope);
        }

        for (LiquibaseCommand command : commands) {
            command.validate(commandScope);
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

    private SortedSet<LiquibaseCommand> getCommands(CommandScope commandScope) {
        SortedSet<LiquibaseCommand> commands = new TreeSet<>((o1, o2) -> {
            final int order = Integer.compare(o1.getOrder(commandScope), o2.getOrder(commandScope));
            if (order == 0) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }

            return order;
        });

        for (LiquibaseCommand command : Scope.getCurrentScope().getServiceLocator().findInstances(LiquibaseCommand.class)) {
            if (command.getOrder(commandScope) > 0) {
                commands.add(command);
            }
        }
        return commands;
    }

    private SortedSet<CommandArgumentDefinition> getArguments(SortedSet<LiquibaseCommand> commands) {
        SortedSet<CommandArgumentDefinition> finalArgumentDefinitions = new TreeSet<>();
        for (LiquibaseCommand command : commands) {
            finalArgumentDefinitions.addAll(getArguments(command));
        }
        return finalArgumentDefinitions;
    }

    public void register(Class<? extends LiquibaseCommand> commandClass, CommandArgumentDefinition definition) {
        if (!commandArgumentDefinitions.containsKey(commandClass)) {
            commandArgumentDefinitions.put(commandClass, new TreeSet<>());
        }

        this.commandArgumentDefinitions.get(commandClass).add(definition);
    }

    public SortedSet<CommandArgumentDefinition> getArguments(LiquibaseCommand command) {
        final SortedSet<CommandArgumentDefinition> definitions = commandArgumentDefinitions.get(command.getClass());
        if (definitions == null) {
            return null;
        }
        return Collections.unmodifiableSortedSet(definitions);
    }
}
