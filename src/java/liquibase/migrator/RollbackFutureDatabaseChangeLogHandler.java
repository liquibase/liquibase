package liquibase.migrator;

import liquibase.migrator.preconditions.PreconditionFailedException;

import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.io.IOException;
import java.io.Writer;

public class RollbackFutureDatabaseChangeLogHandler extends BaseChangeLogHandler {
    private List<ChangeSet> changesToRollback;
    private List<RanChangeSet> ranChangeSets;

    public RollbackFutureDatabaseChangeLogHandler(Migrator migrator) throws SQLException {
        super(migrator);
        changesToRollback = new ArrayList<ChangeSet>();
        ranChangeSets = migrator.getRanChangeSetList();
    }

    protected void handleChangeSet(ChangeSet changeSet) throws SQLException, DatabaseHistoryException, MigrationFailedException, PreconditionFailedException, IOException {
        boolean alreadyRan = false;
        for (RanChangeSet cs : ranChangeSets) {
            if (cs.isSameAs(changeSet)) {
                alreadyRan = true;
                break;
            }
        }
        if (!alreadyRan) {
            changesToRollback.add(0, changeSet);
        }
    }

    public void doRollback() throws MigrationFailedException, DatabaseHistoryException, SQLException, IOException {
        for (ChangeSet changeSet : changesToRollback) {
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
        for (ChangeSet changeSet : changesToRollback) {
            if (!changeSet.canRollBack()) {
                return changeSet;
            }
        }
        return null;
    }
}
