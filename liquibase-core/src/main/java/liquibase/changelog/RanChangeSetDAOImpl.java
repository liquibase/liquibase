package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RanChangeSetDAOImpl implements RanChangeSetDAO<RanChangeSet> {

    private Boolean hasDatabaseChangeLogTable;
    private RanChangeSetFactory<? extends RanChangeSet> ranChangeSetFactory;

    public RanChangeSetDAOImpl(RanChangeSetFactory<? extends RanChangeSet> ranChangeSetFactory) {
        this.ranChangeSetFactory = ranChangeSetFactory;
    }

    public List<RanChangeSet> prepareRanChangeSets(Database database, boolean databaseChecksumsCompatible) throws DatabaseException {
        String databaseChangeLogTableName = database.escapeTableName(database.getLiquibaseCatalogName(),
                database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        List<RanChangeSet> ranChangeSets = new ArrayList<>();
        if (hasDatabaseChangeLogTable(database)) {
            LogService.getLog(getClass()).info(LogType.LOG, "Reading from " + databaseChangeLogTableName);
            List<Map<String, ?>> results = queryDatabaseChangeLogTable(database);
            for (Map rs : results) {
                ranChangeSets.add(getRanChangeSet(databaseChecksumsCompatible, rs));
            }
        }
        return ranChangeSets;
    }

    private RanChangeSet getRanChangeSet(boolean databaseChecksumsCompatible, Map rs) {
        String fileName = rs.get("FILENAME").toString();
        String author = rs.get("AUTHOR").toString();
        String id = rs.get("ID").toString();
        String md5sum = ((rs.get("MD5SUM") == null) || !databaseChecksumsCompatible) ? null : rs.get("MD5SUM").toString();
        String description = (rs.get("DESCRIPTION") == null) ? null : rs.get("DESCRIPTION").toString();
        String comments = (rs.get("COMMENTS") == null) ? null : rs.get("COMMENTS").toString();
        Object tmpDateExecuted = rs.get("DATEEXECUTED");
        Date dateExecuted = null;
        if (tmpDateExecuted instanceof Date) {
            dateExecuted = (Date) tmpDateExecuted;
        } else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                dateExecuted = df.parse((String) tmpDateExecuted);
            } catch (ParseException e) {
                // Ignore ParseException and assume dateExecuted == null instead of aborting.
            }
        }
        String tmpOrderExecuted = rs.get("ORDEREXECUTED").toString();
        Integer orderExecuted = ((tmpOrderExecuted == null) ? null : Integer.valueOf(tmpOrderExecuted));
        String tag = (rs.get("TAG") == null) ? null : rs.get("TAG").toString();
        String execType = (rs.get("EXECTYPE") == null) ? null : rs.get("EXECTYPE").toString();
        ContextExpression contexts = new ContextExpression((String) rs.get("CONTEXTS"));
        Labels labels = new Labels((String) rs.get("LABELS"));
        String deploymentId = (String) rs.get("DEPLOYMENT_ID");

        return ranChangeSetFactory.create(
                fileName,
                author,
                id,
                md5sum,
                description,
                comments,
                dateExecuted,
                orderExecuted,
                tag,
                execType,
                contexts,
                labels,
                deploymentId
        );
    }

    private boolean hasDatabaseChangeLogTable(Database database) {
        if (hasDatabaseChangeLogTable == null) {
            try {
                hasDatabaseChangeLogTable = SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable
                        (database);
            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return hasDatabaseChangeLogTable;
    }


    private List<Map<String, ?>> queryDatabaseChangeLogTable(Database database) throws DatabaseException {
        SelectFromDatabaseChangeLogStatement select = new SelectFromDatabaseChangeLogStatement(new ColumnConfig()
                .setName("*").setComputed(true)).setOrderBy("DATEEXECUTED ASC", "ORDEREXECUTED ASC");
        return ExecutorService.getInstance().getExecutor(database).queryForList(select);
    }

}
