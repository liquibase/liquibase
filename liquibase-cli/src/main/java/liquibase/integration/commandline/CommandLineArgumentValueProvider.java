package liquibase.integration.commandline;

import liquibase.configuration.AbstractMapConfigurationValueProvider;
import picocli.CommandLine;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class CommandLineArgumentValueProvider extends AbstractMapConfigurationValueProvider {

    private final SortedMap<String, Object> argumentValues = new TreeMap<>();

    public CommandLineArgumentValueProvider(CommandLine.ParseResult parseResult) {
        while (parseResult != null) {
            for (CommandLine.Model.OptionSpec option : parseResult.matchedOptions()) {
                this.argumentValues.put(option.names()[0], option.getValue());
            }

            parseResult = parseResult.subcommand();
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
    protected boolean keyMatches(String wantedKey, String storedKey) {
        storedKey = String.valueOf(storedKey).replaceFirst("^--", "");

        if (super.keyMatches(wantedKey, storedKey)) {
            return true;
        }
        // test
        if (wantedKey.startsWith("liquibase.command.")) {
            return super.keyMatches(wantedKey.replaceFirst("^liquibase\\.command\\.", ""), storedKey);
        }

        return super.keyMatches(wantedKey.replaceFirst("^liquibase\\.", ""), storedKey);
    }

    @Override
    protected String getSourceDescription() {
        return "Command argument";
    }
}
