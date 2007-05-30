package liquibase.migrator;

import liquibase.util.StreamUtil;
import liquibase.migrator.preconditions.PreconditionFailedException;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An implementation of BaseChangeLogHandler for generating rollback statements.
 *
 * @see BaseChangeLogHandler
 */
public class RollbackDatabaseChangeLogHandler extends BaseChangeLogHandler {
    private List<RanChangeSet> ranChangesToRollback;
    private List<ChangeSet> allChangeSets;

    public RollbackDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath, String rollbackToTag) throws SQLException {
        super(migrator, physicalFilePath);
        ranChangesToRollback = new ArrayList<RanChangeSet>();
        int currentChangeSetCount = migrator.getRanChangeSetList().size();
        for (int i = currentChangeSetCount - 1; i >= 0; i--) {
            RanChangeSet ranChangeSet = migrator.getRanChangeSetList().get(i);
            if (rollbackToTag.equals(ranChangeSet.getTag())) {
                break;
            }
            ranChangesToRollback.add(ranChangeSet);
        }
        allChangeSets = new ArrayList<ChangeSet>();
    }

    public RollbackDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath, Date rollbackToDate) throws SQLException {
        super(migrator, physicalFilePath);
        ranChangesToRollback = new ArrayList<RanChangeSet>();
        int currentChangeSetCount = migrator.getRanChangeSetList().size();
        for (int i = currentChangeSetCount - 1; i >= 0; i--) {
            RanChangeSet ranChangeSet = migrator.getRanChangeSetList().get(i);
            if (ranChangeSet.getDateExecuted().getTime() > rollbackToDate.getTime()) {
                ranChangesToRollback.add(ranChangeSet);
            }
        }
        allChangeSets = new ArrayList<ChangeSet>();
    }

    public RollbackDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath, Integer numberOfChangeSetsToRollback) throws SQLException {
        super(migrator, physicalFilePath);
        ranChangesToRollback = new ArrayList<RanChangeSet>();
        int currentChangeSetCount = migrator.getRanChangeSetList().size();
        for (int i = currentChangeSetCount - 1; i >= 0; i--) {
            RanChangeSet ranChangeSet = migrator.getRanChangeSetList().get(i);
            ranChangesToRollback.add(ranChangeSet);
            if (ranChangesToRollback.size() >= numberOfChangeSetsToRollback) {
                break;
            }
        }
        allChangeSets = new ArrayList<ChangeSet>();
    }

    protected void handleChangeSet(ChangeSet changeSet) throws SQLException, DatabaseHistoryException, MigrationFailedException, PreconditionFailedException, IOException {
        for (RanChangeSet cs : ranChangesToRollback) {
            if (cs.isSameAs(changeSet)) {
                allChangeSets.add(0, changeSet);
            }
        }
    }

    public void doRollback() throws MigrationFailedException, DatabaseHistoryException, SQLException, IOException {
        for (ChangeSet changeSet : allChangeSets) {
            changeSet.execute();
            removeRanStatus(changeSet);
        }
    }

    private void removeRanStatus(ChangeSet changeSet) throws SQLException, IOException {
        Migrator migrator = changeSet.getDatabaseChangeLog().getMigrator();
        String sql = "DELETE FROM DATABASECHANGELOG WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getId()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getAuthor()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getDatabaseChangeLog().getFilePath()));

        Writer sqlOutputWriter = migrator.getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Connection connection = migrator.getDatabase().getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
        } else {
            sqlOutputWriter.write(sql + ";" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
        }
    }

    public ChangeSet getUnRollBackableChangeSet() {
        for (ChangeSet changeSet : allChangeSets) {
            if (!changeSet.canRollBack()) {
                return changeSet;
            }
        }
        return null;
    }
}
