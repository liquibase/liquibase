package liquibase.changelog;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockChangeLogHistoryService implements ChangeLogHistoryService {

    public List<RanChangeSet> ranChangeSets = new ArrayList<>();

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof MockDatabase;
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
    public void upgradeChecksums(DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labels) throws DatabaseException {

    }

    @Override
    public List<RanChangeSet> getRanChangeSets() throws DatabaseException {
        return ranChangeSets;
    }

    @Override
    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
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

    @Override
    public void clearAllCheckSums() throws LiquibaseException {

    }

    @Override
    public void destroy() throws DatabaseException {

    }

    @Override
    public String getDeploymentId() {
        return null;
    }

    @Override
    public void resetDeploymentId() {

    }

    @Override
    public void generateDeploymentId() {

    }
}
