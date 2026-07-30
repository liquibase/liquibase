package liquibase.changelog.filter;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.sql.visitor.AbstractSqlVisitor;
import org.junit.Test;

import static org.junit.Assert.*;

public class ContextChangeSetFilterTest {


    private static final class TestSqlVisitor extends AbstractSqlVisitor {

        public TestSqlVisitor(final String... contexts) {
            setContextFilter(new ContextExpression(contexts));
        }

        @Override
        public String modifySql(String sql, Database database) {
            throw new UnsupportedOperationException("modifySql has not been implemented");
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException("getName has not been implemented");
        }

        @Override
        public String getSerializedObjectNamespace() {
            return STANDARD_CHANGELOG_NAMESPACE;
        }


    }


    @Test
    public void emptyContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new Contexts());

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, @test2", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, @test2", null, null)).isAccepted());
    }

    @Test
    public void nullContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter();

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, @test2", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, @test2", null, null)).isAccepted());
    }

    @Test public void reallyNullContexts(){
        ContextChangeSetFilter filter = new ContextChangeSetFilter(null);

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, @test2", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, @test2", null, null)).isAccepted());
    }

    @Test
    public void nullListContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new Contexts(""));

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, @test2", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, @test2", null, null)).isAccepted());
    }

    @Test
    public void singleContexts() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new Contexts("TEST1"));

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, test2", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, @test2", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, @test2", null, null)).isAccepted());
    }

    @Test
    public void requiredContext() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new Contexts("@required"));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "required", null, null)).isAccepted());
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@required", null, null)).isAccepted());
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)).isAccepted());
    }

    @Test
    public void multiContexts() {
        ContextChangeSetFilter[] filterArray = {
                new ContextChangeSetFilter(new Contexts("test1", "test2")),
                new ContextChangeSetFilter(new Contexts("test1, test2")),
                new ContextChangeSetFilter(new Contexts("test1,test2"))
        };

        for (ContextChangeSetFilter filter : filterArray) {

            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test2", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, test2", null, null)).isAccepted());
            assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "test3", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test3, test1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test3, TEST1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test2", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, test2", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test1, @test2", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test1, @test2", null, null)).isAccepted());
            assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, "@test3", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test3, test1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test3, @test1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test3, @test1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test3, TEST1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "test3, @TEST1", null, null)).isAccepted());
            assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, "@test3, @TEST1", null, null)).isAccepted());
        }
    }


    @Test
    public void visitorContextFilterLowerLower() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new Contexts("test1"));

        ChangeSet changeSet = new ChangeSet(null, null, false, false, null, null, null, null);
        changeSet.addSqlVisitor(new TestSqlVisitor("test1"));

        assertTrue(filter.accepts(changeSet).isAccepted());

        assertEquals(1, changeSet.getSqlVisitors().size());
    }

    @Test
    public void visitorContextFilterUpperLower() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new Contexts("TEST1"));

        ChangeSet changeSet = new ChangeSet(null, null, false, false, null, null, null, null);
        changeSet.addSqlVisitor(new TestSqlVisitor("test1"));

        assertTrue(filter.accepts(changeSet).isAccepted());

        assertEquals(1, changeSet.getSqlVisitors().size());
    }

    @Test
    public void visitorContextFilterUpperUpper() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new Contexts("TEST1"));

        ChangeSet changeSet = new ChangeSet(null, null, false, false, null, null, null, null);
        changeSet.addSqlVisitor(new TestSqlVisitor("TEST1"));

        assertTrue(filter.accepts(changeSet).isAccepted());

        assertEquals(1, changeSet.getSqlVisitors().size());
    }

    @Test
    public void visitorContextFilterLowerUpper() {
        ContextChangeSetFilter filter = new ContextChangeSetFilter(new Contexts("test1"));

        ChangeSet changeSet = new ChangeSet(null, null, false, false, null, null, null, null);
        changeSet.addSqlVisitor(new TestSqlVisitor("TEST1"));

        assertTrue(filter.accepts(changeSet).isAccepted());

        assertEquals(1, changeSet.getSqlVisitors().size());
    }

    @Test
    public void nocontextsPseudoContext() {
        ContextChangeSetFilter empty = new ContextChangeSetFilter(new Contexts());
        ContextChangeSetFilter withMyContext = new ContextChangeSetFilter(new Contexts("mycontext"));

        // Empty runtime + `!nocontexts AND mycontext` -> author opted into strict matching, so the
        // changeset must be skipped instead of matching by the legacy "empty matches everything" rule.
        assertFalse(empty.accepts(new ChangeSet(null, null, false, false, null, "!nocontexts AND mycontext", null, null)).isAccepted());

        // Empty runtime + `nocontexts OR fallback` -> nocontexts evaluates to true, applies.
        assertTrue(empty.accepts(new ChangeSet(null, null, false, false, null, "nocontexts OR fallback", null, null)).isAccepted());

        // Empty runtime + bare `nocontexts` -> applies.
        assertTrue(empty.accepts(new ChangeSet(null, null, false, false, null, "nocontexts", null, null)).isAccepted());

        // Runtime contexts provided + `!nocontexts AND mycontext` -> applies when mycontext is active.
        assertTrue(withMyContext.accepts(new ChangeSet(null, null, false, false, null, "!nocontexts AND mycontext", null, null)).isAccepted());

        // Legacy behaviour preserved: empty runtime + ordinary context still applies.
        assertTrue(empty.accepts(new ChangeSet(null, null, false, false, null, "mycontext", null, null)).isAccepted());
    }
}
