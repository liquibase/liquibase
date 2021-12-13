package liquibase.command;

import liquibase.Scope;

import java.util.List;

/**
 * Builder for configuring {@link CommandStep} settings, such as {@link CommandArgumentDefinition}s and {@link CommandResultDefinition}s
 */
public class CommandBuilder {

    private final String[][] commandNames;

    /**
     * Creates a builder for the given command name
     */
    public CommandBuilder(String[]... commandNames) {
        this.commandNames = commandNames;
    }

    /**
     * Starts the building of a new {@link CommandArgumentDefinition}.
     */
    public <DataType> CommandArgumentDefinition.Building<DataType> argument(String name, Class<DataType> type) {
        return new CommandArgumentDefinition.Building<>(commandNames, new CommandArgumentDefinition<>(name, type));
    }

    /**
     * Starts the building of a new {@link CommandResultDefinition}.
     */
    public <DataType> CommandResultDefinition.Building<DataType> result(String name, Class<DataType> type) {
        return new CommandResultDefinition.Building<>(new CommandResultDefinition<>(name, type));
    }

    /**
     * Register the specified prompt order as the order that should be used for interactive prompting.
     */
    public void interactivePromptOrder(List<CommandArgumentDefinition<?>> promptOrder) {
        for (String[] commandName : commandNames) {
            Scope.getCurrentScope().getSingleton(CommandFactory.class).registerInteractivePromptOrder(commandName, promptOrder);
        }
    }
}
