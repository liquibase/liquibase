package liquibase.command;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.DependencyUtil;
import liquibase.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages the command related implementations.
 */
public class CommandFactory implements SingletonObject {
    private Collection<CommandStep> allInstances;
    /**
     * A cache of all found command names and their corresponding command definition.
     */
    private static final Map<String, CommandDefinition> COMMAND_DEFINITIONS = new ConcurrentHashMap<>();
    /**
     * A cache of all found CommandStep classes and their corresponding override CommandSteps.
     * Multiple overrides per CommandStep are allowed.
     */
    private Map<Class<? extends CommandStep>, List<CommandStep>> commandOverrides;


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
        String commandNameKey = StringUtil.join(commandName, " ");
        CommandDefinition commandDefinition = COMMAND_DEFINITIONS.get(commandNameKey);
        if (commandDefinition == null) { //Check if we have already computed arguments, dependencies, pipeline and adjusted definition
            commandDefinition = new CommandDefinition(commandName);
            computePipelineForCommandDefinition(commandDefinition);
            consolidateCommandArgumentsForCommand(commandDefinition);
            adjustCommandDefinitionForSteps(commandDefinition);
            COMMAND_DEFINITIONS.put(commandNameKey, commandDefinition);
        }
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
        Map<Class<? extends CommandStep>, List<CommandStep>> overrides = findAllOverrides(allCommandStepInstances);
        for (CommandStep step : allCommandStepInstances) {
            // Skip override steps - they are added when processing their base step
            if (step.getClass().isAnnotationPresent(CommandOverride.class)) {
                continue;
            }

            // order > 0 means is means that this CommandStep has been declared as part of this command
            if (step.getOrder(commandDefinition) > 0) {
                // Add all override steps for this base step first
                List<CommandStep> overrideSteps = overrides.get(step.getClass());
                if (overrideSteps != null && !overrideSteps.isEmpty()) {
                    // Add override steps with their dependencies
                    for (CommandStep overrideStep : overrideSteps) {
                        findDependenciesForCommand(pipelineGraph, allCommandStepInstances, overrideStep, overrides);
                    }
                    // Make base step depend on the first override (ensures overrides run before base)
                    pipelineGraph.add(overrideSteps.get(0), step);
                    findDependenciesForCommand(pipelineGraph, allCommandStepInstances, step, overrides);
                } else {
                    // No overrides, just add the base step normally
                    findDependenciesForCommand(pipelineGraph, allCommandStepInstances, step, overrides);
                }
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
                                            CommandStep step, Map<Class<? extends CommandStep>, List<CommandStep>> overrides) {
        if (step.requiredDependencies() == null || step.requiredDependencies().isEmpty()) {
            pipelineGraph.add(null, step);
        } else {
            for (Class<?> d : step.requiredDependencies()) {
                CommandStep provider = whoProvidesClass(d, allCommandStepInstances, overrides);
                pipelineGraph.add(provider, step);
                findDependenciesForCommand(pipelineGraph, allCommandStepInstances, provider, overrides);
            }
        }
    }

    /**
     * Go through all command steps and find the step that provides the desired class, ignoring the overrides.
     */
    private CommandStep whoProvidesClass(Class<?> dependency, Collection<CommandStep> allCommandStepInstances, Map<Class<? extends CommandStep>, List<CommandStep>> overrides) {
        return allCommandStepInstances.stream().filter(cs -> {
            if (cs.providedDependencies() == null || !cs.providedDependencies().contains(dependency)) {
                return false;
            }
            // Ignore override steps
            for (List<CommandStep> overrideList : overrides.values()) {
                if (overrideList.contains(cs)) {
                    return false;
                }
            }
            return true;
        })
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
        boolean allInternal = true;
        for (CommandStep step : commandDefinition.getPipeline()) {
            step.adjustCommandDefinition(commandDefinition);
            allInternal = step.isInternal() && allInternal;
        }
        commandDefinition.setInternal(allInternal);
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
        if (definition.isRequired() && definition.getDefaultValue() != null) {
            throw new IllegalArgumentException("Argument '" + definition.getName() + "' for command '" + commandNameKey + "' has both a default value and the isRequired flag set to true. Arguments with default values cannot be marked as required.");
        }
        this.commandArgumentDefinitions.get(commandNameKey).add(definition);
        CommandDefinition commandDefinition = COMMAND_DEFINITIONS.get(commandNameKey);
        if (commandDefinition != null){
            commandDefinition.add(definition);
        }
    }

    /**
     * Unregisters all information about the given {@link CommandStep}.
     * <bNOTE:</b> package-protected method used primarily for testing and may be removed or modified in the future.
     */
    protected void unregister(String[] commandName) {
        String commandNameKey = StringUtil.join(commandName, " ");
        commandArgumentDefinitions.remove(commandNameKey);
        COMMAND_DEFINITIONS.remove(commandNameKey);
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

    /**
     *
     * Reset the COMMAND_DEFINITIONS cache.  Added for tests.
     *
     * @return
     *
     */
    public void resetCommandDefinitions() {
        COMMAND_DEFINITIONS.clear();
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

    /**
     * Find all commands that override other commands based on {@link CommandOverride#override()}.
     * Multiple overrides are allowed - they will be filtered at runtime in CommandScope.execute().
     * @param allCommandSteps all commands found during runtime
     * @return a map with key of the CommandStep intended to override and value of list of overriding command steps
     */
    private Map<Class<? extends CommandStep>, List<CommandStep>> findAllOverrides(Collection<CommandStep> allCommandSteps) {
        if (commandOverrides == null) {
            Map<Class<? extends CommandStep>, List<CommandStep>> overrides = new HashMap<>();
            allCommandSteps.stream()
                    .filter(commandStep -> commandStep.getClass().isAnnotationPresent(CommandOverride.class))
                    .forEach(overrideStep -> {
                        Class<? extends CommandStep> classToOverride = overrideStep.getClass().getAnnotation(CommandOverride.class).override();
                        overrides.computeIfAbsent(classToOverride, val -> new ArrayList<>()).add(overrideStep);
                    });
            validateOverrides(overrides);
            this.commandOverrides = overrides;
        }
        return commandOverrides;
    }

    /**
     * Validates override configuration. Multiple overrides are allowed - they will be filtered at runtime based on database support.
     * @param overrides the list of overrides
     */
    private void validateOverrides(Map<Class<? extends CommandStep>, List<CommandStep>> overrides) {
        // Multiple overrides are now allowed
        // Runtime filtering in CommandScope.execute() will ensure only appropriate overrides run
    }

}
