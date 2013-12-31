package liquibase.changelog;

import liquibase.Contexts;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OfflineChangeLogHistoryService implements ChangeLogHistoryService {

    @Override
    public int getPriority() {
        return 500;
    }

    @Override
    public boolean supports(Database database) {
        return database.getConnection() != null && database.getConnection() instanceof OfflineConnection;
    }

    @Override
    public void setDatabase(Database database) {

    }

    @Override
    public void reset() {

    }

    @Override
    public void init() throws DatabaseException {

    }

    @Override
    public void upgradeChecksums(DatabaseChangeLog databaseChangeLog, Contexts contexts) throws DatabaseException {

    }

    @Override
    public List<RanChangeSet> getRanChangeSets() throws DatabaseException {
        return new ArrayList<RanChangeSet>();
    }

    @Override
    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeSet.RunStatus.NOT_RAN;
    }

    @Override
    public Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public void setExecType(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {

    }

    @Override
    public void removeFromHistory(ChangeSet changeSet) throws DatabaseException {

    }

    @Override
    public int getNextSequenceValue() throws LiquibaseException {
        return 0;
    }

    @Override
    public void tag(String tagString) throws DatabaseException {

    }

    @Override
    public boolean tagExists(String tag) throws DatabaseException {
        return false;
    }
}
