package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountChangeSetFilterTest  {

    @Test
    public void acceptsZeroCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(0);
        assertFalse(filter.accepts(new ChangeSet("a1","b1",false, false, "c1", null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet("a2","b2",false, false, "c2", null, null, null)).isAccepted());
    }

    @Test
    public void acceptsOneCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(1);
        assertTrue(filter.accepts(new ChangeSet("a1","b1",false, false, "c1", null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet("a2","b2",false, false, "c2", null, null, null)).isAccepted());
    }

    @Test
    public void acceptsTwoCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(2);
        assertTrue(filter.accepts(new ChangeSet("a1","b1",false, false, "c1", null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet("a2","b2",false, false, "c2", null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet("a3","b3",false, false, "c3", null, null, null)).isAccepted());
    }
}
