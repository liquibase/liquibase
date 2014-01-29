package liquibase.parser.core.formattedsql;

import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.ChangeLogParseException;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.resource.ResourceAccessor;
import liquibase.test.JUnitResourceAccessor;
import liquibase.util.StringUtils;
import org.junit.Before;
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
            "-- changeset mysql:1\n" +
            "create table mysql_boo (\n" +
            "  id int primary key\n" +
            ");\n" +
            "-- rollback drop table mysql_boo;\n" +
            "-- changeset multicontext:1 context:first,second,third\n" +
            "select 1;\n"+
            "--changeset bboisvert:with_preconditions\n" +
            "--preconditions onFail:MARK_RAN onerror:HALT onUpdateSql:FAIL\n" +
            // these aren't very clever
            "--precondition-sql-check expectedResult:\"0 table(s)\" select count(*) || ' table(s)' from information_schema.tables where table_name = 'my_table'\n" +
            "--precondition-sql-check expectedresult:0 select count(*) from information_schema.columns where table_name = 'my_table' and column_name = 'id'\n" +
            "create table my_table (\n" +
            "  id int primary key\n" +
            ");\n" +
            "-- rollback drop table my_table;\n"
            ;

    private static final String INVALID_CHANGELOG = "select * from table1";
    private static final String INVALID_CHANGELOG_INVALID_PRECONDITION = "--liquibase formatted sql\n" +
        "\n" +
        "--changeset bboisvert:invalid_precondition\n" +
        "--precondition-invalid-type 123\n" +
        "select 1;"
        ;

    @Before
    public void before() {
        LiquibaseConfiguration.getInstance().reset();
    }

    @Test
    public void supports() throws Exception {
        assertTrue(new MockFormattedSqlChangeLogParser(VALID_CHANGELOG).supports("asdf.sql", new JUnitResourceAccessor()));
        assertFalse(new MockFormattedSqlChangeLogParser(INVALID_CHANGELOG).supports("asdf.sql", new JUnitResourceAccessor()));
    }

    @Test(expected = ChangeLogParseException.class)
    public void invalidPrecondition() throws Exception{
        new MockFormattedSqlChangeLogParser(INVALID_CHANGELOG_INVALID_PRECONDITION).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor());
    }

    @Test
    public void parse() throws Exception {
        ChangeLogParameters params = new ChangeLogParameters();
        params.set("tablename", "table4");
        DatabaseChangeLog changeLog = new MockFormattedSqlChangeLogParser(VALID_CHANGELOG).parse("asdf.sql", params, new JUnitResourceAccessor());

        assertEquals("asdf.sql", changeLog.getLogicalFilePath());

        assertEquals(9, changeLog.getChangeSets().size());

        assertEquals("nvoxland", changeLog.getChangeSets().get(0).getAuthor());
        assertEquals("1", changeLog.getChangeSets().get(0).getId());
        assertEquals(1, changeLog.getChangeSets().get(0).getChanges().size());
        assertEquals("select * from table1;", ((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).getSql());
        assertNull(((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).getEndDelimiter());
        assertTrue(((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).isSplitStatements());
        assertTrue(((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).isStripComments());
        assertFalse(changeLog.getChangeSets().get(0).isAlwaysRun());
        assertFalse(changeLog.getChangeSets().get(0).isRunOnChange());
        assertTrue(changeLog.getChangeSets().get(0).isRunInTransaction());
        assertEquals(0, changeLog.getChangeSets().get(0).getContexts().size());
        assertNull(changeLog.getChangeSets().get(0).getDbmsSet());


        assertEquals("nvoxland", changeLog.getChangeSets().get(1).getAuthor());
        assertEquals("2", changeLog.getChangeSets().get(1).getId());
        assertEquals(1, changeLog.getChangeSets().get(1).getChanges().size());
        assertEquals("create table table1 (\n" +
                "  id int primary key\n" +
                ");", ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getSql());
        assertEquals("X", ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getEndDelimiter());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isSplitStatements());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isStripComments());
        assertEquals("X", ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getEndDelimiter());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isSplitStatements());
        assertFalse(((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isStripComments());
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
        assertTrue(((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).isSplitStatements());
        assertTrue(((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).isStripComments());
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
        assertTrue(changeLog.getChangeSets().get(6).getChanges().get(0) instanceof RawSQLChange);
        assertEquals("create table mysql_boo (\n" +
                "  id int primary key\n" +
                ");", ((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).getSql());
        assertEquals(1, changeLog.getChangeSets().get(6).getRollBackChanges().length);
        assertTrue(changeLog.getChangeSets().get(6).getRollBackChanges()[0] instanceof RawSQLChange);
        assertEquals("drop table mysql_boo;", ((RawSQLChange) changeLog.getChangeSets().get(6).getRollBackChanges()[0]).getSql());

        assertEquals("multicontext", changeLog.getChangeSets().get(7).getAuthor());
        assertEquals("1", changeLog.getChangeSets().get(7).getId());
        assertEquals(1, changeLog.getChangeSets().get(7).getChanges().size());
        assertTrue(changeLog.getChangeSets().get(7).getChanges().get(0) instanceof RawSQLChange);
        assertEquals("select 1;", ((RawSQLChange) changeLog.getChangeSets().get(7).getChanges().get(0)).getSql());
        assertEquals(0, changeLog.getChangeSets().get(7).getRollBackChanges().length);
        assertEquals(3, changeLog.getChangeSets().get(7).getContexts().size());
        assertTrue(changeLog.getChangeSets().get(7).getContexts().contains("first"));
        assertTrue(changeLog.getChangeSets().get(7).getContexts().contains("second"));
        assertTrue(changeLog.getChangeSets().get(7).getContexts().contains("third"));
        
        


        ChangeSet cs = changeLog.getChangeSets().get(8);
        assertEquals("bboisvert", cs.getAuthor());
        assertEquals("with_preconditions", cs.getId());
        PreconditionContainer pc = cs.getPreconditions();
        assertNotNull(pc);
        assertEquals(PreconditionContainer.FailOption.MARK_RAN, pc.getOnFail());
        assertEquals(PreconditionContainer.ErrorOption.HALT, pc.getOnError());
        assertEquals(PreconditionContainer.OnSqlOutputOption.FAIL, pc.getOnSqlOutput());
        assertEquals(2, pc.getNestedPreconditions().size());
        assertTrue(pc.getNestedPreconditions().get(0) instanceof SqlPrecondition);
        SqlPrecondition p0 = (SqlPrecondition) pc.getNestedPreconditions().get(0);
        assertEquals("0 table(s)", p0.getExpectedResult());
        assertEquals("select count(*) || ' table(s)' from information_schema.tables where table_name = 'my_table'", p0.getSql());
        assertTrue(pc.getNestedPreconditions().get(1) instanceof SqlPrecondition);
        SqlPrecondition p1 = (SqlPrecondition) pc.getNestedPreconditions().get(1);
        assertEquals("0", p1.getExpectedResult());
        assertEquals("select count(*) from information_schema.columns where table_name = 'my_table' and column_name = 'id'", p1.getSql());
        assertEquals(1, cs.getChanges().size());
        assertTrue(cs.getChanges().get(0) instanceof RawSQLChange);
        assertEquals("create table my_table (\n" +
            "  id int primary key\n" +
            ");", ((RawSQLChange) cs.getChanges().get(0)).getSql());
        assertEquals(1, cs.getRollBackChanges().length);
        assertTrue(cs.getRollBackChanges()[0] instanceof RawSQLChange);
        assertEquals("drop table my_table;", ((RawSQLChange) cs.getRollBackChanges()[0]).getSql());
    }

    @Test
    public void parse_authorWithSpace() throws Exception {
        String changeLogWithSpace = "--liquibase formatted sql\n\n"+
                "--changeset John Doe:12345\n" +
                "create table test (id int);\n";

        DatabaseChangeLog changeLog = new MockFormattedSqlChangeLogParser(changeLogWithSpace).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor());
        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("John Doe", changeLog.getChangeSets().get(0).getAuthor());
        assertEquals("12345", changeLog.getChangeSets().get(0).getId());

    }

    @Test
    public void parse_multipleDbms() throws Exception {
        // Happy day scenarios
    	String changeLogWithMultipleDbms = "--liquibase formatted sql\n\n"+
                "--changeset John Doe:12345 dbms:db2,db2i\n" +
                "create table test (id int);\n";

        DatabaseChangeLog changeLog = new MockFormattedSqlChangeLogParser(changeLogWithMultipleDbms).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor());
        assertEquals(2, changeLog.getChangeSets().get(0).getDbmsSet().size());
        assertTrue(changeLog.getChangeSets().get(0).getDbmsSet().contains("db2"));
        assertTrue(changeLog.getChangeSets().get(0).getDbmsSet().contains("db2i"));
        
        // Sad night scenarios
        String changeLogWithInvalidMultipleDbms = "--liquibase formatted sql\n\n"+
                "--changeset John Doe:12345 dbms:db2, db2i\n" +
                "create table test (id int);\n";

        DatabaseChangeLog invalidChangeLog = new MockFormattedSqlChangeLogParser(changeLogWithInvalidMultipleDbms).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor());
        assertEquals(1, invalidChangeLog.getChangeSets().get(0).getDbmsSet().size());
        assertTrue(invalidChangeLog.getChangeSets().get(0).getDbmsSet().contains("db2"));
        assertFalse(invalidChangeLog.getChangeSets().get(0).getDbmsSet().contains("db2i"));

        changeLogWithInvalidMultipleDbms = "--liquibase formatted sql\n\n"+
                "--changeset John Doe:12345 dbms:db2,\n" +
                "create table test (id int);\n";

        invalidChangeLog = new MockFormattedSqlChangeLogParser(changeLogWithInvalidMultipleDbms).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor());
        assertEquals(1, invalidChangeLog.getChangeSets().get(0).getDbmsSet().size());
        assertTrue(invalidChangeLog.getChangeSets().get(0).getDbmsSet().contains("db2"));
        assertFalse(invalidChangeLog.getChangeSets().get(0).getDbmsSet().contains("db2i"));
        
        changeLogWithInvalidMultipleDbms = "--liquibase formatted sql\n\n"+
                "--changeset John Doe:12345 dbms:,db2,\n" +
                "create table test (id int);\n";

        invalidChangeLog = new MockFormattedSqlChangeLogParser(changeLogWithInvalidMultipleDbms).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor());
        assertEquals(null, invalidChangeLog.getChangeSets().get(0).getDbmsSet());
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
