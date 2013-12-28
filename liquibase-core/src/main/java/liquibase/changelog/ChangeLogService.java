package liquibase.changelog;

import liquibase.Contexts;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;
import liquibase.servicelocator.PrioritizedService;

import java.util.Date;
import java.util.List;

public interface ChangeLogService extends PrioritizedService {

    boolean supports(Database database);

    void setDatabase(Database database);

    void reset();

    /**
     * Ensures the change log history container is correctly initialized for use. This method may be called multiple times so it should check state as needed.
     */
    public void init() throws DatabaseException;

    /**
     * Upgrades any existing checksums with an out of date version
     */
    void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts) throws DatabaseException;

    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException;

    ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    void markChangeSetExecStatus(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException;

    void removeRanStatus(ChangeSet changeSet) throws DatabaseException;

    int getNextChangeSetSequenceValue() throws LiquibaseException;

    void tag(String tagString) throws DatabaseException;

    boolean doesTagExist(String tag) throws DatabaseException;
}
