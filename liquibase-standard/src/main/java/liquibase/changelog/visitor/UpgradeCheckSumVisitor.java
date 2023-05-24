package liquibase.changelog.visitor;

import liquibase.ChecksumVersions;
import liquibase.Scope;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;

import java.util.Set;

public class UpgradeCheckSumVisitor implements ChangeSetVisitor {

    private final Database database;

    public UpgradeCheckSumVisitor(Database database) {
        this.database = database;
    }

    @Override
    public Direction getDirection() {
        return Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database,
                      Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);

        ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        if (changeLogService.isDatabaseChecksumsCompatible() || changeSet.getStoredCheckSum() == null ||
                changeSet.getStoredCheckSum().getVersion() == ChecksumVersions.latest().getVersion()) {
            return;
        }

        CheckSum oldChecksum = changeSet.getStoredCheckSum();
        changeSet.clearCheckSum();
        changeSet.setStoredCheckSum(changeSet.generateCheckSum(ChecksumVersions.latest()));
        if (! (executor instanceof LoggingExecutor)) {
            Scope.getCurrentScope().getUI().sendMessage(String.format("Upgrading checksum for Changeset %s from %s to %s.",
                    changeSet, oldChecksum.toString(), changeSet.getStoredCheckSum().toString()));
        }

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .execute(new UpdateChangeSetChecksumStatement(changeSet));

        this.database.commit();
    }
}
