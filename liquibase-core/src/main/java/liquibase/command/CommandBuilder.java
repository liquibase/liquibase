package liquibase.command;

import liquibase.Beta;
import liquibase.command.core.InternalDatabaseCommandStep;
import liquibase.database.Database;

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
     * Starts the building of a new {@link CommandArgumentDefinition}.
     */
    public <DataType> CommandArgumentDefinition.Building<DataType> argument(CommonArgumentNames argument, Class<DataType> type) {
        return new CommandArgumentDefinition.Building<>(commandNames, new CommandArgumentDefinition<>(argument.getArgumentName(), type));
    }


    /**
     * Creates a database argument and registers the current Step as an applicable command
     * to the InternalDatabaseCommandStep.
     *
     * @deprecated Beta approach, may be subject to change in future releases
     */
    @Beta
    public CommandArgumentDefinition.Building<Database> databaseArgument() {
        InternalDatabaseCommandStep.addApplicableCommand(this.commandNames);
        return new CommandArgumentDefinition.Building<>(commandNames,
                new CommandArgumentDefinition<>("database", Database.class))
                .description("Database connection");
    }

    /**
     * Starts the building of a new {@link CommandResultDefinition}.
     */
    public <DataType> CommandResultDefinition.Building<DataType> result(String name, Class<DataType> type) {
        return new CommandResultDefinition.Building<>(new CommandResultDefinition<>(name, type));
    }
}
