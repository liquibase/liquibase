package liquibase.command;

import liquibase.Scope;
import liquibase.exception.CommandArgumentValidationException;

import java.util.Objects;

public class CommandResultDefinition<DataType> implements Comparable {

    private String name;
    private String description;
    private Class<DataType> dataType;
    private boolean required;
    private DataType defaultValue;

    protected CommandResultDefinition(String name, Class<DataType> type) {
        this.name = name;
        this.dataType = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<DataType> getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public int compareTo(Object o) {
        return this.getName().compareTo(((CommandResultDefinition) o).getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandResultDefinition that = (CommandResultDefinition) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        String returnString = getName();

        if (required) {
            returnString += " (required)";
        }

        return returnString;
    }

    public CommandResult<DataType> of(DataType value) {
        return new CommandResult<>(this, value);
    }

    public DataType getValue(CommandScope commandScope) {
        return (DataType) commandScope.getResult(getName());
    }

    public static class Builder {

        private final Class<? extends LiquibaseCommand> commandClass;

        public Builder(Class<? extends LiquibaseCommand> commandClass) {
            this.commandClass = commandClass;
        }

        public <DataType> NewCommandResult<DataType> define(String name, Class<DataType> type) {
            return new NewCommandResult<>(new CommandResultDefinition<>(name, type));
        }

        public class NewCommandResult<DataType> {
            private final CommandResultDefinition<DataType> newCommandResult;

            public NewCommandResult(CommandResultDefinition<DataType> newCommandArgument) {
                this.newCommandResult = newCommandArgument;
            }


            public CommandResultDefinition<DataType> build() {
                return newCommandResult;
            }
        }
    }
}
