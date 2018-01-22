package liquibase.statement;

import liquibase.change.ColumnConfig;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Performance-optimised version of {@link ExecutablePreparedStatementBase}. JDBC batching collects several
 * executions of DML statements and waits until a certain number of commands has been collected. Then, it sends all
 * of them to the RDBMS in a single call. {@link java.sql.Statement#executeBatch()} saves many round-trips between
 * client and database, often speeding up bulk inserts/updates dramatically if the JDBC driver supports it.
 * @see <a href="https://blog.jooq.org/2014/01/16/what-you-didnt-know-about-jdbc-batch/">
 *     Blog entry on "Java Persistence Performance" about batching</a>
 */
public class BatchDmlExecutablePreparedStatement extends ExecutablePreparedStatementBase {
    private final List<ExecutablePreparedStatementBase> collectedStatements;
    private final Logger LOG = LogService.getLog(getClass());

    public BatchDmlExecutablePreparedStatement(
            Database database, String catalogName, String schemaName, String tableName,
            List<LoadDataColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor,
            List<ExecutablePreparedStatementBase> statements) {
        super(database, catalogName, schemaName, tableName, new ArrayList<ColumnConfig>(columns), changeSet,
            resourceAccessor);
        this.collectedStatements = new ArrayList<>(statements);
    }

    /**
     * Returns the individual statements that are currently store in this batch.
     * @return the List of the stored statements (may be empty if none are stored)
     */
    public List<ExecutablePreparedStatementBase> getIndividualStatements() {
        return new ArrayList<>(collectedStatements);
    }

    @Override
    protected void attachParams(List<ColumnConfig> ignored, PreparedStatement stmt)
            throws SQLException, DatabaseException {
        for (ExecutablePreparedStatementBase insertStatement : collectedStatements) {
            super.attachParams(insertStatement.getColumns(), stmt);
            stmt.addBatch();
        }
    }

    @Override
    protected String generateSql(List<ColumnConfig> cols) {
        // By convention, all of the statements are the same except the bind values. So it is sufficient to simply
        // return the first collected statement for generation.
        return collectedStatements.get(0).generateSql(cols);
    }

    @Override
    protected void executePreparedStatement(PreparedStatement stmt) throws SQLException {
        int updateCounts[] = stmt.executeBatch();
        long sumUpdateCounts = 0;
        for (int updateCount : updateCounts) {
            sumUpdateCounts = updateCount;
        }
        LOG.info(LogType.LOG, String.format("Executing JDBC DML batch was successful. %d operations were executed, %d individual UPDATE events were confirmed by the database.",
                updateCounts.length, sumUpdateCounts));
    }

    @Override
    public boolean continueOnError() {
        return false;
    }
}
