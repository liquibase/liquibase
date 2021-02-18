package liquibase.command;

import liquibase.Scope;
import liquibase.exception.CommandArgumentValidationException;

import java.util.Objects;

public class CommandArgumentDefinition<DataType> implements Comparable {

    private String name;
    private String description;
    private Class<DataType> dataType;
    private boolean required;
    private DataType defaultValue;

    protected CommandArgumentDefinition(String name, Class<DataType> type) {
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
        return this.getName().compareTo(((CommandArgumentDefinition) o).getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandArgumentDefinition that = (CommandArgumentDefinition) o;
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

    public CommandArgument<DataType> of(DataType value) {
        return new CommandArgument<>(this, value);
    }

    public DataType getValue(CommandScope commandScope) {
        final DataType value = (DataType) commandScope.getArgumentValue(getName());
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    public void validate(CommandScope commandScope) throws CommandArgumentValidationException {
        final DataType currentValue = getValue(commandScope);
        if (this.isRequired() && currentValue == null) {
            throw new CommandArgumentValidationException(this.getName(), "missing required argument");
        }
    }

    public static class Builder {

        private final Class<? extends LiquibaseCommand> commandClass;

        public Builder(Class<? extends LiquibaseCommand> commandClass) {
            this.commandClass = commandClass;
        }

        public <DataType> NewCommandArgument<DataType> define(String name, Class<DataType> type) {
            return new NewCommandArgument<>(new CommandArgumentDefinition<>(name, type));
        }

        public class NewCommandArgument<DataType> {
            private final CommandArgumentDefinition<DataType> newCommandArgument;

            public NewCommandArgument(CommandArgumentDefinition<DataType> newCommandArgument) {
                this.newCommandArgument = newCommandArgument;
            }

            public NewCommandArgument<DataType> required() {
                this.newCommandArgument.required = true;

                return this;
            }

            public NewCommandArgument<DataType> optional() {
                this.newCommandArgument.required = false;

                return this;
            }

            public CommandArgumentDefinition<DataType> build() {
                Scope.getCurrentScope().getSingleton(CommandFactory.class).register(commandClass, newCommandArgument);

                return newCommandArgument;
            }

            public NewCommandArgument<DataType> defaultValue(DataType defaultValue) {
                newCommandArgument.defaultValue = defaultValue;

                return this;
            }
        }
    }
}
