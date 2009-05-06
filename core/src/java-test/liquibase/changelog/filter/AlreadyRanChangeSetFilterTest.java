package liquibase.changelog.filter;

import liquibase.ChangeSet;
import liquibase.RanChangeSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class AlreadyRanChangeSetFilterTest {

    @Test
    public void accepts_noneRun() {
        AlreadyRanChangeSetFilter filter = new AlreadyRanChangeSetFilter(new ArrayList<RanChangeSet>());

        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog",null,  null, null)));
    }

    @Test
    public void accepts() {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", "12345", new Date(), null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", "12345", new Date(), null));
        AlreadyRanChangeSetFilter filter = new AlreadyRanChangeSetFilter(ranChanges);

        //everything same
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog",null,  null, null)));

        //alwaysRun
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", true, false, "path/changelog",null,  null, null)));

        //run on change
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", false, true, "path/changelog", null, null, null)));

        //different id
        assertFalse(filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog", null, null, null)));

        //different author
        assertFalse(filter.accepts(new ChangeSet("1", "otherAuthor", false, false, "path/changelog", null, null, null)));

        //different path
        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "other/changelog", null, null, null)));
    }
}
