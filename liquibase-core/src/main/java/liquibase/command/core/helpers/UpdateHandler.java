package liquibase.command.core.helpers;

import liquibase.*;
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

/**
 * Helper class used to handle general update related tasks.
 */
@Beta
public class UpdateHandler {
    /**
     * Get the DatabaseChangeLog from a changeLogFile
     * @param changeLogFile the changeLogFile
     * @param changeLogParameters any parameters used for parsing the changelog
     * @param shouldWarnOnMismatchedXsdVersion warn on differing XSDs
     * @return the parsed DatabaseChangeLog
     * @throws LiquibaseException if there was an exception encountered during parsing
     */
    @Beta
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

    /**
     * Check the DatabaseChangeLog table and update any checksums which may be null
     * @param database the database to check
     * @param updateExistingNullChecksums should we update null checksums if found
     * @param databaseChangeLog the databaseChangeLog to use during the check
     * @param contexts the command contexts
     * @param labelExpression the command label expressions
     * @throws LiquibaseException if there was an exception encountered during the check
     */
    @Beta
    public static void checkLiquibaseTables(Database database, boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    @Beta
    public static ChangeLogIterator getStandardChangelogIterator(Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    @Beta
    public static ChangeLogIterator getStatusChangelogIterator(Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        return new StatusChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }
}
