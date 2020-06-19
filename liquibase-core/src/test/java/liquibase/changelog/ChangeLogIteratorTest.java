package liquibase.changelog;

import liquibase.Contexts;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static liquibase.util.Validate.fail;
import static org.junit.Assert.assertEquals;

public class ChangeLogIteratorTest {
    private DatabaseChangeLog changeLog;

    @Before
    public void setUp() {
        changeLog = new DatabaseChangeLog();
        changeLog.addChangeSet(new ChangeSet("1", "nvoxland", false, false, "/path/to/changelog", "test1", "mysql", null));
        changeLog.addChangeSet(new ChangeSet("2", "nvoxland", false, false, "/path/to/changelog",  "test1", "oracle", null));
        changeLog.addChangeSet(new ChangeSet("3", "nvoxland", false, false, "/path/to/changelog",  "test2", "mysql", null));
        changeLog.addChangeSet(new ChangeSet("4", "nvoxland", false, false, "/path/to/changelog",  null, null, null));
        changeLog.addChangeSet(new ChangeSet("5", "nvoxland", false, false, "/path/to/changelog",  null, "mysql", null));
        changeLog.addChangeSet(new ChangeSet("6", "nvoxland", false, false, "/path/to/changelog",  "test2", null, null));
        changeLog.addChangeSet(new ChangeSet("7", "nvoxland", false, false, "/path/to/changelog",  "test2", null, "jdbc", true, ObjectQuotingStrategy.LEGACY, null));
    }

    @Test
    public void runChangeSet_emptyFiltersIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog);
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(7, testChangeLogVisitor.visitedChangeSets.size());
    }

    @Test
    public void runChangeSet_withBogusExecutor() throws Exception {
        changeLog.addChangeSet(new ChangeSet("8", "nvoxland", false, false,
                "/path/to/changelog",  "test2", null,
                "foo", true, ObjectQuotingStrategy.LEGACY, null));
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog);
        try {
            iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
            fail("No exception thrown.  Expected LiquibaseException for invalid Executor");
        }
        catch (LiquibaseException e) {
            boolean b = e.getMessage().contains("Unable to locate Executor");
            assertEquals(b, true);
        }
        assertEquals(7, testChangeLogVisitor.visitedChangeSets.size());
    }

    @Test
    public void runChangeSet_withExecutors() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog);
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(7, testChangeLogVisitor.visitedChangeSets.size());
    }

    @Test
    public void runChangeSet_singleFilterIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter(new Contexts("test1")));
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(4, testChangeLogVisitor.visitedChangeSets.size());
    }

    @Test
    public void runChangeSet_doubleFilterIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter(new Contexts("test1")), new DbmsChangeSetFilter(new MySQLDatabase()));
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(3, testChangeLogVisitor.visitedChangeSets.size());
        assertEquals("1", testChangeLogVisitor.visitedChangeSets.get(0).getId());
        assertEquals("4", testChangeLogVisitor.visitedChangeSets.get(1).getId());
        assertEquals("5", testChangeLogVisitor.visitedChangeSets.get(2).getId());

    }

    @Test
    public void runChangeSet_reverseVisitor() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new ReverseChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter(new Contexts("test1")), new DbmsChangeSetFilter(new MySQLDatabase()));
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(3, testChangeLogVisitor.visitedChangeSets.size());
        assertEquals("5", testChangeLogVisitor.visitedChangeSets.get(0).getId());
        assertEquals("4", testChangeLogVisitor.visitedChangeSets.get(1).getId());
        assertEquals("1", testChangeLogVisitor.visitedChangeSets.get(2).getId());
    }

    private static class TestChangeSetVisitor implements ChangeSetVisitor {

        public List<ChangeSet> visitedChangeSets = new ArrayList<ChangeSet>();


        @Override
        public Direction getDirection() {
            return ChangeSetVisitor.Direction.FORWARD;
        }

        @Override
        public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
            visitedChangeSets.add(changeSet);
        }
    }

    private static class ReverseChangeSetVisitor extends TestChangeSetVisitor {

        @Override
        public Direction getDirection() {
            return ChangeSetVisitor.Direction.REVERSE;
        }
    }
}
