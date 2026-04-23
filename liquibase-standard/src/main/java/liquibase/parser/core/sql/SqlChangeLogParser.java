package liquibase.parser.core.sql;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.*;
import liquibase.changeset.ChangeSetService;
import liquibase.changeset.ChangeSetServiceFactory;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.ExceptionUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("java:S2583")
public class SqlChangeLogParser implements ChangeLogParser {

    // Per-Database index of physicalChangeLogLocation -> interim changeset id, built lazily on
    // the first generateId call and reused thereafter. Avoids an O(N) scan of getRanChangeSets()
    // per SQL changelog file, which on workloads with many SQL changelogs and large
    // DATABASECHANGELOG histories was the dominant cost of changelog parsing.
    private final Map<Database, Map<String, String>> interimIdIndexByDatabase = new ConcurrentHashMap<>();

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return changeLogFile.toLowerCase().endsWith(".sql");
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {

        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.setPhysicalFilePath(physicalChangeLogLocation);

        RawSQLChange change = new RawSQLChange();

        try {
            //
            // Handle empty files with a WARNING message
            //
            Resource sqlResource = resourceAccessor.getExisting(physicalChangeLogLocation);
            String sql = StreamUtil.readStreamAsString(sqlResource.openInputStream());
            //
            // Handle empty files with a WARNING message
            //
            if (StringUtils.isEmpty(sql)) {
                String message = String.format("Unable to parse empty file: '%s'", physicalChangeLogLocation);
                Scope.getCurrentScope().getLog(getClass()).warning(message);
                throw new ChangeLogParseException(message);
            }
            change.setSql(sql);
        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }
        change.setSplitStatements(false);
        change.setStripComments(false, true);

        Database database = Scope.getCurrentScope().getDatabase();
        ChangeSetServiceFactory factory = ChangeSetServiceFactory.getInstance();
        ChangeSetService service = factory.createChangeSetService();
        ChangeSet changeSet =
           service.createChangeSet(generateId(physicalChangeLogLocation, database), "includeAll",
                false, false, physicalChangeLogLocation, null,
                  null, null, null, true,
                         ObjectQuotingStrategy.LEGACY, changeLog);
        changeSet.addChange(change);

        changeLog.addChangeSet(changeSet);

        ExceptionUtil.doSilently(() -> {
            Scope.getCurrentScope().getAnalyticsEvent().incrementSqlChangelogCount();
        });

        return changeLog;
    }

    /**
     *
     * Generate an change set ID based on the SQL file path, unless there is an existing
     * ran change set with an id/author of "raw::includeAll", which has always been
     * the hardcoded combination for SQL changelog change sets
     *
     * @param   physicalChangeLogLocation    the path to the changelog
     * @param   database                     the database we are using
     * @return  String                       a change set ID
     *
     */
    private String generateId(String physicalChangeLogLocation, Database database) {
        if (database == null || isOldFormat(database)) {
            return "raw";
        }
        Map<String, String> index = interimIdIndexByDatabase.computeIfAbsent(database, this::buildInterimIdIndex);
        return index.getOrDefault(physicalChangeLogLocation, "raw");
    }

    private Map<String, String> buildInterimIdIndex(Database database) {
        try {
            Map<String, String> result = new HashMap<>();
            for (RanChangeSet rc : Scope.getCurrentScope()
                    .getSingleton(ChangeLogHistoryServiceFactory.class)
                    .getChangeLogService(database).getRanChangeSets()) {
                String id = rc.getId();
                String changeLog = rc.getChangeLog();
                if (id == null || changeLog == null || !"includeAll".equals(rc.getAuthor())) {
                    continue;
                }
                String expectedInterimId = "raw_" + DatabaseChangeLog.normalizePath(changeLog).replace("/", "_");
                if (id.equals(expectedInterimId)) {
                    result.put(changeLog, id);
                }
            }
            return result;
        } catch (Exception dbe) {
            return Collections.emptyMap();
        }
    }

    /**
     *
     * Handle the possibility that the changelog is an old format
     *
     * @param   database          The database in question
     * @return  boolean
     *
     */
    private static boolean isOldFormat(Database database) {
        Table changeLogTable = null;
        try {
            changeLogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl
                    (database, false, Table.class, Column.class), database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return changeLogTable != null && changeLogTable.getColumn("ORDEREXECUTED") == null;
    }
}
