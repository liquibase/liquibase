package liquibase.parser.filter;

import liquibase.ChangeSet;
import liquibase.RanChangeSet;
import liquibase.exception.RollbackFailedException;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class AfterTagChangeSetFilterTest {

    @Test
    public void accepts_noTag() throws Exception {
        try {
            new AfterTagChangeSetFilter("tag1", new ArrayList<RanChangeSet>());
            fail("Did not throw exception");
        } catch (RollbackFailedException e) {
            ; //what we wanted
        }
    }

    @Test
    public void accepts() throws Exception {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", "12345", new Date(), null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", "12345", new Date(), "tag1"));
        ranChanges.add(new RanChangeSet("path/changelog", "3", "testAuthor", "12345", new Date(), null));
        AfterTagChangeSetFilter filter = new AfterTagChangeSetFilter("tag1", ranChanges);

        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)));
        assertFalse(filter.accepts(new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null)));
        assertTrue(filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog", null, null, null)));

    }
}
