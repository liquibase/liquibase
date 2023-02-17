package liquibase.command;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.DependencyUtil;
import liquibase.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
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
        computePipelineForCommandDefinition(commandDefinition);
        consolidateCommandArgumentsForCommand(commandDefinition);
        adjustCommandDefinitionForSteps(commandDefinition);

        return commandDefinition;
    }

    /**
     * Compute the pipeline for a given command. Takes into consideration all the dependencies required by the command
     * and other commands subscribed to it by getOrder.
     *
     * @param commandDefinition the CommandDefinition to compute the pipeline
     */
    private void computePipelineForCommandDefinition(CommandDefinition commandDefinition) {
        final Set<CommandStep> pipeline = new LinkedHashSet<>();
        // graph used to automatically sort the pipeline steps.
        DependencyUtil.DependencyGraph<CommandStep> pipelineGraph = new DependencyUtil.DependencyGraph<>(
                p -> { if (p != null) pipeline.add(p); }
        );

        Collection<CommandStep> allCommandStepInstances = findAllInstances();
        for (CommandStep step : allCommandStepInstances) {
            // order > 0 means is means that this CommandStep has been declared as part of this command
            if (step.getOrder(commandDefinition) > 0) {
                findDependenciesForCommand(pipelineGraph, allCommandStepInstances, step);
            }
        }
        pipelineGraph.computeDependencies();

        if (pipeline.isEmpty()) {
            throw new IllegalArgumentException("Unknown command '" + StringUtil.join(commandDefinition.getName(), " ") + "'");
        } else {
            pipeline.forEach(p -> {
                try {
                    commandDefinition.add(p.getClass().getConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
            });
        }
    }

    /**
     * Given a CommandStep step this method adds to the pipelineGraph all the CommandSteps that are providing the dependencies that it requires.
     */
    private void findDependenciesForCommand(DependencyUtil.DependencyGraph<CommandStep> pipelineGraph, Collection<CommandStep> allCommandStepInstances,
                                            CommandStep step) {
        if (step.requiredDependencies().isEmpty()) {
            pipelineGraph.add(null, step);
        } else {
            for (Class<?> d : step.requiredDependencies()) {
                CommandStep provider = whoProvidesClass(d, allCommandStepInstances);
                pipelineGraph.add(provider, step);
                findDependenciesForCommand(pipelineGraph, allCommandStepInstances, provider);
            }
        }
    }

    /**
     * Go through all command steps and find the step that provides the desired class.
     */
    private CommandStep whoProvidesClass(Class<?> dependency, Collection<CommandStep> allCommandStepInstances) {
        return allCommandStepInstances.stream().filter(cs -> cs.providedDependencies().contains(dependency))
                .reduce((a, b) -> {
                    throw new IllegalStateException(String.format("More than one CommandStep provides class %s. Steps: %s, %s",
                            dependency.getName(), a.getClass().getName(), b.getClass().getName()));
                })
                .orElseThrow(() -> new IllegalStateException("Unable to find CommandStep provider for class " +  dependency.getName()));
    }

    /**
     * Go through all the commandSteps on the pipeline and add their arguments to the commandDefinition arguments list.
     */
    private void consolidateCommandArgumentsForCommand(CommandDefinition commandDefinition) {
        final Set<CommandArgumentDefinition<?>> stepArguments = new HashSet<>();
        for (CommandStep step : commandDefinition.getPipeline()) {
            String[][] names = step.defineCommandNames();
            if (names != null) {
                for (String[] name : names) {
                    for (CommandArgumentDefinition<?> command : this.commandArgumentDefinitions.getOrDefault(StringUtil.join(name, " "), new HashSet<>())) {
                        // uses the most specialized version of the argument, allowing overrides
                        stepArguments.stream().filter(cad -> cad.getName().equals(command.getName())).findAny()
                                .ifPresent(stepArguments::remove);
                        stepArguments.add(command);
                    }
                }
            }
        }

        if (!stepArguments.isEmpty()) {
            for (CommandArgumentDefinition<?> commandArg : stepArguments) {
                commandDefinition.add(commandArg);
            }
        }
    }

    private void adjustCommandDefinitionForSteps(CommandDefinition commandDefinition) {
        for (CommandStep step : commandDefinition.getPipeline()) {
            step.adjustCommandDefinition(commandDefinition);
        }
    }

    /**
     * Returns all known {@link CommandDefinition}s.
     *
     * @param includeInternal if true, also include commands marked as {@link CommandDefinition#getInternal()}
     */
    public SortedSet<CommandDefinition> getCommands(boolean includeInternal) {
        Map<String, String[]> commandNames = new HashMap<>();
        for (CommandStep step : findAllInstances()) {
            String[][] names = step.defineCommandNames();
            if (names != null) {
                for (String[] name : names) {
                    commandNames.put(StringUtil.join(name, " "), name);
                }
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
