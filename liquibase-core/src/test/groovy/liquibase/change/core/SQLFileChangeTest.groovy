package liquibase.change.core

import liquibase.change.Change
import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.database.core.MockDatabase
import liquibase.parser.core.xml.XMLChangeLogSAXParser
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.statement.SqlStatement
import liquibase.test.JUnitResourceAccessor
import spock.lang.Unroll

import static org.junit.Assert.assertEquals

public class SQLFileChangeTest extends StandardChangeTest {

    def "generateStatements throws Exception if file does not exist"() throws Exception {
        when:
        def change = new SQLFileChange();
        change.setPath("doesnotexist.sql");
        change.finishInitialization();

        change.generateStatements(new MockDatabase())

        then:
         thrown(UnexpectedLiquibaseException.class)
    }

    @Unroll
    def "lines from file parse into one or more statements correctly"() throws Exception {
        when:
        SQLFileChange change2 = new SQLFileChange();
        change2.setSql(fileContents);
        MockDatabase database = new MockDatabase();
        SqlStatement[] statements = change2.generateStatements(database);

        then:
        statements.length == expectedStatements.size();
        for (int i = 0; i < expectedStatements.size(); i++) {
            assert expectedStatements[i] == statements[i].sql
        }

        where:
        fileContents | expectedStatements
        "SELECT * FROM customer;"                                                | ["SELECT * FROM customer"]
        "SELECT * FROM customer;\nSELECT * from table;\nSELECT * from table2;\n" | ["SELECT * FROM customer", "SELECT * from table", "SELECT * from table2"]
        "SELECT * FROM customer\ngo"                                             | ["SELECT * FROM customer"]
        "goSELECT * FROM customer\ngo" | ["goSELECT * FROM customer"]
        "SELECT * FROM customer\ngo\nSELECT * FROM table\ngo" | ["SELECT * FROM customer", "SELECT * FROM table"]
        "SELECT * FROM go\ngo\nSELECT * from gogo\ngo\n" | ["SELECT * FROM go", "SELECT * from gogo"]
        "insert into table ( col ) values (' value with; semicolon ');" | ["insert into table ( col ) values (' value with; semicolon ')"]
        "--\n-- This is a comment\nUPDATE tablename SET column = 1;\nGO" | ["--\n-- This is a comment\nUPDATE tablename SET column = 1"]
    }


    def getConfirmationMessage() throws Exception {
        when:
        def change = new SQLFileChange();
        change.setPath("com/example/changelog.xml");

        then:
        "SQL in file com/example/changelog.xml executed" == change.getConfirmationMessage()
    }

    def replacementOfProperties() throws Exception {
        when:
        SQLFileChange change = new SQLFileChange();
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        changeLogParameters.set("table.prefix", "prfx");
        changeLogParameters.set("some.other.prop", "nofx");
        ChangeSet changeSet = new ChangeSet("x", "y", true, true, null, null, null, null);
        changeSet.setChangeLogParameters(changeLogParameters);
        change.setChangeSet(changeSet);

        String fakeSql = "create \${table.prefix}_customer (\${some.other.prop} INTEGER NOT NULL, PRIMARY KEY (\${some.other.prop}));";

        change.setSql(fakeSql);

        then:
        assertEquals("create prfx_customer (nofx INTEGER NOT NULL, PRIMARY KEY (nofx));", change.getSql());
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def change = new RawSQLChange()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check raw sql status"
    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false;
    }

    def isValidForLoad(Change change) {
        return ((SQLFileChange) change).path != null;
    }

    def "openSqlStream throws exception if file does not exist"() {
        when:
        def change = new SQLFileChange()
        change.path = "non-existing.sql"
        change.openSqlStream()

        then:
        def e = thrown(IOException)
        e.message.startsWith("The file non-existing.sql was not found")

    }

    def "relativeToChangelogFile works if change set has a logical file path"() throws Exception {
        when:
        DatabaseChangeLog changeLog = new DatabaseChangeLog("liquibase/empty.changelog.xml");
        ChangeSet changeSet = new ChangeSet(null, null, true, false,
                "logical-path/empty.changelog.xml",
                null, null, false, null, changeLog);

        SQLFileChange relativeChange = new SQLFileChange();

        relativeChange.setRelativeToChangelogFile(Boolean.TRUE);
        relativeChange.setChangeSet(changeSet);
        relativeChange.setPath("change/core/SQLFileChange.sql");

        SqlStatement[] relativeStatements = relativeChange.generateStatements(new MockDatabase());

        SQLFileChange nonRelativeChange = new SQLFileChange();
        nonRelativeChange.setChangeSet(changeSet);
        nonRelativeChange.setPath("liquibase/change/core/SQLFileChange.sql");

        SqlStatement[] nonRelativeStatements = nonRelativeChange.generateStatements(new MockDatabase());

        then:
        assert relativeStatements != null
        assert nonRelativeStatements != null
        assert relativeStatements.size() == nonRelativeStatements.size()
    }
}
