package liquibase.migrator;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.io.IOException;
import java.io.Writer;

import liquibase.migrator.preconditions.PreconditionFailedException;

public class RollbackDatabaseChangeLogHandler extends BaseChangeLogHandler {
    private List<RanChangeSet> ranChangesToRollback;
    private List<ChangeSet> allChangeSets;

    public RollbackDatabaseChangeLogHandler(Migrator migrator, Date rollbackToDate) throws SQLException {
        super(migrator);
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
        sql = sql.replaceFirst("\\?", changeSet.getId().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", changeSet.getAuthor().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", migrator.getMigrationFile().replaceAll("'", "''"));

        Writer sqlOutputWriter = migrator.getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Connection connection = migrator.getDatabase().getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
        } else {
            sqlOutputWriter.write(sql + ";\n\n");
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
