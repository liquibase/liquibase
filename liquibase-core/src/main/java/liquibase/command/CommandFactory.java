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

    /**
     * @deprecated. Use {@link Scope#getSingleton(Class)}
     */
    public static CommandFactory getInstance() {
        return Scope.getCurrentScope().getSingleton(CommandFactory.class);
    }

    protected CommandFactory() {
    }

    /**
     * Returns the complete {@link CommandDefinition} for the given commandName.
     *
     * @throws IllegalArgumentException if the commandName is not known
     */
    public CommandDefinition getCommandDefinition(String... commandName) throws IllegalArgumentException{
        CommandDefinition commandDefinition = new CommandDefinition(commandName);

        for (CommandStep step : Scope.getCurrentScope().getServiceLocator().findInstances(CommandStep.class)) {
            if (step.getOrder(commandDefinition) > 0) {
                commandDefinition.add(step);
            }
        }

        final List<CommandStep> pipeline = commandDefinition.getPipeline();
        if (pipeline.size() == 0) {
            throw new IllegalArgumentException("Unknown command '" + StringUtil.join(commandName, " ") + "'");
        }

        for (CommandStep step : pipeline) {
            final Set<CommandArgumentDefinition<?>> stepArguments = this.argumentDefinitions.get(step.getClass());

            if (stepArguments != null) {
                for (CommandArgumentDefinition<?> commandArg : stepArguments) {
                    commandDefinition.add(commandArg);
                }
            }
        }

        for (CommandStep step : pipeline) {
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
            try {
                commands.add(getCommandDefinition(commandName));
            } catch (IllegalArgumentException e) {
                //not a full command, like ConvertCommandStep
            }
        }

        return Collections.unmodifiableSortedSet(commands);

    }

    /**
     * Called by {@link CommandArgumentDefinition.Building#build()} to
     * register that a particular {@link CommandArgumentDefinition} is available for a step.
     */
    protected void register(Class<? extends CommandStep> commandClass, CommandArgumentDefinition<?> definition) {
        if (!argumentDefinitions.containsKey(commandClass)) {
            argumentDefinitions.put(commandClass, new TreeSet<>());
        }

        this.argumentDefinitions.get(commandClass).add(definition);
    }

    /**
     * Unregisters all information about the given {@link CommandStep}.
     * <bNOTE:</b> package-protected method used primarily for testing and may be removed or modified in the future.
     */
    protected void unregister(Class<? extends CommandStep> commandStepClass) {
        argumentDefinitions.remove(commandStepClass);
    }

    /**
     * @deprecated use {@link #getCommandDefinition(String...)}
     */
    public LiquibaseCommand getCommand(String commandName) {
        return Scope.getCurrentScope().getSingleton(LiquibaseCommandFactory.class).getCommand(commandName);
    }

    /**
     * @deprecated Use {@link CommandScope#execute()}
     */
    public <T extends CommandResult> T execute(LiquibaseCommand<T> command) throws CommandExecutionException {
        command.validate();
        try {
            return command.run();
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else {
                throw new CommandExecutionException(e);
            }
        }

    }
}
