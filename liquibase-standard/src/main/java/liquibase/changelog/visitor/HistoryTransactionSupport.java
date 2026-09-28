package liquibase.changelog.visitor;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;

final class HistoryTransactionSupport {

    private HistoryTransactionSupport() {
    }

    /**
     * Returns true if the changeset's own transaction can stay open until its DATABASECHANGELOG update commits,
     * so that the changes and the history update are committed (or rolled back) together.
     */
    static boolean commitsWithHistory(ChangeSet changeSet, Database database) {
        if (!changeSet.isRunInTransaction() || !database.supportsDDLInTransaction()) {
            return false;
        }
        ChangeLogHistoryService historyService = Scope.getCurrentScope()
                .getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        return historyService.supportsAtomicHistoryUpdates();
    }
}
