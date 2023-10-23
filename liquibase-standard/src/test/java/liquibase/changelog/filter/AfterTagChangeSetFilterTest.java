package liquibase.changelog.filter;

import liquibase.TagVersionEnum;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.command.core.RollbackCommandStep;
import liquibase.exception.RollbackFailedException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

public class AfterTagChangeSetFilterTest {

    @Test
    public void accepts_noTag() throws Exception {
        try {
            new AfterTagChangeSetFilter("tag1", new ArrayList<RanChangeSet>(), TagVersionEnum.OLDEST);
            fail("Did not throw exception");
        } catch (RollbackFailedException e) {
            ; //what we wanted
        }
    }

    @Test
    public void accepts() throws Exception {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(), "tag1", null, null, null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "3", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null, null, null, null));
        AfterTagChangeSetFilter filter = new AfterTagChangeSetFilter("tag1", ranChanges, TagVersionEnum.OLDEST);

        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());

    }
}
