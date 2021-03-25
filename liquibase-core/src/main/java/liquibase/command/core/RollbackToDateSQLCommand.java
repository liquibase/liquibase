package liquibase.command.core;

import liquibase.command.*;
import liquibase.command.AbstractCliWrapperCommandStep;
import liquibase.integration.commandline.Main;

import java.time.LocalDateTime;

public class RollbackToDateSQLCommand extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> ROLLBACK_SCRIPT_ARG;
    public static final CommandArgumentDefinition<LocalDateTime> DATE_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(RollbackToDateSQLCommand.class);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required().build();
        URL_ARG = builder.argument("url", String.class).required().build();
        LABELS_ARG = builder.argument("labels", String.class).build();
        CONTEXTS_ARG = builder.argument("contexts", String.class).build();
        ROLLBACK_SCRIPT_ARG = builder.argument("rollbackScript", String.class).build();
        DATE_ARG = builder.argument("date", LocalDateTime.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[] {"rollbackToDateSQL"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createParametersFromArgs(createArgs(commandScope), "date");
        int statusCode = Main.run(args);
        resultsBuilder.addResult("statusCode", statusCode);
    }
}
