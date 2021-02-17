package liquibase.command;

import java.util.Objects;

public class CommandArgumentDefinition<DataType> implements Comparable {

    private String name;
    private String description;
    private Class<DataType> dataType;
    private boolean required;
    private DataType defaultValue;

    protected CommandArgumentDefinition(String name, Class<DataType> type) {
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

    public CommandArgument<DataType> of(DataType value) {
        return new CommandArgument<>(this, value);
    }

    public DataType getValue(CommandScope commandScope) {
        return (DataType) commandScope.getValue(getName());
    }

    public CommandValidationErrors validate(CommandScope commandScope) {
        final DataType currentValue = getValue(commandScope);
        if (this.isRequired() && currentValue == null) {
            return new CommandValidationErrors("Missing required argument "+this.getName());
        }

        return null;
    }

    public static class Builder {

        public <DataType> NewCommandArgument<DataType> define(String name, Class<DataType> type) {
            return new NewCommandArgument<>(new CommandArgumentDefinition<>(name, type));
        }

        public static class NewCommandArgument<DataType> {
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
                return newCommandArgument;
            }

            public NewCommandArgument<DataType> defaultValue(DataType defaultValue) {
                newCommandArgument.defaultValue = defaultValue;

                return this;
            }
        }
    }
}
