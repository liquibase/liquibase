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

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 1;
    }

    @Override
    public boolean supports(Database database) {
        return enabled && super.supports(database);
    }

    @Override
    public boolean supportsAtomicHistoryUpdates() {
        return false;
    }

    @Override
    public void setExecType(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {
        used = true;
        super.setExecType(changeSet, execType);
    }
}
