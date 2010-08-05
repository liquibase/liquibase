package liquibase.parser.core.formattedsql;

import liquibase.change.Change;
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
            "--rollback changeSet\n"+
            "delete from table1;\n"+
            "drop table table1;\n"+
            "\n" +
            "--ChangeSet nvoxland:3\n" +
            "create table table2 (\n" +
            "  id int primary key\n" +
            ");\n" +
            "create table table3 (\n" +
            "  id int primary key\n" +
            ");\n"+
            "--rollback changeSet\n"+
            "drop table table2;\n";

    private static final String INVALID_CHANGELOG = "select * from table1";

    @Test
    public void supports() throws Exception {
        assertTrue(new MockFormattedSqlChangeLogParser(VALID_CHANGELOG).supports("asdf.sql", new JUnitResourceAccessor()));
        assertFalse(new MockFormattedSqlChangeLogParser(INVALID_CHANGELOG).supports("asdf.sql", new JUnitResourceAccessor()));
    }

    @Test
    public void parse() throws Exception {
        DatabaseChangeLog changeLog = new MockFormattedSqlChangeLogParser(VALID_CHANGELOG).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor());

        assertEquals("asdf.sql", changeLog.getLogicalFilePath());

        assertEquals(3, changeLog.getChangeSets().size());

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
