package liquibase.changelog;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RanChangeSetService<T extends RanChangeSet> {

    private Boolean hasDatabaseChangeLogTable;
    private RanChangeSetFactory<T> factory;

    public RanChangeSetService(RanChangeSetFactory<T> factory) {
        this.factory = factory;
    }

    public List<T> prepareRanChangeSets(Database database) throws DatabaseException {
        boolean databaseChecksumsCompatible = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).isDatabaseCheckSumCompatible();
        String databaseChangeLogTableName = database.escapeTableName(database.getLiquibaseCatalogName(),
                database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        List<T> ranChangeSets = new ArrayList<>();
        if (hasDatabaseChangeLogTable(database)) {
            LogService.getLog(getClass()).info(LogType.LOG, "Reading from " + databaseChangeLogTableName);
            List<Map<String, ?>> results = queryDatabaseChangeLogTable(database);
            for (Map rs : results) {
                ranChangeSets.add(factory.create(databaseChecksumsCompatible, rs));
            }
        }
        return ranChangeSets;
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
    public RanChangeSet create(ChangeSet changeSet, ChangeSet.ExecType execType) {
        return new RanChangeSet(changeSet, execType, null, null);
    }
}
