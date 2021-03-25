package liquibase.command;

import liquibase.Scope;
import liquibase.exception.CommandArgumentValidationException;

import java.util.Objects;

/**
 * Defines a known, type-safe argument for a specific {@link CommandStep}.
 * Includes metadata about the argument such as a description, if it is required, a default value, etc.
 * <p>
 * Because this definition is tied to a specific step, multiple steps in a pipeline can define arguments of the same name.
 *
 * @see CommandStepBuilder#argument(String, Class) for constructing new instances.
 */
public class CommandArgumentDefinition<DataType> implements Comparable<CommandArgumentDefinition<?>> {

    private final String name;
    private final Class<DataType> dataType;

    private String description;
    private boolean required;
    private DataType defaultValue;

    protected CommandArgumentDefinition(String name, Class<DataType> type) {
        this.name = name;
        this.dataType = type;
    }

    /**
     * The name of the argument. Must be camelCase alphanumeric.
     */
    public String getName() {
        return name;
    }

    /**
     * The description of the argument. Used in generated help documentation.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The datatype this argument will return.
     */
    public Class<DataType> getDataType() {
        return dataType;
    }

    /**
     * Whether this argument is required. Exposed as a separate setting for help doc purposes.
     * {@link #validate(CommandScope)} will ensure required values are set.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Validates that the value stored in the given {@link CommandScope} is valid.
     *
     * @throws CommandArgumentValidationException if the stored value is not valid.
     */
    public void validate(CommandScope commandScope) throws CommandArgumentValidationException {
        final DataType currentValue = commandScope.getArgumentValue(this);
        if (this.isRequired() && currentValue == null) {
            throw new CommandArgumentValidationException(this.getName(), "missing required argument");
        }
    }

    @Override
    public int compareTo(CommandArgumentDefinition<?> o) {
        return this.getName().compareTo(o.getName());
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

    /**
     * A new {@link CommandArgumentDefinition} under construction from {@link CommandStepBuilder}
     */
    public static class UnderConstruction<DataType> {
        private final Class<? extends CommandStep> commandStepClass;
        private final CommandArgumentDefinition<DataType> newCommandArgument;

        UnderConstruction(Class<? extends CommandStep> commandStepClass, CommandArgumentDefinition<DataType> newCommandArgument) {
            this.commandStepClass = commandStepClass;
            this.newCommandArgument = newCommandArgument;
        }

        /**
         * Mark argument as required.
         * @see #optional()
         */
        public UnderConstruction<DataType> required() {
            this.newCommandArgument.required = true;

            return this;
        }

        /**
         * Mark argument as optional.
         * @see #required()
         */
        public UnderConstruction<DataType> optional() {
            this.newCommandArgument.required = false;

            return this;
        }

        /**
         * Add a description
         */
        public UnderConstruction<DataType> description(String description) {
            this.newCommandArgument.description = description;

            return this;
        }

        /**
         * Set the default value for this argument.
         */
        public UnderConstruction<DataType> defaultValue(DataType defaultValue) {
            newCommandArgument.defaultValue = defaultValue;

            return this;
        }

        /**
         * Complete construction and register the definition with the rest of the system.
         */
        public CommandArgumentDefinition<DataType> build() {
            Scope.getCurrentScope().getSingleton(CommandFactory.class).register(commandStepClass, newCommandArgument);

            return newCommandArgument;
        }
    }

}
