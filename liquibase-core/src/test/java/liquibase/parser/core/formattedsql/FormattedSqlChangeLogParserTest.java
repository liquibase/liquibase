package liquibase.parser.core.formattedsql;

import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.resource.ResourceAccessor;
import liquibase.test.JUnitResourceAccessor;
import liquibase.util.StringUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class FormattedSqlChangeLogParserTest {
    private static final String VALID_CHANGELOG = "--liquibase formatted sql\n" +
            "\n" +
            "--changeset nvoxland:1\n" +
            "select * from table1;\n" +
            "\n" +
            "--changeset nvoxland:2 (stripComments:false splitStatements:false endDelimiter:X runOnChange:true runAlways:true context:y dbms:mysql runInTransaction:false failOnError:false)\n" +
            "create table table1 (\n" +
            "  id int primary key\n" +
            ");\n" +
            "\n" +
            "--rollback delete from table1;\n"+
            "--rollback drop table table1;\n"+
            "\n" +
            "--ChangeSet nvoxland:3\n" +
            "create table table2 (\n" +
            "  id int primary key\n" +
            ");\n" +
            "create table table3 (\n" +
            "  id int primary key\n" +
            ");\n"+
            "--rollback drop table table2;\n"+
            "--ChangeSet alwyn:4\n" +
            "select (*) from table2;\n" +
            "--rollback not required\n" +
            "--ChangeSet nvoxland:5\n" +
            "select (*) from table2;\n" +
            "--rollback not required\n" +
            "--ChangeSet paikens:6\n" +
            "create table ${tablename} (\n" +
            "  id int primary key\n" +
            ");\n" +
            "--rollback drop table ${tablename};\n" +
            "-- changeset mysql:1 (stripComments:false splitStatements:false endDelimiter:X runOnChange:true runAlways:true context:y dbms:mysql runInTransaction:false failOnError:false)\n" +
            "create table mysql (\n" +
            "  id int primary key\n" +
            ");\n" +
            "\n" +
            "-- rollback delete from mysql;\n"+
            "-- rollback drop table mysql;\n"+
            "\n" +
            ;

    private static final String INVALID_CHANGELOG = "select * from table1";

    @Test
    public void supports() throws Exception {
        assertTrue(new MockFormattedSqlChangeLogParser(VALID_CHANGELOG).supports("asdf.sql", new JUnitResourceAccessor()));
        assertFalse(new MockFormattedSqlChangeLogParser(INVALID_CHANGELOG).supports("asdf.sql", new JUnitResourceAccessor()));
    }

    @Test
    public void parse() throws Exception {
        ChangeLogParameters params = new ChangeLogParameters();
		params.set("tablename", "table4");
		DatabaseChangeLog changeLog = new MockFormattedSqlChangeLogParser(VALID_CHANGELOG).parse("asdf.sql", params, new JUnitResourceAccessor());

        assertEquals("asdf.sql", changeLog.getLogicalFilePath());

        assertEquals(6, changeLog.getChangeSets().size());

        assertEquals("nvoxland", changeLog.getChangeSets().get(0).getAuthor());
        assertEquals("1", changeLog.getChangeSets().get(0).getId());
        assertEquals(1, changeLog.getChangeSets().get(0).getChanges().size());
        assertEquals("select * from table1;", ((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).getSql());
        assertNull(((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).getEndDelimiter());
        assertTrue(((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).isSplittingStatements());
        assertTrue(((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).isStrippingComments());
        assertFalse(changeLog.getChangeSets().get(0).isAlwaysRun());
        assertFalse(changeLog.getChangeSets().get(0).isRunOnChange());
        assertTrue(changeLog.getChangeSets().get(0).isRunInTransaction());
        assertNull(changeLog.getChangeSets().get(0).getContexts());
        assertNull(changeLog.getChangeSets().get(0).getDbmsSet());


        assertEquals("nvoxland", changeLog.getChangeSets().get(1).getAuthor());
        assertEquals("2", changeLog.getChangeSets().get(1).getId());
        assertEquals(1, changeLog.getChangeSets().get(1).getChanges().size());
        assertEquals("create table table1 (\n" +
                "  id int primary key\n" +
                ");", ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getSql());
        assertEquals("X", ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getEndDelimiter());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isSplittingStatements());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isStrippingComments());
        assertEquals("X", ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getEndDelimiter());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isSplittingStatements());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isStrippingComments());
        assertTrue(changeLog.getChangeSets().get(1).isAlwaysRun());
        assertTrue(changeLog.getChangeSets().get(1).isRunOnChange());
        assertFalse(changeLog.getChangeSets().get(1).isRunInTransaction());
        assertEquals("y", StringUtils.join(changeLog.getChangeSets().get(1).getContexts(), ","));
        assertEquals("mysql", StringUtils.join(changeLog.getChangeSets().get(1).getDbmsSet(), ","));
        assertEquals(1, changeLog.getChangeSets().get(1).getRollBackChanges().length);
        assertEquals("delete from table1;\n" +
                "drop table table1;", ((RawSQLChange) changeLog.getChangeSets().get(1).getRollBackChanges()[0]).getSql());


        assertEquals("nvoxland", changeLog.getChangeSets().get(2).getAuthor());
        assertEquals("3", changeLog.getChangeSets().get(2).getId());
        assertEquals(1, changeLog.getChangeSets().get(2).getChanges().size());
        assertEquals("create table table2 (\n" +
                "  id int primary key\n" +
                ");\n" +
                "create table table3 (\n" +
                "  id int primary key\n" +
                ");", ((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).getSql());
        assertNull(((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).getEndDelimiter());
        assertTrue(((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).isSplittingStatements());
        assertTrue(((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).isStrippingComments());
        assertEquals(1, changeLog.getChangeSets().get(2).getRollBackChanges().length);
        assertTrue(changeLog.getChangeSets().get(2).getRollBackChanges()[0] instanceof RawSQLChange);
        assertEquals("drop table table2;", ((RawSQLChange) changeLog.getChangeSets().get(2).getRollBackChanges()[0]).getSql());

        assertEquals("alwyn", changeLog.getChangeSets().get(3).getAuthor());
        assertEquals("4", changeLog.getChangeSets().get(3).getId());
        assertEquals(1, changeLog.getChangeSets().get(3).getRollBackChanges().length);
        assertTrue(changeLog.getChangeSets().get(3).getRollBackChanges()[0] instanceof EmptyChange);

        assertEquals("nvoxland", changeLog.getChangeSets().get(4).getAuthor());
        assertEquals("5", changeLog.getChangeSets().get(4).getId());
        assertEquals(1, changeLog.getChangeSets().get(4).getRollBackChanges().length);
		assertTrue(changeLog.getChangeSets().get(4).getRollBackChanges()[0] instanceof EmptyChange);

		assertEquals("paikens", changeLog.getChangeSets().get(5).getAuthor());
        assertEquals("6", changeLog.getChangeSets().get(5).getId());
		assertEquals(1, changeLog.getChangeSets().get(5).getChanges().size());
        assertTrue(changeLog.getChangeSets().get(5).getChanges().get(0) instanceof RawSQLChange);
        assertEquals("create table table4 (\n" +
                "  id int primary key\n" +
                ");", ((RawSQLChange) changeLog.getChangeSets().get(5).getChanges().get(0)).getSql());
        assertEquals(1, changeLog.getChangeSets().get(5).getRollBackChanges().length);
        assertTrue(changeLog.getChangeSets().get(5).getRollBackChanges()[0] instanceof RawSQLChange);
        assertEquals("drop table table4;", ((RawSQLChange) changeLog.getChangeSets().get(5).getRollBackChanges()[0]).getSql());

        assertEquals("mysql", changeLog.getChangeSets().get(6).getAuthor());
        assertEquals("1", changeLog.getChangeSets().get(6).getId());
        assertEquals(1, changeLog.getChangeSets().get(6).getChanges().size());
        assertEquals("create table mysql (\n" +
                "  id int primary key\n" +
                ");", ((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).getSql());
        assertEquals("X", ((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).getEndDelimiter());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).isSplittingStatements());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).isStrippingComments());
        assertEquals("X", ((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).getEndDelimiter());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).isSplittingStatements());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).isStrippingComments());
        assertTrue(changeLog.getChangeSets().get(6).isAlwaysRun());
        assertTrue(changeLog.getChangeSets().get(6).isRunOnChange());
        assertFalse(changeLog.getChangeSets().get(6).isRunInTransaction());
        assertEquals("y", StringUtils.join(changeLog.getChangeSets().get(6).getContexts(), ","));
        assertEquals("mysql", StringUtils.join(changeLog.getChangeSets().get(6).getDbmsSet(), ","));
        assertEquals(1, changeLog.getChangeSets().get(6).getRollBackChanges().length);
        assertEquals("delete from mysql;\n" +
                "drop table mysql;", ((RawSQLChange) changeLog.getChangeSets().get(6).getRollBackChanges()[0]).getSql());
    }

    private static class MockFormattedSqlChangeLogParser extends FormattedSqlChangeLogParser {
        private String changeLog;

        public MockFormattedSqlChangeLogParser(String changeLog) {
            this.changeLog = changeLog;
        }

        @Override
        protected InputStream openChangeLogFile(String physicalChangeLogLocation, ResourceAccessor resourceAccessor) throws IOException {
            return new ByteArrayInputStream(changeLog.getBytes());
        }
    }
}
