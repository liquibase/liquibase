package liquibase.command;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.StringUtil;

import java.util.*;

/**
 * Manages the command related implementations.
 */
public class CommandFactory implements SingletonObject {
    private Collection<CommandStep> allInstances;

    private final Map<String, Set<CommandArgumentDefinition<?>>> commandArgumentDefinitions = new HashMap<>();

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
        for (CommandStep step : findAllInstances()) {
            if (step.getOrder(commandDefinition) > 0) {
                commandDefinition.add(step);
            }
        }

        final List<CommandStep> pipeline = commandDefinition.getPipeline();
        if (pipeline.size() == 0) {
            throw new IllegalArgumentException("Unknown command '" + StringUtil.join(commandName, " ") + "'");
        }

        final Set<CommandArgumentDefinition<?>> stepArguments = this.commandArgumentDefinitions.get(StringUtil.join(commandDefinition.getName(), " "));

        if (stepArguments != null) {
            for (CommandArgumentDefinition<?> commandArg : stepArguments) {
                commandDefinition.add(commandArg);
            }
        }

        for (CommandStep step : pipeline) {
            step.adjustCommandDefinition(commandDefinition);
        }


        return commandDefinition;
    }

    /**
     * Returns all known {@link CommandDefinition}s.
     *
     * @param includeInternal if true, also include commands marked as {@link CommandDefinition#getInternal()}
     */
    public SortedSet<CommandDefinition> getCommands(boolean includeInternal) {
        Map<String, String[]> commandNames = new HashMap<>();
        for (CommandStep step : findAllInstances()) {
            for (String[] name : step.defineCommandNames()) {
                commandNames.put(StringUtil.join(name, " "), name);
            }
        }

        SortedSet<CommandDefinition> commands = new TreeSet<>();
        for (String[] commandName : commandNames.values()) {
            try {
                final CommandDefinition definition = getCommandDefinition(commandName);
                if (includeInternal || !definition.getInternal()) {
                    commands.add(definition);
                }
            } catch (IllegalArgumentException e) {
                //not a full command, like ConvertCommandStep
            }
        }

        return Collections.unmodifiableSortedSet(commands);

    }

    /**
     * Called by {@link CommandArgumentDefinition.Building#build()} to
     * register that a particular {@link CommandArgumentDefinition} is available for a command.
     */
    protected void register(String[] commandName, CommandArgumentDefinition<?> definition) {
        String commandNameKey = StringUtil.join(commandName, " ");
        if (!commandArgumentDefinitions.containsKey(commandNameKey)) {
            commandArgumentDefinitions.put(commandNameKey, new TreeSet<>());
        }

        if (commandArgumentDefinitions.get(commandNameKey).contains(definition)) {
           throw new IllegalArgumentException("Duplicate argument '" + definition.getName() + "' found for command '" + commandNameKey + "'");
        }
        this.commandArgumentDefinitions.get(commandNameKey).add(definition);
    }

    /**
     * Unregisters all information about the given {@link CommandStep}.
     * <bNOTE:</b> package-protected method used primarily for testing and may be removed or modified in the future.
     */
    protected void unregister(String[] commandName) {
        commandArgumentDefinitions.remove(StringUtil.join(commandName, " "));
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

    //
    // Find and cache all instances of CommandStep
    //
    private synchronized Collection<CommandStep> findAllInstances() {
        if (this.allInstances == null) {
            this.allInstances = new ArrayList<>();

            ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
            this.allInstances.addAll(serviceLocator.findInstances(CommandStep.class));
        }

        return this.allInstances;
    }
}
