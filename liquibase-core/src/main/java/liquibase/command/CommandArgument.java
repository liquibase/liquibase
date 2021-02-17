package liquibase.command;

public class CommandArgument<DataType> {

    private final CommandArgumentDefinition definition;
    private final DataType value;

    public CommandArgument(CommandArgumentDefinition<DataType> definition, DataType value) {
        this.definition = definition;
        this.value = value;
    }

    public CommandArgumentDefinition getDefinition() {
        return definition;
    }

    public DataType getValue() {
        return value;
    }
}
