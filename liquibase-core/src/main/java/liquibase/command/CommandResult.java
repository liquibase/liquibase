package liquibase.command;

public class CommandResult<DataType> {

    private final CommandResultDefinition definition;
    private final DataType value;

    public CommandResult(CommandResultDefinition<DataType> definition, DataType value) {
        this.definition = definition;
        this.value = value;
    }

    public CommandResultDefinition getDefinition() {
        return definition;
    }

    public DataType getValue() {
        return value;
    }
}
