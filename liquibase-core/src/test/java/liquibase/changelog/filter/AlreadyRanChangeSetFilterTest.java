package liquibase.changelog.filter;

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class AlreadyRanChangeSetFilterTest {

    @Test
    public void accepts_noneRun() {
        AlreadyRanChangeSetFilter filter = new AlreadyRanChangeSetFilter(new ArrayList<RanChangeSet>());

        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog",null, null, null)).isAccepted());
    }

    @Test
    public void accepts() {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));
        AlreadyRanChangeSetFilter filter = new AlreadyRanChangeSetFilter(ranChanges);

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
}
