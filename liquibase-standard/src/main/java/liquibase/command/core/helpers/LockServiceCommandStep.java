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
                Database database = (Database) resultsBuilder.getCommandScope().getDependency(Database.class);
                try {
                    // Check hasChangeLogLock() rather than releasing unconditionally: some
                    // AbstractUpdateCommandStep subclasses (e.g. updateCount/updateSql/updateToTag) also
                    // release the lock themselves, from within run(), before this cleanUp() runs -- see
                    // #5438. Releasing again here without checking live state would fail against an
                    // already-unlocked row.
                    LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                    if (lockService.hasChangeLogLock()) {
                        lockService.releaseLock();
                    }
                } catch (LockException e) {
                    Scope.getCurrentScope().getLog(getClass()).severe(Liquibase.MSG_COULD_NOT_RELEASE_LOCK, e);
                }
                // Drop only this database's lock service, not every database's: resetAll() would discard
                // the singleton and with it the lock services of any execution running concurrently
                // against another database, leaving them unable to release the lock they still hold.
                LockServiceFactory.getInstance().resetDatabase(database);
            }
        } finally {
            isDBLocked.remove();
        }
    }
}
