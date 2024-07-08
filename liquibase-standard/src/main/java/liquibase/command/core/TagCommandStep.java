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

public class TagCommandStep extends AbstractCommandStep {

    protected static final String[] COMMAND_NAME = {"tag"};

    public static final CommandArgumentDefinition<String> TAG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        TAG_ARG = builder.argument("tag", String.class).required().description("Tag to add to the database changelog table").build();
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
        changeLogService.generateDeploymentId();
        changeLogService.init();
        LockServiceFactory.getInstance().getLockService(database).init();
        changeLogService.tag(commandScope.getArgumentValue(TagCommandStep.TAG_ARG));

        sendResults(database);
    }

    private void sendResults(Database database) {
        Scope.getCurrentScope().getUI().sendMessage(String.format(
                        coreBundle.getString("successfully.tagged"), database
                                .getConnection().getConnectionUserName() + "@" +
                                database.getConnection().getURL()
                )
        );
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Mark the current database state with the specified tag");
    }
}
