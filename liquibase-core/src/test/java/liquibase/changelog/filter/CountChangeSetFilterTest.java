package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import static org.junit.Assert.*;
import org.junit.Test;

public class CountChangeSetFilterTest  {

    @Test
    public void acceptsZeroCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(0);
        assertFalse(filter.accepts(new ChangeSet("a1","b1",false, false, "c1", null, null, null)));
        assertFalse(filter.accepts(new ChangeSet("a2","b2",false, false, "c2", null, null, null)));
    }

    @Test
    public void acceptsOneCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(1);
        assertTrue(filter.accepts(new ChangeSet("a1","b1",false, false, "c1", null, null, null)));
        assertFalse(filter.accepts(new ChangeSet("a2","b2",false, false, "c2", null, null, null)));
    }

    @Test
    public void acceptsTwoCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(2);
        assertTrue(filter.accepts(new ChangeSet("a1","b1",false, false, "c1", null, null, null)));
        assertTrue(filter.accepts(new ChangeSet("a2","b2",false, false, "c2", null, null, null)));
        assertFalse(filter.accepts(new ChangeSet("a3","b3",false, false, "c3", null, null, null)));
    }
}
