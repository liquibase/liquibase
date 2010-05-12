package liquibase.parser.core.formattedsql;

import liquibase.change.Change;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.resource.ResourceAccessor;
import liquibase.test.JUnitResourceAccessor;
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
            "--changeset nvoxland:2\n" +
            "create table table1 (\n" +
            "  id int primary key\n" +
            ");\n" +
            "\n" +
            "--changeset nvoxland:3\n" +
            "create table table2 (\n" +
            "  id int primary key\n" +
            ");\n" +
            "create table table3 (\n" +
            "  id int primary key\n" +
            ");\n";

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

        assertEquals("nvoxland", changeLog.getChangeSets().get(1).getAuthor());
        assertEquals("2", changeLog.getChangeSets().get(1).getId());
        assertEquals(1, changeLog.getChangeSets().get(1).getChanges().size());
        assertEquals("create table table1 (\n" +
                "  id int primary key\n" +
                ");", ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getSql());

        assertEquals("nvoxland", changeLog.getChangeSets().get(2).getAuthor());
        assertEquals("3", changeLog.getChangeSets().get(2).getId());
        assertEquals(1, changeLog.getChangeSets().get(2).getChanges().size());
        assertEquals("create table table2 (\n" +
                "  id int primary key\n" +
                ");\n" +
                "create table table3 (\n" +
                "  id int primary key\n" +
                ");", ((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).getSql());
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
