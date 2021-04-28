package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class CalculateCheckSumCommandStep extends AbstractCliWrapperCommandStep {

    public static String[] COMMAND_NAME = {"calculateCheckSum"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> CHANGESET_IDENTIFIER_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required()
                .description("The root changelog file").build();
        URL_ARG = builder.argument("url", String.class).required()
                .description("The JDBC database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
                .description("The database username").build();
        PASSWORD_ARG = builder.argument("password", String.class)
                .description("The database password").build();
        CHANGESET_IDENTIFIER_ARG = builder.argument("changeSetIdentifier", String.class).required()
                .description("Change set ID identifier of form filepath::id::author").build();
    }

    @Override
    public String[] getName() {
        return COMMAND_NAME;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createParametersFromArgs(createArgs(commandScope), "changeSetIdentifier");
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Calculates and prints a checksum for the changeset");
        commandDefinition.setLongDescription("Calculates and prints a checksum for the changeset with the given id in the format filepath::id::author");
    }
}
