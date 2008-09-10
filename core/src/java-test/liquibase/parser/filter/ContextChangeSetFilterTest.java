package liquibase.parser.filter;

import liquibase.ChangeSet;
import static org.junit.Assert.*;
import org.junit.Test;

public class ContextChangeSetFilterTest {

    @Test
    public void emptyContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter();

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test1, test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)));
    }

    @Test
    public void nullContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(null);

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test1, test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)));
    }

    @Test
    public void nullListContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new String[] {null});

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null,  "test1, test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)));
    }

    @Test
    public void singleContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter("TEST1");

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null,null,  "test1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null,null,  "test1, test2", null)));
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null,null,  "test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)));
    }

    @Test
    public void multiContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter("test1", "test2");

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null,null,  "test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null,null,  "test1, test2", null)));
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test3", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test3, test1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test3, TEST1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null,null,  null, null)));
    }

    @Test
    public void multiContextsSingeParameter() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter("test1, test2");

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test2", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test1, test2", null)));
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test3", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test3, test1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "test3, TEST1", null)));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)));
    }
}
