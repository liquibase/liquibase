package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class UpdateSQLCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(UpdateSQLCommandStep.class);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required().build();
        URL_ARG = builder.argument("url", String.class).required().build();
        LABELS_ARG = builder.argument("labels", String.class).build();
        CONTEXTS_ARG = builder.argument("contexts", String.class).build();
    }

    @Override
    public String[] getName() {
        return new String[] {"updateSQL"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createArgs(commandScope);
        int statusCode = Main.run(args);
        resultsBuilder.addResult("statusCode", statusCode);
    }
}
