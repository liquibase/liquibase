package liquibase.command;

import lombok.Getter;

import java.util.Objects;

/**
 * Defines a known, type-safe result from a specific {@link CommandStep}.
 * <p>
 * Because this definition is tied to a specific step, multiple steps in a pipeline can define results of the same name.
 *
 * @see CommandBuilder#result(String, Class) for constructing new instances.
 */
@Getter
public class CommandResultDefinition<DataType> implements Comparable<CommandResultDefinition<?>> {

    /**
     * -- GETTER --
     *  The name of the result. Must be camelCase and alphanumeric.
     */
    private final String name;
    /**
     * -- GETTER --
     *  The description of the result. Used in generated help documentation.
     */
    private String description;
    /**
     * -- GETTER --
     *  The datatype of the result.
     */
    private final Class<DataType> dataType;
    /**
     * -- GETTER --
     *  The default value to use if no value was provided.
     */
    private DataType defaultValue;

    protected CommandResultDefinition(String name, Class<DataType> type) {
        this.name = name;
        this.dataType = type;
    }

    @Override
    public int compareTo(CommandResultDefinition<?> o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandResultDefinition<?> that = (CommandResultDefinition<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return getName();
    }

    public static class Building<DataType> {
        private final CommandResultDefinition<DataType> newCommandResult;

        Building(CommandResultDefinition<DataType> newCommandResult) {
            this.newCommandResult = newCommandResult;
        }

        /**
         * Sets the description for this result.
         */
        public Building<DataType> description(String description) {
            this.newCommandResult.description = description;

            return this;
        }

        /**
         * Set the default value for this result.
         */
        public Building<DataType> defaultValue(DataType defaultValue) {
            newCommandResult.defaultValue = defaultValue;

            return this;
        }

        /**
         * Complete construction and register the definition with the rest of the system.
         */
        public CommandResultDefinition<DataType> build() {
            return newCommandResult;
        }

    }

}
