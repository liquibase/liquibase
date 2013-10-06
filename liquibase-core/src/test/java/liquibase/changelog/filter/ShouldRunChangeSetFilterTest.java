package liquibase.changelog.filter;

import static org.assertj.core.api.Assertions.assertThat;
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

public class ShouldRunChangeSetFilterTest  {

    private Database database = createMock(Database.class);

    @Test
    public void accepts_noneRun() throws DatabaseException {
        expect(database.getRanChangeSetList()).andReturn(new ArrayList<RanChangeSet>());
        replay(database);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database);

        assertThat(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null))).isTrue();
    }

    @Test
    public void accepts() throws DatabaseException {
        given_a_database_with_two_executed_changesets();

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database);

        assertThat(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)))
            .describedAs("everything same")
            .isFalse();

        assertThat(filter.accepts(new ChangeSet("1", "testAuthor", true, false, "path/changelog", null, null, null)))
            .describedAs("alwaysRun")
            .isTrue();

        assertThat(filter.accepts(new ChangeSet("1", "testAuthor", false, true, "path/changelog", null, null, null)))
            .describedAs("run on change")
            .isTrue();

        assertThat(filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog", null, null, null)))
            .describedAs("different id")
            .isTrue();

        assertThat(filter.accepts(new ChangeSet("1", "otherAuthor", false, false, "path/changelog", null, null, null)))
            .describedAs("different author")
            .isTrue();

        assertThat(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "other/changelog", null, null, null)))
            .describedAs("different path")
            .isTrue();
    }

    @Test
    public void does_NOT_accept_current_changeset_with_classpath_prefix() throws DatabaseException {
        given_a_database_with_two_executed_changesets();
        ChangeSet changeSetWithClasspathPrefix = new ChangeSet("1", "testAuthor", false, false, "classpath:path/changelog", null, null, null);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true);

        assertThat(filter.accepts(changeSetWithClasspathPrefix))
            .isFalse();
    }

    @Test
    public void does_NOT_accept_current_changeset_when_inserted_changeset_has_classpath_prefix() throws DatabaseException {
        given_a_database_with_two_executed_changesets();
        ChangeSet changeSet = new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true);

        assertThat(filter.accepts(changeSet))
            .isFalse();
    }

    @Test
    public void does_NOT_accept_current_changeset_when_both_have_classpath_prefix() throws DatabaseException {
        given_a_database_with_two_executed_changesets();
        ChangeSet changeSet = new ChangeSet("2", "testAuthor", false, false, "classpath:path/changelog", null, null, null);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true);

        assertThat(filter.accepts(changeSet))
            .isFalse();
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
