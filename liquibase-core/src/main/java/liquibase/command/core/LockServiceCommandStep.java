package liquibase.command.core;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LockException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;

/**
 * Internal command step to be used on CommandStep pipeline to create lock services.
 */
public class LockServiceCommandStep extends AbstractCommandStep implements CleanUpCommandStep {

    protected static final String[] COMMAND_NAME = {"lockServiceCommandStep"};
    public static final CommandArgumentDefinition<Database> DATABASE_ARG;

    public static final CommandArgumentDefinition<LockService> LOCK_SERVICE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DATABASE_ARG = builder.databaseArgument().build();
        LOCK_SERVICE_ARG = builder.argument("LockService", LockService.class).description("Lock Service").build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = commandScope.getArgumentValue(DATABASE_ARG);
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();
        commandScope.addArgumentValue(LOCK_SERVICE_ARG.getName(), lockService);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        if (commandDefinition.getPipeline().size() == 1) {
            commandDefinition.setInternal(true);
        }
    }

    @Override
    public int getOrder(CommandDefinition commandDefinition) {
        if (isCommandDefinitionHasArgumentOfType(commandDefinition, LockService.class)) {
            return DbUrlConnectionCommandStep.ORDER + 100;
        }
        return super.getOrder(commandDefinition);
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        try {
            resultsBuilder.getCommandScope().getArgumentValue(LOCK_SERVICE_ARG).releaseLock();
        } catch (LockException e) {
            Scope.getCurrentScope().getLog(getClass()).severe(Liquibase.MSG_COULD_NOT_RELEASE_LOCK, e);
        }
    }
}
