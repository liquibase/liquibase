package liquibase.changelog.parser.xml;

import static org.junit.Assert.*;

import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.changelog.parser.xml.XMLChangeLogParser;
import liquibase.change.AddColumnChange;
import liquibase.change.Change;
import liquibase.change.CreateTableChange;
import liquibase.change.RawSQLChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.change.custom.ExampleCustomSqlChange;
import liquibase.exception.ChangeLogParseException;
import liquibase.preconditions.OrPrecondition;
import liquibase.preconditions.Preconditions;
import liquibase.test.JUnitFileOpener;

import org.junit.Test;

import java.util.HashMap;

public class XMLChangeLogParserTest {

    @Test
    public void simpleChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/changelog/parser/xml/simpleChangeLog.xml", new HashMap<String, Object>(), new JUnitFileOpener());

        assertEquals("liquibase/changelog/parser/xml/simpleChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/changelog/parser/xml/simpleChangeLog.xml", changeLog.getPhysicalFilePath());

        assertEquals(0, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(1, changeLog.getChangeSets().size());

        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/changelog/parser/xml/simpleChangeLog.xml", changeSet.getFilePath());
        assertEquals("Some comments go here", changeSet.getComments());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getChangeMetaData().getName());
        assertTrue(change instanceof CreateTableChange);
    }

    @Test
    public void multiChangeSetChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/changelog/parser/xml/multiChangeSetChangeLog.xml", new HashMap<String, Object>(), new JUnitFileOpener());

        assertEquals("liquibase/changelog/parser/xml/multiChangeSetChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/changelog/parser/xml/multiChangeSetChangeLog.xml", changeLog.getPhysicalFilePath());

        assertEquals(0, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(4, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/changelog/parser/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertNull(changeSet.getComments());
        assertFalse(changeSet.shouldAlwaysRun());
        assertFalse(changeSet.shouldRunOnChange());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getChangeMetaData().getName());
        assertTrue(change instanceof CreateTableChange);

        // change 1
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(2, changeSet.getChanges().size());
        assertEquals("liquibase/changelog/parser/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertEquals("Testing add column", changeSet.getComments());
        assertTrue(changeSet.shouldAlwaysRun());
        assertTrue(changeSet.shouldRunOnChange());
        assertEquals(2, changeSet.getRollBackChanges().length);
        assertTrue(changeSet.getRollBackChanges()[0] instanceof RawSQLChange);
        assertTrue(changeSet.getRollBackChanges()[1] instanceof RawSQLChange);

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", change.getChangeMetaData().getName());
        assertTrue(change instanceof AddColumnChange);

        change = changeSet.getChanges().get(1);
        assertEquals("addColumn", change.getChangeMetaData().getName());
        assertTrue(change instanceof AddColumnChange);

        // change 2
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("bob", changeSet.getAuthor());
        assertEquals("3", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/changelog/parser/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertNull(changeSet.getComments());
        assertFalse(changeSet.shouldAlwaysRun());
        assertFalse(changeSet.shouldRunOnChange());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getChangeMetaData().getName());
        assertTrue(change instanceof CreateTableChange);

    
        // change 3
        changeSet = changeLog.getChangeSets().get(3);
        assertEquals(1, changeSet.getChanges().size());

        change = changeSet.getChanges().get(0);
        assertTrue(change instanceof CustomChangeWrapper);
        CustomChangeWrapper wrapper = (CustomChangeWrapper) change;
        wrapper.generateStatements(null);
        assertTrue(wrapper.getCustomChange() instanceof ExampleCustomSqlChange);
        ExampleCustomSqlChange exChg = (ExampleCustomSqlChange) wrapper.getCustomChange();
        assertEquals("table", exChg.getTableName());
        assertEquals("column", exChg.getColumnName());

    }

    @Test
    public void logicalPathChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/changelog/parser/xml/logicalPathChangeLog.xml", new HashMap<String, Object>(), new JUnitFileOpener());

        assertEquals("liquibase/parser-logical/xml/logicalPathChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/changelog/parser/xml/logicalPathChangeLog.xml", changeLog.getPhysicalFilePath());

        assertEquals(0, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("liquibase/parser-logical/xml/logicalPathChangeLog.xml", changeLog.getChangeSets().get(0).getFilePath());

    }

    @Test
    public void preconditionsChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/changelog/parser/xml/preconditionsChangeLog.xml", new HashMap<String, Object>(), new JUnitFileOpener());

        assertEquals("liquibase/changelog/parser/xml/preconditionsChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/changelog/parser/xml/preconditionsChangeLog.xml", changeLog.getPhysicalFilePath());

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
    	final String nestedFileName = "liquibase/changelog/parser/xml/nestedChangeLog.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/changelog/parser/xml/nestedChangeLog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        nestedFileAssertions(changeLog, nestedFileName);

    }

    @Test
    public void nestedRelativeChangeLog() throws Exception {
    	final String nestedFileName = "liquibase/changelog/parser/xml/nestedRelativeChangeLog.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse(nestedFileName, new HashMap<String, Object>(), new JUnitFileOpener());
        nestedFileAssertions(changeLog, nestedFileName);

    }

    private void nestedFileAssertions(DatabaseChangeLog changeLog, String nestedFileName) {
        assertEquals(nestedFileName, changeLog.getLogicalFilePath());
        assertEquals(nestedFileName, changeLog.getPhysicalFilePath());

        assertEquals(1, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(0, ((Preconditions) changeLog.getPreconditions().getNestedPreconditions().get(0)).getNestedPreconditions().size());
        assertEquals(3, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(nestedFileName, changeSet.getFilePath());


        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getChangeMetaData().getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("employee", ((CreateTableChange) change).getTableName());

        // change 1 (from included simple change log)
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/changelog/parser/xml/simpleChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getChangeMetaData().getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("person", ((CreateTableChange) change).getTableName());

        // change 2
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(nestedFileName, changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", change.getChangeMetaData().getName());
        assertTrue(change instanceof AddColumnChange);
        assertEquals("employee", ((AddColumnChange) change).getTableName());
	}


    @Test
    public void doubleNestedChangeLog() throws Exception {
    	final String doubleNestedFileName = "liquibase/changelog/parser/xml/doubleNestedChangeLog.xml";
    	final String nestedFileName = "liquibase/changelog/parser/xml/nestedChangeLog.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse(doubleNestedFileName, new HashMap<String, Object>(), new JUnitFileOpener());

        doubleNestedFileAssertions(doubleNestedFileName, nestedFileName,
				changeLog);
    }

    @Test
    public void doubleNestedRelativeChangeLog() throws Exception {
    	final String doubleNestedFileName = "liquibase/changelog/parser/xml/doublenestedRelativeChangeLog.xml";
    	final String nestedFileName = "liquibase/changelog/parser/xml/nestedRelativeChangeLog.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogParser().parse(doubleNestedFileName, new HashMap<String, Object>(), new JUnitFileOpener());

        doubleNestedFileAssertions(doubleNestedFileName, nestedFileName,
				changeLog);
    }

	private void doubleNestedFileAssertions(final String doubleNestedFileName,
			final String nestedFileName, DatabaseChangeLog changeLog) {
		assertEquals(doubleNestedFileName, changeLog.getLogicalFilePath());
        assertEquals(doubleNestedFileName, changeLog.getPhysicalFilePath());

		assertEquals(1, changeLog.getPreconditions().getNestedPreconditions().size());
        Preconditions nested = (Preconditions) changeLog.getPreconditions().getNestedPreconditions().get(0);
        assertEquals(1, nested.getNestedPreconditions().size());
        assertEquals(0, ((Preconditions) nested.getNestedPreconditions().get(0)).getNestedPreconditions().size());
        assertEquals(4, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(doubleNestedFileName, changeSet.getFilePath());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getChangeMetaData().getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("partner", ((CreateTableChange) change).getTableName());

        // change 1 from nestedChangeLog
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(nestedFileName, changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getChangeMetaData().getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("employee", ((CreateTableChange) change).getTableName());

        // change 2 (from included simple change log)
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/changelog/parser/xml/simpleChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", change.getChangeMetaData().getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("person", ((CreateTableChange) change).getTableName());

        // change 3 from nested Change log
        changeSet = changeLog.getChangeSets().get(3);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(nestedFileName, changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", change.getChangeMetaData().getName());
        assertTrue(change instanceof AddColumnChange);
        assertEquals("employee", ((AddColumnChange) change).getTableName());
	}

    @Test
    public void missingChangeLog() throws Exception {
        try {
            @SuppressWarnings("unused")
			DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/changelog/parser/xml/missingChangeLog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        } catch (Exception e) {
            assertTrue(e instanceof ChangeLogParseException);
            assertEquals("liquibase/changelog/parser/xml/missingChangeLog.xml does not exist", e.getMessage());

        }
    }

    @Test
    public void malformedChangeLog() throws Exception {
        try {
            DatabaseChangeLog changeLog = new XMLChangeLogParser().parse("liquibase/changelog/parser/xml/malformedChangeLog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        } catch (Exception e) {
            assertTrue(e instanceof ChangeLogParseException);
            assertTrue(e.getMessage().startsWith("Error parsing line"));

        }
    }

    @Test
    public void sampleChangeLogs() throws Exception {
        new XMLChangeLogParser().parse("changelogs/cache/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/db2/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/derby/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/firebird/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/h2/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/hsqldb/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/maxdb/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/mysql/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/oracle/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/pgsql/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/sybase/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/asany/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
        new XMLChangeLogParser().parse("changelogs/unsupported/complete/root.changelog.xml", new HashMap<String, Object>(), new JUnitFileOpener());
    }
}
