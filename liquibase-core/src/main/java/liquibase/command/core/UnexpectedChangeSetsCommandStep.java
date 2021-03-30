package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnexpectedChangeSetsCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> VERBOSE_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(UnexpectedChangeSetsCommandStep.class);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required().build();
        URL_ARG = builder.argument("url", String.class).required().build();
        CONTEXTS_ARG = builder.argument("contexts", String.class).build();
        VERBOSE_ARG = builder.argument("verbose", String.class).build();
    }

    @Override
    public String[] getName() {
        return new String[] {"unexpectedChangeSets"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        List<String> rhsArgs = new ArrayList<>();
        rhsArgs.add("verbose");
        String[] args = createArgs(commandScope, rhsArgs);
        for (int i=0; i < args.length; i++) {
            if (args[i].toLowerCase().startsWith("--verbose")) {
                args[i] = "--verbose";
                break;
            }
        }
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }
}
