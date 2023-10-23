package liquibase.command.core;

import liquibase.Scope;
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

        TAG_EXISTS_RESULT = builder.result("tagExistsResult", Boolean.class).description("Does the tag exists?").build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);
        ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        changeLogService.init();
        LockServiceFactory.getInstance().getLockService(database).init();

        String tag = commandScope.getArgumentValue(TagCommandStep.TAG_ARG);
        sendResults(changeLogService.tagExists(tag), database, tag, resultsBuilder);
    }

    private void sendResults(boolean exists, Database database, String tag, CommandResultsBuilder resultsBuilder) {
        resultsBuilder.addResult(TAG_EXISTS_RESULT, exists);
        Scope.getCurrentScope().getUI().sendMessage(String.format(coreBundle.getString(exists ? "tag.exists" : "tag.does.not.exist"),
                tag, database.getConnection().getConnectionUserName() + "@" + database.getConnection().getURL())
        );
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
