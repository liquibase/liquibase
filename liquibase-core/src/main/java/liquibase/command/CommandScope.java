package liquibase.command;

import liquibase.Scope;
import liquibase.exception.CommandExecutionException;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

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

    private OutputStream outputStream;

    /**
     * Creates a new scope for the given command.
     */
    public CommandScope(String... commandName) throws CommandExecutionException {
        setOutput(System.out);

        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);

        this.commandDefinition = commandFactory.getCommand(commandName);

        if (this.commandDefinition == null) {
            throw new CommandExecutionException("Unknown command: "+ StringUtil.join(Arrays.asList(commandName), " "));
        }
    }

    /**
     * Returns the {@link CommandDefinition} for the command in this scope.
     */
    public CommandDefinition getCommand() {
        return commandDefinition;
    }


    public CommandScope addArgumentValue(String argument, Object value) {
        this.argumentValues.put(argument, value);

        return this;
    }

    public <T> CommandScope addArgumentValue(CommandArgumentDefinition<T> argument, T value) {
        this.argumentValues.put(argument.getName(), value);

        return this;
    }


    /**
     * Returns the value of the given argument.
     * Prefers values set with {@link #addArgumentValue(String, Object)}, but will also check {@link liquibase.configuration.LiquibaseConfiguration}
     * for settings of liquibase.command.${commandName(s)}.${argumentName} and then liquibase.command.${argumentName}
     */
    public Object getArgumentValue(String argument) {
        return this.argumentValues.get(argument);
    }

    /**
     * Convenience method for {@link #getArgumentValue(String)} given a known {@link CommandArgumentDefinition}
     */
    public <T> T getArgumentValue(CommandArgumentDefinition<T> argument) {
        return (T) this.getArgumentValue(argument.getName());
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

        for (CommandArgumentDefinition<?> definition : commandDefinition.getArguments().values()) {
            definition.validate(this);
        }

        for (CommandStep step : commandDefinition.getPipeline()) {
            step.validate(this);
        }
        try {
            for (CommandStep command : commandDefinition.getPipeline()) {
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
                this.outputStream.flush();
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Error flushing command output stream: "+e.getMessage(), e);
            }
        }

        return resultsBuilder.build();
    }
}
