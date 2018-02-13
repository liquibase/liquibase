package liquibase.parser.core.formattedsql

import liquibase.change.core.EmptyChange
import liquibase.change.core.RawSQLChange
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.configuration.LiquibaseConfiguration
import liquibase.exception.ChangeLogParseException
import liquibase.precondition.core.PreconditionContainer
import liquibase.precondition.core.SqlPrecondition
import liquibase.resource.ResourceAccessor
import liquibase.test.JUnitResourceAccessor
import liquibase.util.StringUtil
import org.hamcrest.Matchers
import spock.lang.Specification
import spock.lang.Unroll

import static spock.util.matcher.HamcrestSupport.that

public class FormattedSqlChangeLogParserTest extends Specification {

    private static final String VALID_CHANGELOG = """
--liquibase formatted sql

--changeset nvoxland:1
select * from table1;

--changeset nvoxland:2 (stripComments:false splitStatements:false endDelimiter:X runOnChange:true runAlways:true context:y dbms:mysql runInTransaction:false failOnError:false)
create table table1 (
    id int primary key
);

--rollback delete from table1;
--rollback drop table table1;

--ChangeSet nvoxland:3
create table table2 (
    id int primary key
);
create table table3 (
    id int primary key
);
--rollback drop table table2;

--ChangeSet alwyn:4
select (*) from table2;
--rollback not required

--ChangeSet nvoxland:5
select (*) from table2;
--rollback not required

--ChangeSet paikens:6
create table \${tablename} (
    id int primary key
);
--rollback drop table \${tablename};

-- changeset mysql:1
-- comment: this is a comment
create table mysql_boo (
    id int primary key
);
-- rollback drop table mysql_boo;

-- changeset multicontext:1 context:first,second,third
select 1;

--changeset bboisvert:with_preconditions
--preconditions onFail:MARK_RAN onerror:HALT onUpdateSql:FAIL
--precondition-sql-check expectedResult:"0 table(s)" select count(*) || ' table(s)' from information_schema.tables where table_name = 'my_table'
--precondition-sql-check expectedresult:0 select count(*) from information_schema.columns where table_name = 'my_table' and column_name = 'id'
create table my_table (
    id int primary key
);
-- rollback drop table my_table;

--changeset complexContext:1 context:"a or b"
select 1
""".trim()


    private static final String INVALID_CHANGELOG = "select * from table1"
    private static final String INVALID_CHANGELOG_INVALID_PRECONDITION = "--liquibase formatted sql\n" +
            "\n" +
            "--changeset bboisvert:invalid_precondition\n" +
            "--precondition-invalid-type 123\n" +
            "select 1;"


    def setup() {
        LiquibaseConfiguration.getInstance().reset()
    }

    def cleanup() {
        LiquibaseConfiguration.getInstance().reset()
    }

    def supports() throws Exception {
        expect:
        assert new MockFormattedSqlChangeLogParser(VALID_CHANGELOG).supports("asdf.sql", new JUnitResourceAccessor())
        assert !new MockFormattedSqlChangeLogParser(INVALID_CHANGELOG).supports("asdf.sql", new JUnitResourceAccessor())
    }

