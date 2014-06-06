package liquibase.changelog.filter

import liquibase.change.CheckSum
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.exception.DatabaseException
import liquibase.executor.Executor
import liquibase.executor.ExecutorService
import liquibase.executor.UpdateResult
import liquibase.statement.core.UpdateStatement
import spock.lang.Specification

public class ShouldRunChangeSetFilterTest extends Specification {

    def database

    def setup() {
        this.database = Mock(Database)
    }

    def cleanup() {
        ExecutorService.getInstance().reset()
    }

    def accepts_noneRun() throws DatabaseException {
        when:
        database.getRanChangeSetList() >> new ArrayList<RanChangeSet>()

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database)

        then:
        assert filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted()
    }

    def accepts() throws DatabaseException {
        when:
        given_a_database_with_two_executed_changesets()
        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database)

        then:
        assert !filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted()
        assert filter.accepts(new ChangeSet("1", "testAuthor", true, false, "path/changelog", null, null, null)).isAccepted()
        assert filter.accepts(new ChangeSet("1", "testAuthor", false, true, "path/changelog", null, null, null)).isAccepted() //RunOnChange changed changeset should be accepted
        assert filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted() //ChangeSet with different id should be accepted
        assert filter.accepts(new ChangeSet("1", "otherAuthor", false, false, "path/changelog", null, null, null)).isAccepted() //ChangeSet with different author should be accepted
        assert filter.accepts(new ChangeSet("1", "testAuthor", false, false, "other/changelog", null, null, null)).isAccepted() //ChangSet with different path should be accepted
    }

    def does_NOT_accept_current_changeset_with_classpath_prefix() throws DatabaseException {
        when:
        given_a_database_with_two_executed_changesets()
        ChangeSet changeSetWithClasspathPrefix = new ChangeSet("1", "testAuthor", false, false, "classpath:path/changelog", null, null, null)

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true)

        then:
        assert !filter.accepts(changeSetWithClasspathPrefix).isAccepted()
    }

    def does_NOT_accept_current_changeset_when_inserted_changeset_has_classpath_prefix() throws DatabaseException {
        when:
        given_a_database_with_two_executed_changesets()
        ChangeSet changeSet = new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null)

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true)

        then:
        assert !filter.accepts(changeSet).isAccepted()
    }

    def does_NOT_accept_current_changeset_when_both_have_classpath_prefix() throws DatabaseException {
        when:
        given_a_database_with_two_executed_changesets()
        ChangeSet changeSet = new ChangeSet("2", "testAuthor", false, false, "classpath:path/changelog", null, null, null)

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database, true)

        then:
        assert !filter.accepts(changeSet).isAccepted()
    }

    private Database given_a_database_with_two_executed_changesets() throws DatabaseException {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>()
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null))
        ranChanges.add(new RanChangeSet("classpath:path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null))

        database.getRanChangeSetList() >> ranChanges
        database.getDatabaseChangeLogTableName() >> "DATABASECHANGELOG"
        database.getDefaultSchemaName() >> null

        def template = Mock(Executor)
        template.update(UpdateStatement) >> new UpdateResult(1)

        ExecutorService.getInstance().setExecutor(database, template)
        return database
    }
}
