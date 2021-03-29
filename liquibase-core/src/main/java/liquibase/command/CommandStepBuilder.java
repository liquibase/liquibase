package liquibase.command;

/**
 * Builder for configuring {@link CommandStep} settings, such as {@link CommandArgumentDefinition}s and {@link CommandResultDefinition}s
 */
public class CommandStepBuilder {

    private final Class<? extends CommandStep> commandStepClass;

    /**
     * Creates a builder for the given {@link CommandStep}
     */
    public CommandStepBuilder(Class<? extends CommandStep> commandStepClass) {
        this.commandStepClass = commandStepClass;
    }

    /**
     * Starts the building of a new {@link CommandArgumentDefinition}.
     */
    public <DataType> CommandArgumentDefinition.Building<DataType> argument(String name, Class<DataType> type) {
        return new CommandArgumentDefinition.Building<>(commandStepClass, new CommandArgumentDefinition<>(name, type));
    }

    /**
     * Starts the building of a new {@link CommandResultDefinition}.
     */
    public <DataType> CommandResultDefinition.Building<DataType> result(String name, Class<DataType> type) {
        return new CommandResultDefinition.Building<>(new CommandResultDefinition<>(name, type));
    }
}
