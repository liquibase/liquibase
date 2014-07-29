package liquibase.changelog.filter;

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.*;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.UpdateStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class AlreadyRanChangeSetFilterTest {

    private Database database = createMock(Database.class);

    @Test
    public void accepts_noneRun() {
        AlreadyRanChangeSetFilter filter = new AlreadyRanChangeSetFilter(new ArrayList<RanChangeSet>(), false);

        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog",null, null, null)).isAccepted());
    }

    @Test
    public void accepts() {
        AlreadyRanChangeSetFilter filter = new AlreadyRanChangeSetFilter(getRanChangeSets(), false);

        //everything same
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog",  null, null, null)).isAccepted());

        //alwaysRun
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", true, false, "path/changelog",  null, null, null)).isAccepted());

        //run on change
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", false, true, "path/changelog", null, null, null)).isAccepted());

        //different id
        assertFalse(filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());

        //different author
        assertFalse(filter.accepts(new ChangeSet("1", "otherAuthor", false, false, "path/changelog", null, null, null)).isAccepted());

        //different path
        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "other/changelog", null, null, null)).isAccepted());
    }

    @Test
    public void does_accept_current_changeset_with_classpath_prefix() throws DatabaseException {
        ChangeSet changeSetWithClasspathPrefix = new ChangeSet("1", "testAuthor", false, false, "classpath:path/changelog", null, null, null);

        AlreadyRanChangeSetFilter filter = new AlreadyRanChangeSetFilter(getRanChangeSets(), true);

        assertTrue(filter.accepts(changeSetWithClasspathPrefix).isAccepted());
    }

    @Test
    public void does_accept_current_changeset_when_inserted_changeset_has_classpath_prefix() throws DatabaseException {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));
        ranChanges.add(new RanChangeSet("classpath:path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));

        ChangeSet changeSet = new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null);

        AlreadyRanChangeSetFilter filter = new AlreadyRanChangeSetFilter(ranChanges, true);

        assertTrue(filter.accepts(changeSet).isAccepted());
    }

    private ArrayList<RanChangeSet> getRanChangeSets() {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));
        return ranChanges;
    }
}
