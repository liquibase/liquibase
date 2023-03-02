package liquibase.command.core.helpers;

import liquibase.*;
import liquibase.changelog.*;
import liquibase.changelog.visitor.ListVisitor;
import liquibase.changelog.visitor.StatusVisitor;
import liquibase.command.core.UpdateCommandStep;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.util.ShowSummaryUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles checking ChangeLogHistoryService to determine if there are no un-run ChangeSets without obtaining an exclusive write lock.
 */
@Beta
public class FastCheck {
    private static final Map<String, Boolean> upToDateFastCheck = new ConcurrentHashMap<>();

    public FastCheck() {

    }

    /**
     * Performs check of the historyService to determine if there is no unrun changesets without obtaining an exclusive write lock.
     * This allows multiple peer services to boot in parallel in the common case where there are no changelogs to run.
     * <p>
     * If we see that there is nothing in the changelog to run and this returns <b>true</b>, then regardless of the lock status we already know we are "done" and can finish up without waiting for the lock.
     * <p>
     * But, if there are changelogs that might have to be ran and this returns <b>false</b>, you MUST get a lock and do a real check to know what changesets actually need to run.
     * <p>
     * NOTE: to reduce the number of queries to the databasehistory table, this method will cache the "fast check" results within this instance under the assumption that the total changesets will not change within this instance.
     */
    private boolean isUpToDateFastCheck(Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        String cacheKey = contexts + "/" + labelExpression;
        if (!upToDateFastCheck.containsKey(cacheKey)) {
            try {
                if (listUnrunChangeSets(database, databaseChangeLog, contexts, labelExpression).isEmpty()) {
                    Scope.getCurrentScope().getLog(FastCheck.class).fine("Fast check found no un-run changesets");
                    upToDateFastCheck.put(cacheKey, true);
                } else {
                    upToDateFastCheck.put(cacheKey, false);
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(FastCheck.class).info("Error querying Liquibase tables, disabling fast check for this execution. Reason: " + e.getMessage());
                upToDateFastCheck.put(cacheKey, false);
            } finally {
                // Discard the cached fetched un-run changeset list, as if
                // another peer is running the changesets in parallel, we may
                // get a different answer after taking out the write lock
                ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
                changeLogService.reset();
            }
        }
        return upToDateFastCheck.get(cacheKey);
    }

    /**
     * Get list of ChangeSet which have not been applied
     * @param database the target database
     * @param databaseChangeLog the database changelog
     * @param contexts the command contexts
     * @param labels the command label expressions
     * @return a list of ChangeSet that have not been applied
     * @throws LiquibaseException if there was a problem building our ChangeLogIterator or checking the database
     */
    private static List<ChangeSet> listUnrunChangeSets(Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labels) throws LiquibaseException {
        ListVisitor visitor = new ListVisitor();
        databaseChangeLog.validate(database, contexts, labels);
        ChangeLogIterator logIterator = UpdateCommandStep.getStandardChangelogIterator(database, contexts, labels, databaseChangeLog);
        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
        return visitor.getSeenChangeSets();
    }


    /**
     * Checks if the database is up-to-date.
     * @param database the database to check
     * @param databaseChangeLog the databaseChangeLog of the database
     * @param contexts the command contexts
     * @param labelExpression the command label expressions
     * @return true if there are no additional changes to execute, otherwise false
     * @throws LiquibaseException if there was a problem running any queries
     * @throws IOException if there was a problem handling the update summary
     */
    @Beta
    public boolean isUpToDate(Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException, IOException {
        if (isUpToDateFastCheck(database, databaseChangeLog, contexts, labelExpression)) {
            Scope.getCurrentScope().getUI().sendMessage("Database is up to date, no changesets to execute");
            StatusVisitor statusVisitor = new StatusVisitor(database);
            ChangeLogIterator shouldRunIterator = UpdateCommandStep.getStatusChangelogIterator(database, contexts, labelExpression, databaseChangeLog);
            shouldRunIterator.run(statusVisitor, new RuntimeEnvironment(database, contexts, labelExpression));
            ShowSummaryUtil.showUpdateSummary(databaseChangeLog, statusVisitor);
            return true;
        }
        return false;
    }

}
