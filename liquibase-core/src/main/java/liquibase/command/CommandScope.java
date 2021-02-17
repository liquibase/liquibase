package liquibase.command;

import liquibase.snapshot.DatabaseSnapshot;

import java.util.HashMap;
import java.util.Map;

public class CommandScope {

    private final String[] command;

    private final Map<String, Object> arguments = new HashMap<>();

    public CommandScope(String... command) {
        this.command = command;
    }

    public String[] getCommand() {
        return command;
    }

    public CommandScope addArguments(CommandArgument... arguments) {
        for (CommandArgument argument : arguments) {
            this.addArgument(argument.getDefinition().getName(), argument.getValue());
        }

        return this;
    }

    public CommandScope addArgument(String argument, Object value) {
        this.arguments.put(argument, value);

        return this;
    }


    public Object getValue(String argument) {
        return this.arguments.get(argument);
    }

    public void addResult(String key, Object value) {
        //TODO: save
    }

    public Object getResult(String key) {
        //TODO: return

        return null;
    }
}
