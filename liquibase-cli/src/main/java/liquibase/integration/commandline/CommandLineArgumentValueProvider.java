package liquibase.integration.commandline;

import liquibase.configuration.AbstractMapConfigurationValueProvider;
import picocli.CommandLine;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CommandLineArgumentValueProvider  extends AbstractMapConfigurationValueProvider {

    private final SortedMap<String, Object> argumentValues = new TreeMap<>();

    public CommandLineArgumentValueProvider(CommandLine.ParseResult parseResult) {
        for (CommandLine.Model.OptionSpec option : parseResult.matchedOptions()) {
            this.argumentValues.put(option.names()[0].replaceFirst("^--", ""), option.getValue());
        }
    }

    @Override
    public int getPrecedence() {
        return 250;
    }

    @Override
    protected Map<?, ?> getMap() {
        return argumentValues;
    }

    @Override
    protected String getSourceDescription() {
        return "Command argument";
    }
}
