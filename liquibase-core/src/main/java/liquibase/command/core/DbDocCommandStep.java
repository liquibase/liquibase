package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class DbDocCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> OUTPUT_DIRECTORY_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(DbDocCommandStep.class);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required().build();
        URL_ARG = builder.argument("url", String.class).required().build();
        OUTPUT_DIRECTORY_ARG = builder.argument("outputDirectory", String.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[] {"dbDoc"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createParametersFromArgs(createArgs(commandScope), "outputDirectory");
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }
}
