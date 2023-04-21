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
    private final Map<String[], CommandDefinition> commandDefinitions = new ConcurrentHashMap<>();
    /**
     * A cache of all found CommandStep classes and their corresponding override CommandStep.
     */
    private Map<Class<? extends CommandStep>, CommandStep> commandOverrides;


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
        CommandDefinition commandDefinition = commandDefinitions.get(commandName);
        if (commandDefinition == null) { //Check if we have already computed arguments, dependencies, pipeline and adjusted definition
            commandDefinition = new CommandDefinition(commandName);
            computePipelineForCommandDefinition(commandDefinition);
            consolidateCommandArgumentsForCommand(commandDefinition);
            adjustCommandDefinitionForSteps(commandDefinition);
            commandDefinitions.put(commandName, commandDefinition);
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
        Map<Class<? extends CommandStep>, CommandStep> overrides = findAllOverrides(allCommandStepInstances);
        for (CommandStep step : allCommandStepInstances) {
            // order > 0 means is means that this CommandStep has been declared as part of this command
            if (step.getOrder(commandDefinition) > 0) {
                Optional<CommandStep> overrideStep = getOverride(overrides, step);
                findDependenciesForCommand(pipelineGraph, allCommandStepInstances, overrideStep.orElse(step));
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
        if (step.requiredDependencies() == null || step.requiredDependencies().isEmpty()) {
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
        return allCommandStepInstances.stream().filter(cs -> cs.providedDependencies() != null && cs.providedDependencies().contains(dependency))
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

    /**
     * Find all commands that override other commands based on {@link CommandOverride#override()}.
     * Validates that only a single command is overriding another.
     * @param allCommandSteps all commands found during runtime
     * @return a map with key of the CommandStep intended to override and value of the valid overriding command step
     * @throws RuntimeException if more than one command step overrides another command step
     */
    private Map<Class<? extends CommandStep>, CommandStep> findAllOverrides(Collection<CommandStep> allCommandSteps) throws RuntimeException {
        if (commandOverrides == null) { //If we have not already found any overrides
            Map<Class<? extends CommandStep>, List<CommandStep>> overrides = new HashMap<>();
            allCommandSteps.stream()
                    .filter(commandStep -> commandStep.getClass().isAnnotationPresent(CommandOverride.class))
                    .forEach(overrideStep -> {
                        Class<? extends CommandStep> classToOverride = overrideStep.getClass().getAnnotation(CommandOverride.class).override();
                        overrides.computeIfAbsent(classToOverride, val -> new ArrayList<>()).add(overrideStep);
                    });
            validateOverrides(overrides);
            Map<Class<? extends CommandStep>, CommandStep> validOverrides = new HashMap<>();
            overrides.forEach((overriddenClass, validOverride) -> validOverride.stream().findFirst().ifPresent(valid -> validOverrides.put(overriddenClass, valid)));
            this.commandOverrides = validOverrides;
        }
        return commandOverrides;
    }

    /**
     * Validates that for all overrides, there is only a single override per command step. Logs all invalid overrides found.
     * @param overrides the list of overrides
     * @throws RuntimeException if more than one command step overrides a command step
     */
    private void validateOverrides(Map<Class<? extends CommandStep>, List<CommandStep>> overrides) throws RuntimeException {
        Map<Class<? extends CommandStep>, List<CommandStep>> invalidOverrides = new HashMap<>();
        overrides.forEach((step, overrideSteps) -> {
            if (overrideSteps.size() > 1) {
                invalidOverrides.put(step, overrideSteps);
            }
        });
        invalidOverrides.forEach((step, overrideSteps) -> {
            Scope.getCurrentScope().getLog(getClass()).severe(String.format("Found multiple command steps overriding %s! A command may have at most one override. Invalid overrides include: %s",
                    step.getSimpleName(),
                    overrideSteps.stream().map(ovrr -> ovrr.getClass().getSimpleName()).collect(Collectors.joining(", "))));
        });
        if (!invalidOverrides.isEmpty()) {
            throw new RuntimeException(String.format("Found more than one CommandOverride for CommandStep(s): %s! A command may have at most one override.", invalidOverrides.keySet().stream().map(Class::getSimpleName).collect(Collectors.joining(", "))));
        }
    }

    /**
     * Get the override for a given CommandStep.
     * @param overrides the list of overrides
     * @param step the step to check for overrides
     * @return an optional containing the CommandStep override if present
     */
    private Optional<CommandStep> getOverride(Map<Class<? extends CommandStep>, CommandStep> overrides, CommandStep step) {
        CommandStep overrideStep = overrides.get(step.getClass());
        if (overrideStep != null) {
            if (overrideStep.getClass() != step.getClass()) {
                Scope.getCurrentScope().getLog(getClass()).fine(String.format("Found %s override for %s! Using %s in pipeline.", overrideStep.getClass().getSimpleName(), step.getClass().getSimpleName(), overrideStep.getClass().getSimpleName()));
                return Optional.of(overrideStep);
            }
        }
        return Optional.empty();
    }
}
