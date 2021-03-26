package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class ChangeLogSyncCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(ChangeLogSyncCommandStep.class);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required().build();
        URL_ARG = builder.argument("url", String.class).required().build();
        LABELS_ARG = builder.argument("labels", String.class).build();
        CONTEXTS_ARG = builder.argument("contexts", String.class).build();
    }

    @Override
    public String[] getName() {
        return new String[] {"changeLogSync"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createArgs(commandScope);
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }
}