    def invalidPrecondition() throws Exception {
        when:
        new MockFormattedSqlChangeLogParser(INVALID_CHANGELOG_INVALID_PRECONDITION).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor())
        then:
        thrown(ChangeLogParseException)
    }

    def parse() throws Exception {
        expect:
        ChangeLogParameters params = new ChangeLogParameters()
        params.set("tablename", "table4")
        DatabaseChangeLog changeLog = new MockFormattedSqlChangeLogParser(VALID_CHANGELOG).parse("asdf.sql", params, new JUnitResourceAccessor())

        changeLog.getLogicalFilePath() == "asdf.sql"

        changeLog.getChangeSets().size() == 10

        changeLog.getChangeSets().get(0).getAuthor() == "nvoxland"
        changeLog.getChangeSets().get(0).getId() == "1"
        changeLog.getChangeSets().get(0).getChanges().size() == 1
        ((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).getSql() == "select * from table1;"
        ((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).getEndDelimiter() == null
        assert ((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).isSplitStatements()
        assert ((RawSQLChange) changeLog.getChangeSets().get(0).getChanges().get(0)).isStripComments()
        assert !changeLog.getChangeSets().get(0).isAlwaysRun()
        assert !changeLog.getChangeSets().get(0).isRunOnChange()
        assert changeLog.getChangeSets().get(0).isRunInTransaction()
        assert changeLog.getChangeSets().get(0).getContexts().isEmpty()
        changeLog.getChangeSets().get(0).getDbmsSet() == null


        changeLog.getChangeSets().get(1).getAuthor() == "nvoxland"
        changeLog.getChangeSets().get(1).getId() == "2"
        changeLog.getChangeSets().get(1).getChanges().size() == 1
        ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getSql().replace("\r\n", "\n") == "create table table1 (\n    id int primary key\n);"
        ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getEndDelimiter() == "X"
        assert !((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isSplitStatements()
        assert !((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isStripComments()
        ((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).getEndDelimiter() == "X"
        assert !((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isSplitStatements()
        assert !((RawSQLChange) changeLog.getChangeSets().get(1).getChanges().get(0)).isStripComments()
        assert changeLog.getChangeSets().get(1).isAlwaysRun()
        assert changeLog.getChangeSets().get(1).isRunOnChange()
        assert !changeLog.getChangeSets().get(1).isRunInTransaction()
        changeLog.getChangeSets().get(1).getContexts().toString() == "y"
        StringUtil.join(changeLog.getChangeSets().get(1).getDbmsSet(), ",") == "mysql"
        changeLog.getChangeSets().get(1).rollback.changes.size() == 1
        ((RawSQLChange) changeLog.getChangeSets().get(1).rollback.changes[0]).getSql().replace("\r\n", "\n") == "delete from table1;\ndrop table table1;"


        changeLog.getChangeSets().get(2).getAuthor() == "nvoxland"
        changeLog.getChangeSets().get(2).getId() == "3"
        changeLog.getChangeSets().get(2).getChanges().size() == 1
        ((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).getSql().replace("\r\n", "\n") == "create table table2 (\n    id int primary key\n);\ncreate table table3 (\n    id int primary key\n);"
        ((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).getEndDelimiter() == null
        assert ((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).isSplitStatements()
        assert ((RawSQLChange) changeLog.getChangeSets().get(2).getChanges().get(0)).isStripComments()
        changeLog.getChangeSets().get(2).rollback.changes.size() == 1
        assert changeLog.getChangeSets().get(2).rollback.changes[0] instanceof RawSQLChange
        ((RawSQLChange) changeLog.getChangeSets().get(2).rollback.changes[0]).getSql() == "drop table table2;"

        changeLog.getChangeSets().get(3).getAuthor() == "alwyn"
        changeLog.getChangeSets().get(3).getId() == "4"
        changeLog.getChangeSets().get(3).rollback.changes.size() == 1
        assert changeLog.getChangeSets().get(3).rollback.changes[0] instanceof EmptyChange

        changeLog.getChangeSets().get(4).getAuthor() == "nvoxland"
        changeLog.getChangeSets().get(4).getId() == "5"
        changeLog.getChangeSets().get(4).rollback.changes.size() == 1
        assert changeLog.getChangeSets().get(4).rollback.changes[0] instanceof EmptyChange

        changeLog.getChangeSets().get(5).getAuthor() == "paikens"
        changeLog.getChangeSets().get(5).getId() == "6"
        changeLog.getChangeSets().get(5).getChanges().size() == 1
        assert changeLog.getChangeSets().get(5).getChanges().get(0) instanceof RawSQLChange
        ((RawSQLChange) changeLog.getChangeSets().get(5).getChanges().get(0)).getSql().replace("\r\n", "\n") == "create table table4 (\n    id int primary key\n);"
        changeLog.getChangeSets().get(5).rollback.changes.size() == 1
        assert changeLog.getChangeSets().get(5).rollback.changes[0] instanceof RawSQLChange
        ((RawSQLChange) changeLog.getChangeSets().get(5).rollback.changes[0]).getSql() == "drop table table4;"


        changeLog.getChangeSets().get(6).getAuthor() == "mysql"
        changeLog.getChangeSets().get(6).getId() == "1"
        changeLog.getChangeSets().get(6).getChanges().size() == 1
        assert changeLog.getChangeSets().get(6).getChanges().get(0) instanceof RawSQLChange
        ((RawSQLChange) changeLog.getChangeSets().get(6).getChanges().get(0)).getSql().replace("\r\n", "\n") == "create table mysql_boo (\n    id int primary key\n);"
        changeLog.getChangeSets().get(6).rollback.changes.size() == 1
        assert changeLog.getChangeSets().get(6).rollback.changes[0] instanceof RawSQLChange
        ((RawSQLChange) changeLog.getChangeSets().get(6).rollback.changes[0]).getSql() == "drop table mysql_boo;"

        changeLog.getChangeSets().get(7).getAuthor() == "multicontext"
        changeLog.getChangeSets().get(7).getId() == "1"
        changeLog.getChangeSets().get(7).getChanges().size() == 1
        assert changeLog.getChangeSets().get(7).getChanges().get(0) instanceof RawSQLChange
        ((RawSQLChange) changeLog.getChangeSets().get(7).getChanges().get(0)).getSql() == "select 1;"
        changeLog.getChangeSets().get(7).rollback.changes.size() == 0
//         changeLog.getChangeSets().get(7).getContexts().size() == 3
        assert changeLog.getChangeSets().get(7).getContexts().toString().contains("first")
        assert changeLog.getChangeSets().get(7).getContexts().toString().contains("second")
        assert changeLog.getChangeSets().get(7).getContexts().toString().contains("third")


        ChangeSet cs = changeLog.getChangeSets().get(8)
        cs.getAuthor() == "bboisvert"
        cs.getId() == "with_preconditions"
        PreconditionContainer pc = cs.getPreconditions()
        pc != null
        pc.getOnFail() == PreconditionContainer.FailOption.MARK_RAN
        pc.getOnError() == PreconditionContainer.ErrorOption.HALT
        pc.getOnSqlOutput() == PreconditionContainer.OnSqlOutputOption.FAIL
        pc.getNestedPreconditions().size() == 2
        assert pc.getNestedPreconditions().get(0) instanceof SqlPrecondition
        SqlPrecondition p0 = (SqlPrecondition) pc.getNestedPreconditions().get(0)
        p0.getExpectedResult() == "0 table(s)"
        p0.getSql() == "select count(*) || ' table(s)' from information_schema.tables where table_name = 'my_table'"
        assert pc.getNestedPreconditions().get(1) instanceof SqlPrecondition
        SqlPrecondition p1 = (SqlPrecondition) pc.getNestedPreconditions().get(1)
        p1.getExpectedResult() == "0"
        p1.getSql() == "select count(*) from information_schema.columns where table_name = 'my_table' and column_name = 'id'"
        cs.getChanges().size() == 1
        assert cs.getChanges().get(0) instanceof RawSQLChange
        ((RawSQLChange) cs.getChanges().get(0)).getSql().replace("\r\n", "\n") == "create table my_table (\n    id int primary key\n);"
        cs.rollback.changes.size() == 1
        assert cs.rollback.changes[0] instanceof RawSQLChange
        ((RawSQLChange) cs.rollback.changes[0]).getSql() == "drop table my_table;"


        changeLog.getChangeSets().get(9).getContexts().toString() == "a or b"

    }

    def parse_authorWithSpace() throws Exception {
        when:
        String changeLogWithSpace = "--liquibase formatted sql\n\n" +
                "--changeset John Doe:12345\n" +
                "create table test (id int);\n"

        DatabaseChangeLog changeLog = new MockFormattedSqlChangeLogParser(changeLogWithSpace).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        changeLog.getChangeSets().size() == 1
        changeLog.getChangeSets().get(0).getAuthor() == "John Doe"
        changeLog.getChangeSets().get(0).getId() == "12345"

    }

    def parse_withComment() throws Exception {
        when:
        String changeLogWithComment = "--liquibase formatted sql\n\n" +
                "--changeset JohnDoe:12345\n" +
                "--comment: This is a test comment\n" +
                "create table test (id int);\n"

        DatabaseChangeLog changeLog = new MockFormattedSqlChangeLogParser(changeLogWithComment).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        changeLog.getChangeSets().size() == 1
        changeLog.getChangeSets().get(0).getAuthor() == "JohnDoe"
        changeLog.getChangeSets().get(0).getId() == "12345"
        changeLog.getChangeSets().get(0).getComments() == "This is a test comment"
    }

    @Unroll
    def parse_multipleDbms() throws Exception {
        when:
        def changeLog = new MockFormattedSqlChangeLogParser(changelog).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor())
        def dbmsSet = changeLog.getChangeSets().get(0).getDbmsSet()

        then:
        if (expected == null) {
            assert dbmsSet == null
            return
        }

        that dbmsSet, Matchers.containsInAnyOrder(expected.toArray())

        where:
        changelog                                                                                               | expected
        "--liquibase formatted sql\n\n--changeset John Doe:12345 dbms:db2,db2i\ncreate table test (id int);\n"  | ["db2", "db2i"]
        "--liquibase formatted sql\n\n--changeset John Doe:12345 dbms:db2, db2i\ncreate table test (id int);\n" | ["db2"]
        "--liquibase formatted sql\n\n--changeset John Doe:12345 dbms:db2,\ncreate table test (id int);\n"      | ["db2"]
        "--liquibase formatted sql\n\n--changeset John Doe:12345 dbms:,db2,\ncreate table test (id int);\n"     | null
    }

    @Unroll("#featureName: #example")
    def "example file"() {
        when:
        def changeLog = new MockFormattedSqlChangeLogParser(example).parse("asdf.sql", new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        ((RawSQLChange) changeLog.changeSets[0].changes[0]).sql.replace("\r\n", "\n") == expected

        where:
        example                                                                                                  | expected
        "--liquibase formatted sql\n--changeset John Doe:12345\nCREATE PROC TEST\nAnother Line\nEND MY PROC;\n/" | "CREATE PROC TEST\nAnother Line\nEND MY PROC;\n/"
    }

    private static class MockFormattedSqlChangeLogParser extends FormattedSqlChangeLogParser {
        private String changeLog

        public MockFormattedSqlChangeLogParser(String changeLog) {
            this.changeLog = changeLog
        }

        @Override
        protected InputStream openChangeLogFile(String physicalChangeLogLocation, ResourceAccessor resourceAccessor) throws IOException {
            return new ByteArrayInputStream(changeLog.getBytes())
        }
    }
}
