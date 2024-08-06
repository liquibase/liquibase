package liquibase.command;

import liquibase.Scope;
import liquibase.util.StringUtil;
import lombok.Getter;

import java.io.OutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Because {@link CommandResults} is immutable, this class is used to build up the results during {@link CommandStep#run(CommandResultsBuilder)}.
 */
public class CommandResultsBuilder {

    /**
     * -- GETTER --
     *  Returns the
     *  for output for the command.
     *
     */
    @Getter
    private final OutputStream outputStream;
    private final SortedMap<String, Object> resultValues = new TreeMap<>();
    /**
     * -- GETTER --
     *  Return the
     *  the results are being built for.
     */
    @Getter
    private final CommandScope commandScope;


    CommandResultsBuilder(CommandScope commandScope, OutputStream outputStream) {
        this.outputStream = outputStream;
        this.commandScope = commandScope;
    }

    /**
     * Adds a key/value pair to the command results.
     * @see #addResult(CommandResultDefinition, Object)
     */
    public CommandResultsBuilder addResult(String key, Object value) {
        this.resultValues.put(key, value);

        return this;
    }

    /**
     * Allows any step of the pipeline to access any of the results.
     */
    public Object getResult(String key) {
        return this.resultValues.get(key);
    }

    /**
     * Sets the value for a known {@link CommandResultDefinition} to the command results.
     */
    public <T> CommandResultsBuilder addResult(CommandResultDefinition<T> definition, T value) {
        return addResult(definition.getName(), value);
    }

    public CommandFailedException commandFailed(String message, int exitCode) {
        return commandFailed(message, exitCode, false);
    }

    public CommandFailedException commandFailed(String message, int exitCode, boolean expected) {
        return new CommandFailedException(this.build(), exitCode, message, expected);
    }

    /**
     * Collects the results and flushes the output stream.
     */
    CommandResults build() {
        try {
            if (this.outputStream != null) {
                outputStream.flush();
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Error flushing " + StringUtil.join(commandScope.getCommand().getName(), " ") + " output: " + e.getMessage(), e);
        }

        return new CommandResults(resultValues, commandScope);
    }
}
