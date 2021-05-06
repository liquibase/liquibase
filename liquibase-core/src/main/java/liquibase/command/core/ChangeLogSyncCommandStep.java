package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class ChangeLogSyncCommandStep extends AbstractCliWrapperCommandStep {

    public static final String[] COMMAND_NAME = {"changeLogSync"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;

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
        LABELS_ARG = builder.argument("labels", String.class)
            .description("Label expression to use for filtering which changes to mark as executed").build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
            .description("Context string to use for filtering which changes to mark as executed").build();
    }

    @Override
    public String[] getName() {
        return COMMAND_NAME;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createArgs(commandScope);
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Marks all changes as executed in the database");
        commandDefinition.setLongDescription("Marks all changes as executed in the database");
    }
}
