package liquibase.change.core

import liquibase.ChecksumVersion
import liquibase.Scope
import liquibase.change.Change
import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.core.MockDatabase
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration
import liquibase.statement.SqlStatement
import liquibase.test.JUnitResourceAccessor
import liquibase.util.StreamUtil
import spock.lang.Unroll

import static org.junit.Assert.assertEquals

class SQLFileChangeTest extends StandardChangeTest {

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
        fileContents                                                             | expectedStatements
        "SELECT * FROM customer;"                                                | ["SELECT * FROM customer"]
        "SELECT * FROM customer;\nSELECT * from table;\nSELECT * from table2;\n" | ["SELECT * FROM customer", "SELECT * from table", "SELECT * from table2"]
        "SELECT * FROM customer\ngo"                                             | ["SELECT * FROM customer"]
        "goSELECT * FROM customer\ngo"                                           | ["goSELECT * FROM customer"]
        "SELECT * FROM customer\ngo\nSELECT * FROM table\ngo"                    | ["SELECT * FROM customer", "SELECT * FROM table"]
        "SELECT * FROM go\ngo\nSELECT * from gogo\ngo\n"                         | ["SELECT * FROM go", "SELECT * from gogo"]
        "insert into table ( col ) values (' value with; semicolon ');"          | ["insert into table ( col ) values (' value with; semicolon ')"]
        "--\n-- This is a comment\nUPDATE tablename SET column = 1;\nGO"         | ["--\n-- This is a comment\nUPDATE tablename SET column = 1"]
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

    @Unroll
    def "openSqlStream correctly opens files"() {
        when:
        def changelog = new DatabaseChangeLog("com/example/changelog.xml")

        def changeset = new ChangeSet("1", "auth", false, false, logicalFilePath, null, null, changelog)

        def change = new SQLFileChange()
        change.path = changePath
        change.relativeToChangelogFile = relativeToChangelogFile
        change.setChangeSet(changeset)

        String fileContents = Scope.child([(Scope.Attr.resourceAccessor.name()): new JUnitResourceAccessor()], {
            return StreamUtil.readStreamAsString(change.openSqlStream())
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        fileContents.trim() == "My Logic Here"

        where:
        changePath                 | logicalFilePath      | relativeToChangelogFile
        "com/example/my-logic.sql" | null                 | false
        "com/example/my-logic.sql" | "a/logical/path.xml" | false
        "my-logic.sql"             | null                 | true
        "my-logic.sql"             | "a/logical/path.xml" | true

    }

    @Unroll
    def "validate checksum if sql(file) content change - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        String procedureText =
        """CREATE OR REPLACE PROCEDURE testHello()
                  LANGUAGE plpgsql
                  AS \$\$
        BEGIN
                  raise notice 'valueToReplace';
        END \$\$"""
        def change = new SQLFileChange()

        procedureText = procedureText.replace("valueToReplace", "value1")
        change.setSql(procedureText)

        def checkSumFirstReplacement = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn).toString()

        procedureText = procedureText.replace("value1", "value2")
        change.setSql(procedureText)

        def checkSumSecondReplacement = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn).toString()

        then:
        checkSumFirstReplacement == originalChecksum
        checkSumSecondReplacement == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:25560f4c442fa581b820d0a6206fd14e" | "8:b934d68e53222bc7b5cbf147ce6746b4"
        ChecksumVersion.latest() | "9:8cfbd3e5970885470db17cd149feb637" | "9:f6302129ace10ca356faa21343dd1aa8"
    }
}
