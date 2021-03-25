package liquibase.command.core;

import liquibase.command.AbstractWrapperCommand;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandScope;
import liquibase.integration.commandline.Main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class RollbackToDateCommand extends AbstractWrapperCommand {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> ROLLBACK_SCRIPT_ARG;
    public static final CommandArgumentDefinition<LocalDateTime> DATE_ARG;

    static {
        CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder(RollbackToDateCommand.class);
        CHANGELOG_FILE_ARG = builder.define("changeLogFile", String.class).required().build();
        URL_ARG = builder.define("url", String.class).required().build();
        LABELS_ARG = builder.define("labels", String.class).build();
        CONTEXTS_ARG = builder.define("contexts", String.class).build();
        ROLLBACK_SCRIPT_ARG = builder.define("rollbackScript", String.class).build();
        DATE_ARG = builder.define("date", LocalDateTime.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[] {"rollbackToDate"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        String[] args = createParametersFromArgs(createArgs(commandScope), "date");
        int statusCode = Main.run(args);
        commandScope.addResult("statusCode", statusCode);
    }
}
