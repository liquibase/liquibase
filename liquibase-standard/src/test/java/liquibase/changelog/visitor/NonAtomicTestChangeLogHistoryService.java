package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.StandardChangeLogHistoryService;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;

/**
 * A history service that does not opt in to atomic history updates. Discovered through META-INF/services and
 * only applies while {@link #enabled} is set, so it has no effect on other tests.
 */
public class NonAtomicTestChangeLogHistoryService extends StandardChangeLogHistoryService {

    static volatile boolean enabled;
    static volatile boolean used;

    /** Ranks above {@link StandardChangeLogHistoryService} so this service is chosen while enabled. */
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 1;
    }

    /** Applies only while {@link #enabled} is set. */
    @Override
    public boolean supports(Database database) {
        return enabled && super.supports(database);
    }

    /** Returns false, so changesets keep the two-commit behavior. */
    @Override
    public boolean supportsAtomicHistoryUpdates() {
        return false;
    }

    /** Records that this service was used, then writes the history row as usual. */
    @Override
    public void setExecType(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {
        used = true;
        super.setExecType(changeSet, execType);
    }
}
