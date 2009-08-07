package liquibase.changelog;

import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ChangeLogIteratorTest {
    private DatabaseChangeLog changeLog;

    @Before
    public void setUp() {
        changeLog = new DatabaseChangeLog("path/to/changelog");
        changeLog.addChangeSet(new ChangeSet("1", "nvoxland", false, false, "/path/to/changelog", null, "test1", "mysql"));
        changeLog.addChangeSet(new ChangeSet("2", "nvoxland", false, false, "/path/to/changelog",null,  "test1", "oracle"));
        changeLog.addChangeSet(new ChangeSet("3", "nvoxland", false, false, "/path/to/changelog",null,  "test2", "mysql"));
        changeLog.addChangeSet(new ChangeSet("4", "nvoxland", false, false, "/path/to/changelog",null,  null, null));
        changeLog.addChangeSet(new ChangeSet("5", "nvoxland", false, false, "/path/to/changelog",null,  null, "mysql"));
        changeLog.addChangeSet(new ChangeSet("6", "nvoxland", false, false, "/path/to/changelog",null,  "test2", null));
    }

    @Test
    public void runChangeSet_emptyFiltersIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog);
        iterator.run(testChangeLogVisitor, null);
        assertEquals(6, testChangeLogVisitor.visitedChangeSets.size());
    }

    @Test
    public void runChangeSet_singleFilterIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter("test1"));
        iterator.run(testChangeLogVisitor, null);
        assertEquals(4, testChangeLogVisitor.visitedChangeSets.size());
    }

    @Test
    public void runChangeSet_doubleFilterIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter("test1"), new DbmsChangeSetFilter(new MySQLDatabase()));
        iterator.run(testChangeLogVisitor, null);
        assertEquals(3, testChangeLogVisitor.visitedChangeSets.size());
        assertEquals("1", testChangeLogVisitor.visitedChangeSets.get(0).getId());
        assertEquals("4", testChangeLogVisitor.visitedChangeSets.get(1).getId());
        assertEquals("5", testChangeLogVisitor.visitedChangeSets.get(2).getId());

    }

    @Test
    public void runChangeSet_reverseVisitor() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new ReverseChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter("test1"), new DbmsChangeSetFilter(new MySQLDatabase()));
        iterator.run(testChangeLogVisitor, null);
        assertEquals(3, testChangeLogVisitor.visitedChangeSets.size());
        assertEquals("5", testChangeLogVisitor.visitedChangeSets.get(0).getId());
        assertEquals("4", testChangeLogVisitor.visitedChangeSets.get(1).getId());
        assertEquals("1", testChangeLogVisitor.visitedChangeSets.get(2).getId());
    }

    private static class TestChangeSetVisitor implements ChangeSetVisitor {

        public List<ChangeSet> visitedChangeSets = new ArrayList<ChangeSet>();


        public Direction getDirection() {
            return ChangeSetVisitor.Direction.FORWARD;
        }

        public void visit(ChangeSet changeSet, Database database) {
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
