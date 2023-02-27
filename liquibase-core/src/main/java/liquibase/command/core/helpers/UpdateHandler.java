package liquibase.command.core.helpers;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ListVisitor;
import liquibase.changelog.visitor.StatusVisitor;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.mdc.MdcKey;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ShowSummaryUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UpdateHandler {
    private static final Map<String, Boolean> upToDateFastCheck = new ConcurrentHashMap<>();

    public static boolean isUpToDateFastCheck(Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        String cacheKey = contexts + "/" + labelExpression;
        if (!upToDateFastCheck.containsKey(cacheKey)) {
            try {
                if (listUnrunChangeSets(database, databaseChangeLog, contexts, labelExpression, false).isEmpty()) {
                    Scope.getCurrentScope().getLog(UpdateHandler.class).fine("Fast check found no un-run changesets");
                    upToDateFastCheck.put(cacheKey, true);
                } else {
                    upToDateFastCheck.put(cacheKey, false);
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(UpdateHandler.class).info("Error querying Liquibase tables, disabling fast check for this execution. Reason: " + e.getMessage());
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

    public static List<ChangeSet> listUnrunChangeSets(Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labels, boolean checkLiquibaseTables) throws LiquibaseException {
        ListVisitor visitor = new ListVisitor();
        if (checkLiquibaseTables) {
            checkLiquibaseTables(database, true, databaseChangeLog, contexts, labels);
        }
        databaseChangeLog.validate(database, contexts, labels);
        ChangeLogIterator logIterator = getStandardChangelogIterator(database, contexts, labels, databaseChangeLog);
        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
        return visitor.getSeenChangeSets();
    }

    public static DatabaseChangeLog getDatabaseChangeLog(String changeLogFile, ChangeLogParameters changeLogParameters, boolean shouldWarnOnMismatchedXsdVersion) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        if (changeLogFile != null) {
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
            ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
            if (parser instanceof XMLChangeLogSAXParser) {
                ((XMLChangeLogSAXParser) parser).setShouldWarnOnMismatchedXsdVersion(shouldWarnOnMismatchedXsdVersion);
            }
            return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
        }

        return null;
    }

    public static void checkLiquibaseTables(Database database, boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    public static ChangeLogIterator getStandardChangelogIterator(Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    public static ChangeLogIterator getStatusChangelogIterator(Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        return new StatusChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    public static boolean isUpToDate(Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException, IOException {
        if (isUpToDateFastCheck(database, databaseChangeLog, contexts, labelExpression)) {
            Scope.getCurrentScope().getUI().sendMessage("Database is up to date, no changesets to execute");
            StatusVisitor statusVisitor = new StatusVisitor(database);
            ChangeLogIterator shouldRunIterator = getStatusChangelogIterator(database, contexts, labelExpression, databaseChangeLog);
            shouldRunIterator.run(statusVisitor, new RuntimeEnvironment(database, contexts, labelExpression));
            ShowSummaryUtil.showUpdateSummary(databaseChangeLog, statusVisitor);
            return true;
        }
        return false;
    }
}
