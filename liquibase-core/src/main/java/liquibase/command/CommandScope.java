package liquibase.command;

import com.sun.deploy.util.StringUtils;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.CommandExecutionException;

import java.util.*;

public class CommandScope {

    private final String[] command;
    private final SortedSet<LiquibaseCommand> commandPipeline;
    private final SortedMap<String, CommandArgumentDefinition> arguments = new TreeMap<>();

    private final Map<String, Object> argumentValues = new HashMap<>();

    public CommandScope(String... command) throws CommandExecutionException {
        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);

        this.command = command;
        this.commandPipeline = commandFactory.getCommandPipeline(this);

        if (commandPipeline.size() == 0) {
            throw new CommandExecutionException("Unknown command: "+ StringUtils.join(Arrays.asList(this.getCommand()), " "));
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

    public void addResult(String key, Object value) {
        //TODO: save
    }

    public Object getResult(String key) {
        //TODO: return

        return null;
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
        }
    }
}
