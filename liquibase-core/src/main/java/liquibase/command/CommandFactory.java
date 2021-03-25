package liquibase.command;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.util.StringUtil;

import java.util.*;

/**
 * Manages the command related implementations.
 */
public class CommandFactory implements SingletonObject {

    private final Map<Class<? extends CommandStep>, Set<CommandArgumentDefinition<?>>> argumentDefinitions = new HashMap<>();

    protected CommandFactory() {
    }

    /**
     * Returns the complete {@link CommandDefinition} for the given commandName.
     */
    protected CommandDefinition getCommand(String... commandName) {
        CommandDefinition commandDefinition = new CommandDefinition(commandName);

        for (CommandStep step : Scope.getCurrentScope().getServiceLocator().findInstances(CommandStep.class)) {
            if (step.getOrder(commandDefinition) > 0) {
                commandDefinition.add(step);
            }
        }

        for (CommandStep step : commandDefinition.getPipeline()) {
            final Set<CommandArgumentDefinition<?>> stepArguments = this.argumentDefinitions.get(step.getClass());

            if (stepArguments != null) {
                for (CommandArgumentDefinition<?> commandArg : stepArguments) {
                    commandDefinition.add(commandArg);
                }
            }
        }

        for (CommandStep step : commandDefinition.getPipeline()) {
            step.adjustCommandDefinition(commandDefinition);
        }


        return commandDefinition;
    }

    /**
     * Returns all known {@link CommandDefinition}s.
     */
    public SortedSet<CommandDefinition> getCommands() {
        Map<String, String[]> commandNames = new HashMap<>();
        for (CommandStep step : Scope.getCurrentScope().getServiceLocator().findInstances(CommandStep.class)) {
            final String[] name = step.getName();
            commandNames.put(StringUtil.join(name, " "), name);
        }

        SortedSet<CommandDefinition> commands = new TreeSet<>();
        for (String[] commandName : commandNames.values()) {
            commands.add(getCommand(commandName));
        }

        return Collections.unmodifiableSortedSet(commands);

    }

    /**
     * Called by {@link CommandStepBuilder.NewCommandArgument#build()} to
     * register that a particular {@link CommandArgumentDefinition} is available for a step.
     */
    protected void register(Class<? extends CommandStep> commandClass, CommandArgumentDefinition<?> definition) {
        if (!argumentDefinitions.containsKey(commandClass)) {
            argumentDefinitions.put(commandClass, new TreeSet<>());
        }

        this.argumentDefinitions.get(commandClass).add(definition);
    }

}
