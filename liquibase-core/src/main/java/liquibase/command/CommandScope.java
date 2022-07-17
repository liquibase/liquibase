package liquibase.command;

import liquibase.Scope;
import liquibase.configuration.*;
import liquibase.exception.CommandExecutionException;
import liquibase.util.StringUtil;

import java.io.OutputStream;
import java.util.*;

/**
 * The primary facade used for executing commands.
 * This object gets configured with the command to run and the input arguments associated with it,
 * then is populated with the result output after {@link #execute()} is called.
 * <p>
 * Named similarly to {@link Scope} because they both define a self-contained unit of values, but this
 * scope is specific to a command rather than being a global scope.
 */
public class CommandScope {

    private final CommandDefinition commandDefinition;

    private final SortedMap<String, Object> argumentValues = new TreeMap<>();
    private final CommandScopeValueProvider commandScopeValueProvider = new CommandScopeValueProvider();

    /**
     * Config key including the command name. Example `liquibase.command.update`
     */
    private final String completeConfigPrefix;

    /**
     * Config key without the command name. Example `liquibase.command`
     */
    private final String shortConfigPrefix;

    private OutputStream outputStream;

    /**
     * Creates a new scope for the given command.
     */
    public CommandScope(String... commandName) throws CommandExecutionException {
        setOutput(System.out);

        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);

        this.commandDefinition = commandFactory.getCommandDefinition(commandName);

        completeConfigPrefix = "liquibase.command." + StringUtil.join(Arrays.asList(this.getCommand().getName()), ".");
        shortConfigPrefix = "liquibase.command";


    }

    /**
     * Returns the {@link CommandDefinition} for the command in this scope.
     */
    public CommandDefinition getCommand() {
        return commandDefinition;
    }

    /**
     * Returns the complete config prefix (without a trailing period) for the command in this scope.
     * @return
     */
    public String getCompleteConfigPrefix() {
        return completeConfigPrefix;
    }

    /**
     * Adds the given key/value pair to the stored argument data.
     *
     * @see #addArgumentValue(CommandArgumentDefinition, Object) for a type-safe version
     */
    public CommandScope addArgumentValue(String argument, Object value) {
        this.argumentValues.put(argument, value);

        return this;
    }

    /**
     * Adds the given key/value pair to the stored argument data.
     */
    public <T> CommandScope addArgumentValue(CommandArgumentDefinition<T> argument, T value) {
        this.argumentValues.put(argument.getName(), value);

        return this;
    }


    /**
     * Returns the detailed information about how an argument is set.
     * <br><br>
     * Prefers values set with {@link #addArgumentValue(String, Object)}, but will also check {@link liquibase.configuration.LiquibaseConfiguration}
     * for settings of liquibase.command.${commandName(s)}.${argumentName} or liquibase.command.${argumentName}
     */
    public <T> ConfiguredValue<T> getConfiguredValue(CommandArgumentDefinition<T> argument) {
        ConfigurationDefinition<T> configDef = createConfigurationDefinition(argument, true);
        ConfiguredValue<T> providedValue = configDef.getCurrentConfiguredValue();

        if (!providedValue.found() || providedValue.wasDefaultValueUsed()) {
            ConfigurationDefinition<T> noCommandConfigDef = createConfigurationDefinition(argument, false);
            ConfiguredValue<T> noCommandNameProvidedValue = noCommandConfigDef.getCurrentConfiguredValue();
            if (noCommandNameProvidedValue.found() && !noCommandNameProvidedValue.wasDefaultValueUsed()) {
                configDef = noCommandConfigDef;
                providedValue = noCommandNameProvidedValue;
            }
        }

        providedValue.override(commandScopeValueProvider.getProvidedValue(configDef.getKey(), argument.getName()));

        return providedValue;
    }

    /**
     * Convenience method for {@link #getConfiguredValue(CommandArgumentDefinition)}, returning {@link ConfiguredValue#getValue()} along with any
     * {@link CommandArgumentDefinition#getValueConverter()} applied
     */
    public <T> T getArgumentValue(CommandArgumentDefinition<T> argument) {
        final T value = getConfiguredValue(argument).getValue();
        return argument.getValueConverter().convert(value);
    }

    /**
     * Sets the output stream for this command.
     * The command output sent to this stream should not include status/progress type output, only the actual output itself.
     * Think "what would be piped out", not "what the user is told about what is happening".
     */
    public CommandScope setOutput(OutputStream outputStream) {
        this.outputStream = outputStream;

        return this;
    }

    /**
     * Executes the command in this scope, and returns the results.
     */
    public CommandResults execute() throws CommandExecutionException {
        CommandResultsBuilder resultsBuilder = new CommandResultsBuilder(this, outputStream);
        for (ConfigurationValueProvider provider : Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getProviders()) {
            provider.validate(this);
        }

        for (CommandArgumentDefinition<?> definition : commandDefinition.getArguments().values()) {
            definition.validate(this);
        }

        final List<CommandStep> pipeline = commandDefinition.getPipeline();

        Scope.getCurrentScope().getLog(getClass()).fine("Pipeline for command '" + StringUtil.join(commandDefinition.getName(), " ") + ": " + StringUtil.join(pipeline, " then ", obj -> obj.getClass().getName()));

        for (CommandStep step : pipeline) {
            step.validate(this);
        }
        try {
            for (CommandStep command : pipeline) {
                command.run(resultsBuilder);
            }
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else {
                throw new CommandExecutionException(e);
            }
        } finally {
            try {
                if (this.outputStream != null) {
                    this.outputStream.flush();
                }
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Error flushing command output stream: " + e.getMessage(), e);
            }
        }

        return resultsBuilder.build();
    }

    private <T> ConfigurationDefinition<T> createConfigurationDefinition(CommandArgumentDefinition<T> argument, boolean includeCommandName) {
        final String key;
        if (includeCommandName) {
            key = completeConfigPrefix;
        } else {
            key = shortConfigPrefix;
        }

        return new ConfigurationDefinition.Builder(key)
                .define(argument.getName(), argument.getDataType())
                .setDefaultValue(argument.getDefaultValue())
                .setDescription(argument.getDescription())
                .setValueHandler(argument.getValueConverter())
                .setValueObfuscator(argument.getValueObfuscator())
                .buildTemporary();
    }

    private class CommandScopeValueProvider extends AbstractMapConfigurationValueProvider {

        @Override
        public int getPrecedence() {
            return -1;
        }

        @Override
        protected Map<?, ?> getMap() {
            return CommandScope.this.argumentValues;
        }

        @Override
        protected String getSourceDescription() {
            return "Command argument";
        }
    }
}
