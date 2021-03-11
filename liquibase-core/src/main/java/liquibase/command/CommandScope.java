package liquibase.command;

import liquibase.Scope;
import liquibase.exception.CommandExecutionException;
import liquibase.util.StringUtil;

import java.io.*;
import java.util.*;

public class CommandScope {

    private final String[] command;
    private final SortedSet<LiquibaseCommand> commandPipeline;
    private final SortedMap<String, CommandArgumentDefinition> arguments = new TreeMap<>();

    private final Map<String, Object> argumentValues = new HashMap<>();
    private final Map<String, Object> resultValues = new HashMap<>();

    private PrintWriter output;
    private OutputStream outputStream;

    public CommandScope(String... command) throws CommandExecutionException {
        setOutput(System.out);

        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);

        this.command = command;
        this.commandPipeline = commandFactory.getCommandPipeline(this);

        if (commandPipeline.size() == 0) {
            throw new CommandExecutionException("Unknown command: "+ StringUtil.join(Arrays.asList(this.getCommand()), " "));
        }


        for (LiquibaseCommand step : commandPipeline) {
            for (CommandArgumentDefinition commandArg : commandFactory.getArguments(step)) {
                arguments.put(commandArg.getName(), commandArg);
            }
        }
    }

    public String[] getCommand() {
        return command;
    }


    public SortedMap<String, CommandArgumentDefinition> getArguments() throws CommandExecutionException {
        return Collections.unmodifiableSortedMap(arguments);
    }

    public SortedSet<CommandArgumentDefinition> getArguments(Class argumentType) {
        SortedSet<CommandArgumentDefinition> returnSet = new TreeSet<>();

        for (CommandArgumentDefinition definition : arguments.values()) {
            if (definition.getDataType().isAssignableFrom(argumentType)) {
                returnSet.add(definition);
            }
        }

        return returnSet;
    }


    public CommandScope addArgumentValues(CommandArgument... arguments) {
        for (CommandArgument argument : arguments) {
            this.addArgumentValue(argument.getDefinition().getName(), argument.getValue());
        }

        return this;
    }

    public CommandScope addArgumentValue(String argument, Object value) {
        this.argumentValues.put(argument, value);

        return this;
    }


    public Object getArgumentValue(String argument) {
        return this.argumentValues.get(argument);
    }

    /**
     * Returns a {@link PrintWriter} for output for the command.
     * The command output should not include status/progress type output, only the actual output itself.
     * Think "what would be piped out", not "what goes to stderr during a pipe".
     *
     * @see #getOutputStream() for binary output
     */
    public PrintWriter getOutput() {
        return output;
    }

    /**
     * Returns an {@link OutputStream} for output for the command.
     *
     * @see #getOutput() for text-based output.
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

    public CommandScope addResults(CommandResult... results) {
        if (results != null) {
            for (CommandResult result : results) {
                addResult(result.getDefinition().getName(), result.getValue());
            }
        }

        return this;
    }

    public CommandScope addResult(String key, Object value) {
        this.resultValues.put(key, value);

        return this;
    }

    public <DataType> DataType getResult(CommandResultDefinition<DataType> definition) {
        return (DataType) resultValues.get(definition.getName());
    }

    public Object getResult(String key) {
        return resultValues.get(key);
    }

    public void execute() throws CommandExecutionException {

        for (CommandArgumentDefinition definition : arguments.values()) {
            definition.validate(this);
        }

        for (LiquibaseCommand command : commandPipeline) {
            command.validate(this);
        }
        try {
            for (LiquibaseCommand command : commandPipeline) {
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
