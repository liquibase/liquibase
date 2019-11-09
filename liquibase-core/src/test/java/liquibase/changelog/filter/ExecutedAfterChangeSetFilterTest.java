package liquibase.changelog.filter;

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExecutedAfterChangeSetFilterTest {

    @Test
    public void accepts_noRan() throws Exception {
        ExecutedAfterChangeSetFilter filter = new ExecutedAfterChangeSetFilter(new Date(), new ArrayList<RanChangeSet>());

        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());
    }

    @Test
    public void accepts_nullDate() throws Exception {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", CheckSum.parse("12345"), null, null, null, null, null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "3", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null, null, null, null));
        ExecutedAfterChangeSetFilter filter = new ExecutedAfterChangeSetFilter(new Date(), ranChanges);

        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());
    }

    @Test
    public void accepts() throws Exception {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(new
            Date().getTime() - (10 * 1000 * 60 * 60)), null, null, null, null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(new
            Date().getTime() - (8 * 1000 * 60 * 60)), null, null, null, null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "3", "testAuthor", CheckSum.parse("12345"), new Date(new
            Date().getTime() - (4 * 1000 * 60 * 60)), null, null, null, null, null, null, null));
        ExecutedAfterChangeSetFilter filter = new ExecutedAfterChangeSetFilter(new Date(new Date().getTime() - (6 *
            1000 * 60 * 60)), ranChanges);

        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog",  null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet("2", "testAuthor", false, false, "path/changelog",  null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog",null, null, null)).isAccepted());

    }
}
