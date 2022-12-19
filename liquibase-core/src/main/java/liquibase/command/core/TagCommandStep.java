package liquibase.command.core;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LockException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;

public class TagCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"tag"};

    public static final CommandArgumentDefinition<String> TAG_ARG;
    public static final CommandArgumentDefinition<Database> DATABASE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        TAG_ARG = builder.argument("tag", String.class).required()
            .description("Tag to add to the database changelog table").build();
        DATABASE_ARG = builder.argument("database", Database.class).required()
                .description("Database connection").build();
        InternalDatabaseCommandStep.addApplicableCommand(COMMAND_NAME);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = commandScope.getArgumentValue(DATABASE_ARG);
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
            changeLogService.generateDeploymentId(); // TODO do we need that?
            changeLogService.init();
            LockServiceFactory.getInstance().getLockService(database).init();
            changeLogService.tag(commandScope.getArgumentValue(TagCommandStep.TAG_ARG));
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                Scope.getCurrentScope().getLog(getClass()).severe(Liquibase.MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
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
