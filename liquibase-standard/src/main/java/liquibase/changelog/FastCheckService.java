package liquibase.changelog;

import liquibase.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ListVisitor;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.BooleanUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to perform a fast check of the historyService to determine if there is no unrun changesets without obtaining an exclusive write lock.
 */
public class FastCheckService implements SingletonObject {

    private FastCheckService() {
    }

    private final Map<String, Boolean> upToDateFastCheck = new ConcurrentHashMap<>();

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
    public boolean isUpToDateFastCheck(List<ChangeSetFilter> changesetFilters, Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        String cacheKey = String.format("%s/%s/%s/%s/%s/%s", contexts, labelExpression, database.getDefaultSchemaName(), database.getDefaultCatalogName(), database.getConnection().getURL(), databaseChangeLog.getLogicalFilePath());
        if (!upToDateFastCheck.containsKey(cacheKey) || BooleanUtils.isFalse(upToDateFastCheck.get(cacheKey))) {
            ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
            try {
                if (changeLogService.isDatabaseChecksumsCompatible() && listUnrunChangeSets(changesetFilters, database, databaseChangeLog, contexts, labelExpression).isEmpty()) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Fast check found no un-run changesets");
                    upToDateFastCheck.put(cacheKey, true);
                } else {
                    upToDateFastCheck.put(cacheKey, false);
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).info("Error querying Liquibase tables, disabling fast check for this execution. Reason: " + e.getMessage());
                upToDateFastCheck.put(cacheKey, false);
            } finally {
                // Discard the cached fetched un-run changeset list, as if another peer is running the changesets
                // in parallel, we may get a different answer after taking out the write lock
                changeLogService.reset();
            }
        }
        return upToDateFastCheck.get(cacheKey);
    }

    /**
     * Get list of ChangeSet which have not been applied
     *
     * @param database          the target database
     * @param databaseChangeLog the database changelog
     * @param contexts          the command contexts
     * @param labels            the command label expressions
     * @return a list of ChangeSet that have not been applied
     * @throws LiquibaseException if there was a problem building our ChangeLogIterator or checking the database
     */
    private List<ChangeSet> listUnrunChangeSets(List<ChangeSetFilter> changesetFilters, Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labels) throws LiquibaseException {
        ListVisitor visitor = new ListVisitor();
        databaseChangeLog.validate(database, contexts, labels);
        if (changesetFilters == null) {
            changesetFilters = getStandardChangelogIteratorFilters(database, contexts, labels);
        }
        changesetFilters.add(new ShouldRunChangeSetFilter(database));
        ChangeLogIterator logIterator = new ChangeLogIterator(databaseChangeLog, changesetFilters.toArray(new ChangeSetFilter[0]));
        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
        return visitor.getSeenChangeSets();
    }

    private List<ChangeSetFilter> getStandardChangelogIteratorFilters(Database database, Contexts contexts, LabelExpression labelExpression) {
        return new ArrayList<>(Arrays.asList(new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter()));
    }

    /**
     * Clear fastCheck cache
     */
    public void clearCache() {
        this.upToDateFastCheck.clear();
    }

}
