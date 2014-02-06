package liquibase.changelog.filter;

import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.Date;

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.UpdateStatement;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShouldRunChangeSetFilterTest  {

    private Database database = createMock(Database.class);

    @Test
    public void accepts_noneRun() throws DatabaseException {
        expect(database.getRanChangeSetList()).andReturn(new ArrayList<RanChangeSet>());
        replay(database);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database);

        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());
    }

    @Test
    public void accepts() throws DatabaseException {
        given_a_database_with_two_executed_changesets();

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database);

        assertFalse("Already ran changeset should not be accepted", filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());

        assertTrue("AlwaysRun changesets should always be accepted", filter.accepts(new ChangeSet("1", "testAuthor", true, false, "path/changelog", null, null, null)).isAccepted());

        assertTrue("RunOnChange changed changeset should be accepted", filter.accepts(new ChangeSet("1", "testAuthor", false, true, "path/changelog", null, null, null)).isAccepted());

        assertTrue("ChangeSet with different id should be accepted", filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());

        assertTrue("ChangeSet with different author should be accepted", filter.accepts(new ChangeSet("1", "otherAuthor", false, false, "path/changelog", null, null, null)).isAccepted());

        assertTrue("ChangSet with different path should be accepted", filter.accepts(new ChangeSet("1", "testAuthor", false, false, "other/changelog", null, null, null)).isAccepted());
    }

    @Test
    public void does_NOT_accept_current_changeset_with_classpath_prefix() throws DatabaseException {
        given_a_database_with_two_executed_changesets();
        ChangeSet changeSetWithClasspathPrefix = new ChangeSet("1", "testAuthor", false, false, "classpath:path/changelog", null, null, null);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true);

        assertFalse(filter.accepts(changeSetWithClasspathPrefix).isAccepted());
    }

    @Test
    public void does_NOT_accept_current_changeset_when_inserted_changeset_has_classpath_prefix() throws DatabaseException {
        given_a_database_with_two_executed_changesets();
        ChangeSet changeSet = new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true);

        assertFalse(filter.accepts(changeSet).isAccepted());
    }

    @Test
    public void does_NOT_accept_current_changeset_when_both_have_classpath_prefix() throws DatabaseException {
        given_a_database_with_two_executed_changesets();
        ChangeSet changeSet = new ChangeSet("2", "testAuthor", false, false, "classpath:path/changelog", null, null, null);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true);

        assertFalse(filter.accepts(changeSet).isAccepted());
    }

    private Database given_a_database_with_two_executed_changesets() throws DatabaseException {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));
        ranChanges.add(new RanChangeSet("classpath:path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));

        expect(database.getRanChangeSetList()).andReturn(ranChanges);
        expect(database.getDatabaseChangeLogTableName()).andReturn("DATABASECHANGELOG").anyTimes();
        expect(database.getDefaultSchemaName()).andReturn(null).anyTimes();

        Executor template = createMock(Executor.class);
        expect(template.update(isA(UpdateStatement.class))).andReturn(1).anyTimes();

        replay(database);
        replay(template);
        ExecutorService.getInstance().setExecutor(database, template);
        return database;
    }
}
