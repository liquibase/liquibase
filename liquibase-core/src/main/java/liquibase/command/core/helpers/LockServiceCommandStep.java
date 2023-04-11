package liquibase.command.core.helpers;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LockException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;

import java.util.Collections;
import java.util.List;

/**
 * Internal command step to be used on CommandStep pipeline to create lock services.
 */
public class LockServiceCommandStep extends AbstractHelperCommandStep implements CleanUpCommandStep {

    protected static final String[] COMMAND_NAME = {"lockServiceCommandStep"};

    private LockService lockService;

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(LockService.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);
        lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();
        commandScope.provideDependency(LockService.class, lockService);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        try {
            lockService.releaseLock();
        } catch (LockException e) {
            Scope.getCurrentScope().getLog(getClass()).severe(Liquibase.MSG_COULD_NOT_RELEASE_LOCK, e);
        }
        LockServiceFactory.getInstance().resetAll();
    }
}
