package liquibase.parser.core.xml;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.RawSQLChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.change.custom.ExampleCustomSqlChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.core.MockDatabase;
import liquibase.exception.ChangeLogParseException;
import liquibase.precondition.core.OrPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.test.JUnitResourceAccessor;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class XMLChangeLogSAXParserTest {

    @Before
    public void before() {
        LiquibaseConfiguration.getInstance().reset();
    }

    @Test
    public void simpleChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/simpleChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());

        assertEquals("liquibase/parser/core/xml/simpleChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/core/xml/simpleChangeLog.xml", changeLog.getPhysicalFilePath());

        assertEquals(0, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(1, changeLog.getChangeSets().size());

        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/core/xml/simpleChangeLog.xml", changeSet.getFilePath());
        assertEquals("Some comments go here", changeSet.getComments());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);
    }

    @Test
    public void multiChangeSetChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/multiChangeSetChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());

        assertEquals("liquibase/parser/core/xml/multiChangeSetChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/core/xml/multiChangeSetChangeLog.xml", changeLog.getPhysicalFilePath());

        assertEquals(0, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(4, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/core/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertNull(changeSet.getComments());
        assertFalse(changeSet.shouldAlwaysRun());
        assertFalse(changeSet.shouldRunOnChange());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);

        // change 1
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(2, changeSet.getChanges().size());
        assertEquals("liquibase/parser/core/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertEquals("Testing add column", changeSet.getComments());
        assertTrue(changeSet.shouldAlwaysRun());
        assertTrue(changeSet.shouldRunOnChange());
        assertEquals(2, changeSet.getRollBackChanges().length);
        assertTrue(changeSet.getRollBackChanges()[0] instanceof RawSQLChange);
        assertTrue(changeSet.getRollBackChanges()[1] instanceof RawSQLChange);

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof AddColumnChange);

        change = changeSet.getChanges().get(1);
        assertEquals("addColumn", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof AddColumnChange);

        // change 2
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("bob", changeSet.getAuthor());
        assertEquals("3", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/core/xml/multiChangeSetChangeLog.xml", changeSet.getFilePath());
        assertNull(changeSet.getComments());
        assertFalse(changeSet.shouldAlwaysRun());
        assertFalse(changeSet.shouldRunOnChange());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);


        // change 3
        changeSet = changeLog.getChangeSets().get(3);
        assertEquals(1, changeSet.getChanges().size());

        change = changeSet.getChanges().get(0);
        assertTrue(change instanceof CustomChangeWrapper);
        CustomChangeWrapper wrapper = (CustomChangeWrapper) change;
        wrapper.generateStatements(new MockDatabase());
        assertTrue(wrapper.getCustomChange() instanceof ExampleCustomSqlChange);
        ExampleCustomSqlChange exChg = (ExampleCustomSqlChange) wrapper.getCustomChange();
        assertEquals("table", exChg.getTableName());
        assertEquals("column", exChg.getColumnName());

    }

    @Test
    public void logicalPathChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/logicalPathChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());

        assertEquals("liquibase/parser-logical/xml/logicalPathChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/core/xml/logicalPathChangeLog.xml", changeLog.getPhysicalFilePath());

        assertEquals(0, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("liquibase/parser-logical/xml/logicalPathChangeLog.xml", changeLog.getChangeSets().get(0).getFilePath());

    }

    @Test
    public void preconditionsChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/preconditionsChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());

        assertEquals("liquibase/parser/core/xml/preconditionsChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/core/xml/preconditionsChangeLog.xml", changeLog.getPhysicalFilePath());

        assertNotNull(changeLog.getPreconditions());
        assertEquals(2, changeLog.getPreconditions().getNestedPreconditions().size());

        assertEquals("runningAs", changeLog.getPreconditions().getNestedPreconditions().get(0).getName());

        assertEquals("or", changeLog.getPreconditions().getNestedPreconditions().get(1).getName());
        assertEquals("dbms", ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions().get(1)).getNestedPreconditions().get(0).getName());
        assertEquals("dbms", ((OrPrecondition) changeLog.getPreconditions().getNestedPreconditions().get(1)).getNestedPreconditions().get(1).getName());

        assertEquals(1, changeLog.getChangeSets().size());
    }

    @Test
    public void testNestedChangeLog() throws Exception {
    	final String nestedFileName = "liquibase/parser/core/xml/nestedChangeLog.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/nestedChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());
        nestedFileAssertions(changeLog, nestedFileName);

    }

    @Test
    public void nestedRelativeChangeLog() throws Exception {
    	final String nestedFileName = "liquibase/parser/core/xml/nestedRelativeChangeLog.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(nestedFileName, new ChangeLogParameters(), new JUnitResourceAccessor());
        nestedFileAssertions(changeLog, nestedFileName);

    }

    private void nestedFileAssertions(DatabaseChangeLog changeLog, String nestedFileName) {
        assertEquals(nestedFileName, changeLog.getLogicalFilePath());
        assertEquals(nestedFileName, changeLog.getPhysicalFilePath());

        assertEquals(1, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(0, ((PreconditionContainer) changeLog.getPreconditions().getNestedPreconditions().get(0)).getNestedPreconditions().size());
        assertEquals(3, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(nestedFileName, changeSet.getFilePath());


        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("employee", ((CreateTableChange) change).getTableName());

        // change 1 (from included simple change log)
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/core/xml/simpleChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("person", ((CreateTableChange) change).getTableName());

        // change 2
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(nestedFileName, changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof AddColumnChange);
        assertEquals("employee", ((AddColumnChange) change).getTableName());
	}


    @Test
    public void doubleNestedChangeLog() throws Exception {
    	final String doubleNestedFileName = "liquibase/parser/core/xml/doubleNestedChangeLog.xml";
    	final String nestedFileName = "liquibase/parser/core/xml/nestedChangeLog.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(doubleNestedFileName, new ChangeLogParameters(), new JUnitResourceAccessor());

        doubleNestedFileAssertions(doubleNestedFileName, nestedFileName, changeLog);
    }

    @Test
    public void doubleNestedRelativeChangeLog() throws Exception {
    	final String doubleNestedFileName = "liquibase/parser/core/xml/doubleNestedRelativeChangeLog.xml";
    	final String nestedFileName = "liquibase/parser/core/xml/nestedRelativeChangeLog.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(doubleNestedFileName, new ChangeLogParameters(), new JUnitResourceAccessor());

        doubleNestedFileAssertions(doubleNestedFileName, nestedFileName,
				changeLog);
    }

	private void doubleNestedFileAssertions(final String doubleNestedFileName, final String nestedFileName, DatabaseChangeLog changeLog) {
		assertEquals(doubleNestedFileName, changeLog.getLogicalFilePath());
        assertEquals(doubleNestedFileName, changeLog.getPhysicalFilePath());

		assertEquals(1, changeLog.getPreconditions().getNestedPreconditions().size());
        PreconditionContainer nested = (PreconditionContainer) changeLog.getPreconditions().getNestedPreconditions().get(0);
        assertEquals(1, nested.getNestedPreconditions().size());
        assertEquals(0, ((PreconditionContainer) nested.getNestedPreconditions().get(0)).getNestedPreconditions().size());
        assertEquals(4, changeLog.getChangeSets().size());

        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(doubleNestedFileName, changeSet.getFilePath());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("partner", ((CreateTableChange) change).getTableName());

        // change 1 from nestedChangeLog
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(nestedFileName, changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("employee", ((CreateTableChange) change).getTableName());

        // change 2 (from included simple change log)
        changeSet = changeLog.getChangeSets().get(2);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/core/xml/simpleChangeLog.xml", changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);
        assertEquals("person", ((CreateTableChange) change).getTableName());

        // change 3 from nested Change log
        changeSet = changeLog.getChangeSets().get(3);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals(nestedFileName, changeSet.getFilePath());

        change = changeSet.getChanges().get(0);
        assertEquals("addColumn", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof AddColumnChange);
        assertEquals("employee", ((AddColumnChange) change).getTableName());
	}

    @Test
    public void missingChangeLog() throws Exception {
        try {
            @SuppressWarnings("unused")
			DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/changelog/parser/xml/missingChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());
        } catch (Exception e) {
            assertTrue(e instanceof ChangeLogParseException);
            assertEquals("liquibase/changelog/parser/xml/missingChangeLog.xml does not exist", e.getMessage());

        }
    }

    @Test
    public void malformedChangeLog() throws Exception {
        try {
            DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/malformedChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());
        } catch (Exception e) {
            assertTrue(e instanceof ChangeLogParseException);
            assertTrue(e.getMessage().startsWith("Error parsing line"));

        }
    }

    @Test
    public void otherNamespaceAttributesChangeLog() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/simpleChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());

        assertEquals("liquibase/parser/core/xml/simpleChangeLog.xml", changeLog.getLogicalFilePath());
        assertEquals("liquibase/parser/core/xml/simpleChangeLog.xml", changeLog.getPhysicalFilePath());

        assertEquals(0, changeLog.getPreconditions().getNestedPreconditions().size());
        assertEquals(1, changeLog.getChangeSets().size());

        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("1", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("liquibase/parser/core/xml/simpleChangeLog.xml", changeSet.getFilePath());
        assertEquals("Some comments go here", changeSet.getComments());

        Change change = changeSet.getChanges().get(0);
        assertEquals("createTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertTrue(change instanceof CreateTableChange);
    }

	@Test
	public void rawSqlParameter() throws Exception {
		ChangeLogParameters params = new ChangeLogParameters();
		params.set("tablename", "rawsql");
		DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/rawSqlParameters.xml", params, new JUnitResourceAccessor());

		assertEquals("liquibase/parser/core/xml/rawSqlParameters.xml", changeLog.getLogicalFilePath());
		assertEquals("liquibase/parser/core/xml/rawSqlParameters.xml", changeLog.getPhysicalFilePath());

		assertEquals(1, changeLog.getChangeSets().size());

		// change 0
		ChangeSet changeSet = changeLog.getChangeSets().get(0);
		assertEquals("paikens", changeSet.getAuthor());
		assertEquals("1", changeSet.getId());
		assertEquals("liquibase/parser/core/xml/rawSqlParameters.xml", changeSet.getFilePath());
		assertEquals(1, changeSet.getChanges().size());
		assertTrue(changeSet.getChanges().get(0) instanceof RawSQLChange);
		assertEquals("create table rawsql;", ((RawSQLChange) changeSet.getChanges().get(0)).getSql());
		assertEquals(1, changeSet.getRollBackChanges().length);
		assertTrue(changeSet.getRollBackChanges()[0] instanceof RawSQLChange);
		assertEquals("drop table rawsql", ((RawSQLChange) changeSet.getRollBackChanges()[0]).getSql());
	}

	@Test
	public void addColumnAfter() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/columnAfterChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());
        
        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("1", changeSet.getId());
        
        // change 1
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("2", changeSet.getId());

        List<Change> changes = changeSet.getChanges();
        assertEquals(1, changes.size());

        Change change = changes.get(0);
        assertTrue(change instanceof AddColumnChange);

        List<AddColumnConfig> columns = ((AddColumnChange) change).getColumns();
        assertEquals(1, columns.size());
        
        AddColumnConfig columnConfig = columns.get(0);
        assertEquals("middlename", columnConfig.getName());
        
        assertEquals("firstname", columnConfig.getAfterColumn());
	}

	@Test
	public void addColumnBefore() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/columnBeforeChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());
        
        // change 0
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        assertEquals("1", changeSet.getId());
        
        // change 1
        changeSet = changeLog.getChangeSets().get(1);
        assertEquals("2", changeSet.getId());

        List<Change> changes = changeSet.getChanges();
        assertEquals(1, changes.size());

        Change change = changes.get(0);
        assertTrue(change instanceof AddColumnChange);

        List<AddColumnConfig> columns = ((AddColumnChange) change).getColumns();
        assertEquals(1, columns.size());
        
        AddColumnConfig columnConfig = columns.get(0);
        assertEquals("middlename", columnConfig.getName());
        
        assertEquals("lastname", columnConfig.getBeforeColumn());
	}

	@Test
	public void addColumnFirst() throws Exception {
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/columnFirstChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());
        
        ChangeSet changeSet = changeLog.getChangeSets().get(1);
        List<Change> changes = changeSet.getChanges();
        Change change = changes.get(0);
        List<AddColumnConfig> columns = ((AddColumnChange) change).getColumns();
        
        AddColumnConfig columnConfig = columns.get(0);
        assertEquals("middlename", columnConfig.getName());

		assertEquals(Integer.valueOf(1), columnConfig.getPosition());
	}
}
