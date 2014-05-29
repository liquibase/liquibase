package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ChangeLogSyncVisitorTest {
    private ChangeSet changeSet;
    private DatabaseChangeLog databaseChangeLog;

    @Before
    public void setUp() {
        changeSet = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null);
        databaseChangeLog = new DatabaseChangeLog();
    }

    @Test
    public void testVisitDatabaseConstructor() throws LiquibaseException {
        Database mockDatabase = mock(Database.class);
        ChangeLogSyncVisitor visitor = new ChangeLogSyncVisitor(mockDatabase);
        visitor.visit(changeSet, databaseChangeLog, mockDatabase, Collections.<ChangeSetFilterResult>emptySet());
        verify(mockDatabase).markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
    }

    @Test
    public void testVisitListenerConstructor() throws LiquibaseException {
        Database mockDatabase = mock(Database.class);
        ChangeLogSyncListener mockListener = mock(ChangeLogSyncListener.class);
        ChangeLogSyncVisitor visitor = new ChangeLogSyncVisitor(mockDatabase, mockListener);
        visitor.visit(changeSet, databaseChangeLog, mockDatabase, Collections.<ChangeSetFilterResult>emptySet());
        verify(mockDatabase).markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
        verify(mockListener).markedRan(changeSet, databaseChangeLog, mockDatabase);
    }
}
