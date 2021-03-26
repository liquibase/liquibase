package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class CalculateCheckSumCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> CHANGESET_IDENTIFIER_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(CalculateCheckSumCommandStep.class);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required().build();
        URL_ARG = builder.argument("url", String.class).required().build();
        CHANGESET_IDENTIFIER_ARG = builder.argument("changeSetIdentifier", String.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[] {"calculateCheckSum"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createParametersFromArgs(createArgs(commandScope), "changeSetIdentifier");
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }
}
