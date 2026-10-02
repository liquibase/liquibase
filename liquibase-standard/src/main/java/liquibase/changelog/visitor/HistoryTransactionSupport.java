package liquibase.changelog.visitor;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;

final class HistoryTransactionSupport {

    /** Static helpers only; not meant to be instantiated. */
    private HistoryTransactionSupport() {
    }

    /**
     * Returns true if the changeset's own transaction can stay open until its DATABASECHANGELOG update commits,
     * so that the changes and the history update are committed (or rolled back) together. Always false for a
     * changeset with {@code runWith}, because a custom executor runs its changes outside this transaction.
     */
    static boolean commitsWithHistory(ChangeSet changeSet, Database database) {
        if (!GlobalConfiguration.ATOMIC_HISTORY_UPDATES.getCurrentValue()) {
            return false;
        }
        if (!changeSet.isRunInTransaction() || !database.supportsDDLInTransaction()) {
            return false;
        }
        // getRunWith() rather than the field: it treats an empty runWith as unset, as ChangeSet itself does
        if (changeSet.getRunWith() != null) {
            return false;
        }
        ChangeLogHistoryService historyService = Scope.getCurrentScope()
                .getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        return historyService.supportsAtomicHistoryUpdates();
    }
}
