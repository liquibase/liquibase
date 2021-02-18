package liquibase.command;

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

    protected SortedSet<LiquibaseCommand> getCommandPipeline(CommandScope commandScope) {
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

    public void register(Class<? extends LiquibaseCommand> commandClass, CommandArgumentDefinition definition) {
        if (!commandArgumentDefinitions.containsKey(commandClass)) {
            commandArgumentDefinitions.put(commandClass, new TreeSet<>());
        }

        this.commandArgumentDefinitions.get(commandClass).add(definition);
    }

    public SortedSet<CommandArgumentDefinition> getArguments(LiquibaseCommand command) {
        final SortedSet<CommandArgumentDefinition> definitions = commandArgumentDefinitions.get(command.getClass());
        if (definitions == null) {
            return Collections.emptySortedSet();
        }
        return Collections.unmodifiableSortedSet(definitions);
    }
}
