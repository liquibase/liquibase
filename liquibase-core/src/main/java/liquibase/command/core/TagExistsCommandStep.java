package liquibase.command.core;

import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;

import java.util.Arrays;
import java.util.List;

public class TagExistsCommandStep  extends AbstractCommandStep {

    protected static final String[] COMMAND_NAME = {"tagExists"};

    public static final CommandArgumentDefinition<String> TAG_ARG;
    public static final CommandResultDefinition<Boolean> TAG_EXISTS_RESULT;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        TAG_ARG = builder.argument("tag", String.class).required().description("Tag to check").build();

        TAG_EXISTS_RESULT = builder.result("tagExistsResult", Boolean.class).build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);
        ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogService.init();
        LockServiceFactory.getInstance().getLockService(database).init();
        resultsBuilder.addResult(TAG_EXISTS_RESULT, changeLogService.tagExists(commandScope.getArgumentValue(TagCommandStep.TAG_ARG)));
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Verify the existence of the specified tag");
    }
}
