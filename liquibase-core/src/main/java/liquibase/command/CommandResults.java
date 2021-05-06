package liquibase.command;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The results of {@link CommandScope#execute()}.
 */
public class CommandResults {

    private final SortedMap<String, Object> resultValues = new TreeMap<>();
    private final CommandScope commandScope;

    protected CommandResults(SortedMap<String, Object> resultValues, CommandScope commandScope) {
        this.resultValues.putAll(resultValues);
        this.commandScope = commandScope;
    }

    /**
     * The {@link CommandScope} that was executed to produce this result.
     */
    public CommandScope getCommandScope() {
        return commandScope;
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
}
