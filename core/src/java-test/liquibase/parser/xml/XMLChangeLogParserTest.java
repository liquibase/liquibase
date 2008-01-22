package liquibase.parser.xml;

import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.change.AddColumnChange;
import liquibase.change.Change;
import liquibase.change.CreateTableChange;
import liquibase.database.sql.RawSqlStatement;
import liquibase.exception.ChangeLogParseException;
import liquibase.preconditions.OrPrecondition;
import liquibase.test.JUnitFileOpener;
import static org.junit.Assert.*;
import org.junit.Test;

public class XMLChangeLogParserTest {

    @Test
    public void simpleChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/parser/xml/simpleChangeLog.xml", new JUnitFileOpener());

        assertEquals("liquibase/parser/xml/simpleChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/xml/simpleChangeLog.xml", changeLog.getPhysicalFilePath());

        assertNull(changeLog.getPreconditions());
        assertEquals(1, changeLog.getChangeSets().size());

        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/simpleChangeLog.xml", changeSet.getFilePath());
        assertEquals("Some comments go here", changeSet.getComments());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getTagName());
        assertTrue(change instanceof CreateTableChange);
    }

    @Test
    public void multiChangeSetChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/parser/xml/multiChangeSetChangeLog.xml", new JUnitFileOpener());

        assertEquals("liquibase/parser/xml/multiChangeSetChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/xml/multiChangeSetChangeLog.xml", changeLog.getPhysicalFilePath());

        assertNull(changeLog.getPreconditions());
        assertEquals(3, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertNull(changeSet.getComments());
        assertFalse(changeSet.shouldAlwaysRun());
        assertFalse(changeSet.shouldRunOnChange());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getTagName());
        assertTrue(change instanceof CreateTableChange);

        // change 1
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(2, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertEquals("Testing add column", changeSet.getComments());
        assertTrue(changeSet.shouldAlwaysRun());
        assertTrue(changeSet.shouldRunOnChange());
        assertEquals(2, changeSet.getRollBackStatements().length);
        assertTrue(changeSet.getRollBackStatements()[0] instanceof RawSqlStatement);
        assertTrue(changeSet.getRollBackStatements()[1] instanceof RawSqlStatement);

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", change.getTagName());
        assertTrue(change instanceof AddColumnChange);

        change = changeSet.getChanges().get(1);
        assertEquals("addColumn", change.getTagName());
        assertTrue(change instanceof AddColumnChange);

        // change 2
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("bob", changeSet.getAuthor());
        assertEquals("3", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertNull(changeSet.getComments());
        assertFalse(changeSet.shouldAlwaysRun());
        assertFalse(changeSet.shouldRunOnChange());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getTagName());
        assertTrue(change instanceof CreateTableChange);
    }

    @Test
    public void logicalPathChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/parser/xml/logicalPathChangeLog.xml", new JUnitFileOpener());

        assertEquals("liquibase/parser-logical/xml/logicalPathChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/xml/logicalPathChangeLog.xml", changeLog.getPhysicalFilePath());

        assertNull(changeLog.getPreconditions());
        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("liquibase/parser-logical/xml/logicalPathChangeLog.xml", changeLog.getChangeSets().get(0).getFilePath());

    }

    @Test
    public void preconditionsChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/parser/xml/preconditionsChangeLog.xml", new JUnitFileOpener());

        assertEquals("liquibase/parser/xml/preconditionsChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/xml/preconditionsChangeLog.xml", changeLog.getPhysicalFilePath());

        assertNotNull(changeLog.getPreconditions());
        assertEquals(2, changeLog.getPreconditions().getNestedPreconditions().size());

        assertEquals("runningAs", changeLog.getPreconditions().getNestedPreconditions().get(0).getTagName());

        assertEquals("or", changeLog.getPreconditions().getNestedPreconditions().get(1).getTagName());
        assertEquals("dbms", ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions().get(1)).getNestedPreconditions().get(0).getTagName());
        assertEquals("dbms", ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions().get(1)).getNestedPreconditions().get(1).getTagName());

        assertEquals(1, changeLog.getChangeSets().size());
    }

    @Test
    public void testNestedChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/parser/xml/nestedChangeLog.xml", new JUnitFileOpener());

        assertEquals("liquibase/parser/xml/nestedChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/xml/nestedChangeLog.xml", changeLog.getPhysicalFilePath());

        assertNull(changeLog.getPreconditions());
        assertEquals(3, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/nestedChangeLog.xml", changeSet.getFilePath());


        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getTagName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("employee", ((CreateTableChange) change).getTableName());

        // change 1 (from included simple change log)
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/simpleChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getTagName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("person", ((CreateTableChange) change).getTableName());

        // change 2
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/nestedChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", change.getTagName());
        assertTrue(change instanceof AddColumnChange);
        assertEquals("employee", ((AddColumnChange) change).getTableName());
    }

    @Test
    public void doubleNestedChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/parser/xml/doubleNestedChangeLog.xml", new JUnitFileOpener());

        assertEquals("liquibase/parser/xml/doubleNestedChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/xml/doubleNestedChangeLog.xml", changeLog.getPhysicalFilePath());

        assertNull(changeLog.getPreconditions());
        assertEquals(4, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/doubleNestedChangeLog.xml", changeSet.getFilePath());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getTagName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("partner", ((CreateTableChange) change).getTableName());

        // change 1 from nestedChangeLog
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/nestedChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getTagName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("employee", ((CreateTableChange) change).getTableName());

        // change 2 (from included simple change log)
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/simpleChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getTagName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("person", ((CreateTableChange) change).getTableName());

        // change 3 from nested Change log
        changeSet = changeLog.getChangeSets().get(3);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/xml/nestedChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", change.getTagName());
        assertTrue(change instanceof AddColumnChange);
        assertEquals("employee", ((AddColumnChange) change).getTableName());
    }

    @Test
    public void missingChangeLog() throws Exception {
        try {
            DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/parser/xml/missingChangeLog.xml", new JUnitFileOpener());
        } catch (Exception e) {
            assertTrue(e instanceof ChangeLogParseException);
            assertEquals("liquibase/parser/xml/missingChangeLog.xml does not exist", e.getMessage());

        }
    }

    @Test
    public void malformedChangeLog() throws Exception {
        try {
            DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/parser/xml/malformedChangeLog.xml", new JUnitFileOpener());
        } catch (Exception e) {
            assertTrue(e instanceof ChangeLogParseException);
            assertTrue(e.getMessage().startsWith("Error parsing line"));

        }
    }

    @Test
    public void sampleChangeLogs() throws Exception {
        new XMLChangeLogParser().parse("changelogs/cache/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/db2/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/derby/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/firebird/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/h2/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/hsqldb/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/maxdb/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/mysql/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/oracle/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/pgsql/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/sybase/complete/root.changelog.xml", new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/unsupported/complete/root.changelog.xml", new JUnitFileOpener());
    }
}
