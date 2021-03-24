package liquibase.command.core;

import liquibase.command.AbstractWrapperCommand;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandScope;
import liquibase.integration.commandline.Main;

public class RollbackCountCommand extends AbstractWrapperCommand {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<Integer> COUNT_ARG;

    static {
        CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder(RollbackCountCommand.class);
        CHANGELOG_FILE_ARG = builder.define("changeLogFile", String.class).required().build();
        URL_ARG = builder.define("url", String.class).required().build();
        LABELS_ARG = builder.define("labels", String.class).build();
        CONTEXTS_ARG = builder.define("contexts", String.class).build();
        COUNT_ARG = builder.define("count", Integer.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[] {"rollbackCount"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        String[] args = createArgs(commandScope);
        args = createParametersFromArgs(args, "count");
        int statusCode = Main.run(args);
        commandScope.addResult("statusCode", statusCode);
    }
}
