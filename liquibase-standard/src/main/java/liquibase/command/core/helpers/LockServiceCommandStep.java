package liquibase.command.core.helpers;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CleanUpCommandStep;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
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

    public static final String[] COMMAND_NAME = {"lockServiceCommandStep"};

    private final ThreadLocal<Boolean> isDBLocked = ThreadLocal.withInitial(() -> false);

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
        LockServiceFactory.getInstance().getLockService(database).waitForLock();
        isDBLocked.set(true);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        try {
            if (isDBLocked.get()) {
                try {
                    LockServiceFactory.getInstance().getLockService(
                            (Database) resultsBuilder.getCommandScope().getDependency(Database.class)
                    ).releaseLock();
                } catch (LockException e) {
                    Scope.getCurrentScope().getLog(getClass()).severe(Liquibase.MSG_COULD_NOT_RELEASE_LOCK, e);
                }
                LockServiceFactory.getInstance().resetAll();
            }
        } finally {
            isDBLocked.remove();
        }
    }
}
