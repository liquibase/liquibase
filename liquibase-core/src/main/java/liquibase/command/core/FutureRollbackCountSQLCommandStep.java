package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class FutureRollbackCountSQLCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<Integer> COUNT_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(FutureRollbackCountSQLCommandStep.class);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required()
            .description("The root changelog").build();
        URL_ARG = builder.argument("url", String.class).required()
            .description("The JDBC database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
            .description("The database username").build();
        PASSWORD_ARG = builder.argument("password", String.class)
            .description("The database password").build();
        LABELS_ARG = builder.argument("labels", String.class)
            .description("Changeset labels to match").build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
            .description("Changeset contexts to match").build();
        COUNT_ARG = builder.argument("count", Integer.class).required()
            .description("Number of change sets to generate rollback SQL for").build();
    }

    @Override
    public String[] getName() {
        return new String[] {"futureRollbackCountSQL"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createParametersFromArgs(createArgs(commandScope), "count");
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Generates SQL to sequentially revert <count> number of changes");
        commandDefinition.setLongDescription("Generates SQL to sequentially revert <count> number of changes");
    }
}
