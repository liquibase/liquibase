package liquibase.command;

import liquibase.Scope;
import liquibase.exception.CommandExecutionException;
import liquibase.util.StringUtil;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
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
    private final SortedMap<String, Object> resultValues = new TreeMap<>();

    private PrintWriter output;
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
     * Returns a {@link PrintWriter} for output for the command.
     * The command output should not include status/progress type output, only the actual output itself.
     * Think "what would be piped out", not "what goes to stderr during a pipe".
     *
     * Defaults to {@link System#out}
     *
     * @see #getOutputStream() for binary output
     * @see #setOutput(OutputStream) for setting
     */
    public PrintWriter getOutput() {
        return output;
    }

    /**
     * Returns an {@link OutputStream} for output for the command.
     * Generally {@link #getOutput()} should be used, but this is available for commands that deal with binary data.
     *
     * @see #getOutput() for text-based output.
     * @see #setOutput(OutputStream) for setting
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Sets the output stream for this command. Creates a convenience {@link PrintWriter} around the stream that can be accessed with
     * {@link #getOutput()}, or this stream can be used directly with {@link #getOutputStream()}
     */
    public CommandScope setOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.output = new PrintWriter(outputStream);

        return this;
    }

    /**
     * Adds a key/value pair to the command results.
     * @see #addResult(CommandResultDefinition, Object)
     */
    public CommandScope addResult(String key, Object value) {
        this.resultValues.put(key, value);

        return this;
    }

    /**
     * Sets the value for a known {@link CommandResultDefinition} to the command results.
     */
    public <T> CommandScope addResult(CommandResultDefinition<T> definition, T value) {
        this.resultValues.put(definition.getName(), value);

        return this;
    }

    /**
     * Return the value for the given {@link CommandResultDefinition}, or the default value if not set.
     */
    public <DataType> DataType getResult(CommandResultDefinition<DataType> definition) {
        return (DataType) resultValues.get(definition.getName());
    }

    /**
     * Return the result value for the given key.
     */
    public Object getResult(String key) {
        return resultValues.get(key);
    }

    /**
     * Returns all the results for this command.
     */
    public SortedMap<String, Object> getResults() {
        return Collections.unmodifiableSortedMap(resultValues);
    }

    /**
     * Executes the command in this scope, and stores the results in it.
     */
    public void execute() throws CommandExecutionException {

        for (CommandArgumentDefinition definition : commandDefinition.getArguments().values()) {
            definition.validate(this);
        }

        for (CommandStep step : commandDefinition.getPipeline()) {
            step.validate(this);
        }
        try {
            for (CommandStep command : commandDefinition.getPipeline()) {
                command.run(this);
            }
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else {
                throw new CommandExecutionException(e);
            }
        } finally {
            this.output.flush();
        }
    }
}
