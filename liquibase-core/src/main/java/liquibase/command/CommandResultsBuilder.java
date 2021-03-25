package liquibase.command;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Because {@link CommandResults} is immutable, this class is used to build up the results during {@link CommandStep#run(CommandResultsBuilder)}.
 */
public class CommandResultsBuilder {

    private final PrintWriter output;
    private final OutputStream outputStream;
    private final SortedMap<String, Object> resultValues = new TreeMap<>();
    private final CommandScope commandScope;


    CommandResultsBuilder(CommandScope commandScope, OutputStream outputStream) {
        this.outputStream = outputStream;
        this.output = new PrintWriter(outputStream);
        this.commandScope = commandScope;
    }

    /**
     * Return the {@link CommandScope} the results are being built for.
     */
    public CommandScope getCommandScope() {
        return commandScope;
    }

    /**
     * Returns a {@link PrintWriter} wrapper around the stream in {@link #getOutputStream()}
     *
     * @see #getOutputStream() for binary output
     * @see CommandScope#setOutput(OutputStream)
     */
    public PrintWriter getOutput() {
        return output;
    }

    /**
     * Returns an {@link OutputStream} for output for the command.
     *
     * @see #getOutput() for text-based output.
     * @see CommandScope#setOutput(OutputStream)
     */
    public OutputStream getOutputStream() {
        return outputStream;
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
     * Sets the value for a known {@link CommandResultDefinition} to the command results.
     */
    public <T> CommandResultsBuilder addResult(CommandResultDefinition<T> definition, T value) {
        this.resultValues.put(definition.getName(), value);

        return this;
    }

    public CommandResults build() {
        return new CommandResults(resultValues, commandScope);
    }
}
