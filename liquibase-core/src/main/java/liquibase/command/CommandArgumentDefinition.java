package liquibase.command;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.exception.CommandValidationException;
import liquibase.util.ObjectUtil;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Defines a known, type-safe argument for a specific {@link CommandStep}.
 * Includes metadata about the argument such as a description, if it is required, a default value, etc.
 * <p>
 * Because this definition is tied to a specific step, multiple steps in a pipeline can define arguments of the same name.
 *
 * @see CommandBuilder#argument(String, Class) for constructing new instances.
 */
public class CommandArgumentDefinition<DataType> implements Comparable<CommandArgumentDefinition<?>> {

    private static final Pattern ALLOWED_ARGUMENT_PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    private final String name;
    private final Class<DataType> dataType;

    private String description;
    private boolean required;
    private DataType defaultValue;
    private String defaultValueDescription;
    private ConfigurationValueConverter<DataType> valueHandler;
    private ConfigurationValueObfuscator<DataType> valueObfuscator;

    protected CommandArgumentDefinition(String name, Class<DataType> type) {
        this.name = name;
        this.dataType = type;
        this.valueHandler = value -> ObjectUtil.convert(value, type);
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
     * The default value to use for this argument
     */
    public DataType getDefaultValue() {
        return defaultValue;
    }

    /**
     * A description of the default value. Defaults to {@link String#valueOf(Object)} of {@link #getDefaultValue()} but
     * can be explicitly with {@link Building#defaultValueDescriptionHandler(ConfigurationValueConverter)}.
     */
    public String getDefaultValueDescription() {
        return defaultValueDescription;
    }

    /**
     * Function for converting values set in underlying {@link liquibase.configuration.ConfigurationValueProvider}s into the
     * type needed for this command.
     */
    public ConfigurationValueConverter<DataType> getValueHandler() {
        return valueHandler;
    }

    /**
     * Used when sending the value to user output to protect secure values.
     */
    public ConfigurationValueObfuscator<DataType> getValueObfuscator() {
        return valueObfuscator;
    }

    /**
     * Validates that the value stored in the given {@link CommandScope} is valid.
     *
     * @throws CommandValidationException if the stored value is not valid.
     */
    public void validate(CommandScope commandScope) throws CommandValidationException {
        final DataType currentValue = commandScope.getArgumentValue(this);
        if (this.isRequired() && currentValue == null) {
            throw new CommandValidationException(this.getName(), "missing required argument");
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
     * A new {@link CommandArgumentDefinition} under construction from {@link CommandBuilder}
     */
    public static class Building<DataType> {
        private final String[] commandName;
        private final CommandArgumentDefinition<DataType> newCommandArgument;

        Building(String[] commandName, CommandArgumentDefinition<DataType> newCommandArgument) {
            this.commandName = commandName;
            this.newCommandArgument = newCommandArgument;
        }

        /**
         * Mark argument as required.
         * @see #optional()
         */
        public Building<DataType> required() {
            this.newCommandArgument.required = true;

            return this;
        }

        /**
         * Mark argument as optional.
         * @see #required()
         */
        public Building<DataType> optional() {
            this.newCommandArgument.required = false;

            return this;
        }

        /**
         * Add a description
         */
        public Building<DataType> description(String description) {
            this.newCommandArgument.description = description;

            return this;
        }

        /**
         * Set the default value for this argument as well as the description of the default value.
         */
        public Building<DataType> defaultValue(DataType defaultValue, String description) {
            newCommandArgument.defaultValue = defaultValue;
            newCommandArgument.defaultValueDescription = description;

            return this;
        }

        /**
         * Convenience version of {@link #defaultValue(Object, String)} but using {@link String#valueOf(Object)} for the description.
         */
        public Building<DataType> defaultValue(DataType defaultValue) {
            String description = null;
            if (defaultValue != null) {
                description = String.valueOf(defaultValue);
            }
            return this.defaultValue(defaultValue, description);
        }

        /**
         * Set the {@link #getValueHandler()} to use.
         */
        public Building<DataType> setValueHandler(ConfigurationValueConverter<DataType> valueHandler) {
            newCommandArgument.valueHandler = valueHandler;
            return this;
        }

        /**
         * Set the {@link #getValueObfuscator()} to use.
         */
        public Building<DataType> setValueObfuscator(ConfigurationValueObfuscator<DataType> valueObfuscator) {
            newCommandArgument.valueObfuscator = valueObfuscator;
            return this;
        }

        /**
         * Complete construction and register the definition with the rest of the system.
         *
         * @throws IllegalArgumentException is an invalid configuration was specified
         */
        public CommandArgumentDefinition<DataType> build() throws IllegalArgumentException {
            if (!ALLOWED_ARGUMENT_PATTERN.matcher(newCommandArgument.name).matches()) {
                throw new IllegalArgumentException("Invalid argument format: " + newCommandArgument.name);
            }

            try {
                Scope.getCurrentScope().getSingleton(CommandFactory.class).register(commandName, newCommandArgument);
            }
            catch (IllegalArgumentException iae) {
                Scope.getCurrentScope().getLog(CommandArgumentDefinition.class).warning(
                    "Unable to register command '" + commandName + "' argument '" + newCommandArgument.getName() + "': " + iae.getMessage());
                throw iae;
            }

            return newCommandArgument;
        }
    }

}
