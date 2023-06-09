package liquibase.change.core

import liquibase.Scope
import liquibase.change.CheckSum
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.exception.ValidationErrors
import liquibase.parser.core.ParsedNode
import liquibase.database.core.MockDatabase
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.test.JUnitResourceAccessor
import liquibase.util.StreamUtil
import spock.lang.Unroll

class CreateProcedureChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        CreateProcedureChange refactoring = new CreateProcedureChange();

        then:
        "Stored procedure created" == refactoring.getConfirmationMessage()
    }

    public static final String PROCEDURE_TEXT = "SOME SQL";

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory
        def change = new CreateProcedureChange()
        change.procedureName = "test_proc"

        then:
        assert change.checkStatus(database).message == "Cannot check createProcedure status"
    }

    def "load with inline sql"() {
        when:
        def change = new CreateProcedureChange()
        change.load(new ParsedNode(null, "createProcedure").setValue("create procedure sql"), null, new MockResourceAccessor())
        change.validate(new OracleDatabase())

        then:
        change.serialize().toString() == "createProcedure[procedureText=create procedure sql]"
    }

    @Unroll
    def "load correct file"() {
        when:
        def changelog = new DatabaseChangeLog("com/example/changelog.xml")

        def changeset = new ChangeSet("1", "auth", false, false, logicalFilePath, null, null, changelog)

        def change = new CreateProcedureChange()
        change.path = sqlPath
        change.relativeToChangelogFile = relativeToChangelogFile
        change.setChangeSet(changeset)

        String fileContents = Scope.child([(Scope.Attr.resourceAccessor.name()): new JUnitResourceAccessor()], {
            return StreamUtil.readStreamAsString(change.openSqlStream())
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        fileContents.trim() == "My Logic Here"

        where:
        sqlPath                    | logicalFilePath      | relativeToChangelogFile
        "com/example/my-logic.sql" | null                 | false
        "com/example/my-logic.sql" | "a/logical/path.xml" | false
        "my-logic.sql"             | null                 | true
        "my-logic.sql"             | "a/logical/path.xml" | true

    }

    @Unroll
    def "validate CreateProcedure with dmbs attribute set"() {
        when:

        CreateProcedureChange createProcedure = new CreateProcedureChange();
        createProcedure.setDbms(dbms);
        ValidationErrors valErrors = createProcedure.validate(database);

        then:
        valErrors.getErrorMessages().get(0).contains(expectedValidationErrorMsg);

        where:
        database               | dbms                             | expectedValidationErrorMsg
        new PostgresDatabase() | "post"                           | String.format("%s is not a supported DB", dbms)
        new PostgresDatabase() | "postgresql"                     | ""
        new MockDatabase()     | "postgresql, h2, mssql, !sqlite" | ""
        new PostgresDatabase() | "none"                           | ""
        new PostgresDatabase() | "all"                            | ""
    }

    def "dbms is not considered on checksum generation"() {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureCheckSumWithoutDbms = change.generateCheckSum()
        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setProcedureText(PROCEDURE_TEXT)
        change2.setDbms("postgresql")
        CheckSum procedureCheckSumWithDbms = change2.generateCheckSum()

        then:
        procedureCheckSumWithoutDbms == procedureCheckSumWithDbms

    }

    def "path is not considered on checksum generation"() {
        when:
        String testScopeId = Scope.enter([
                "resourceAccessor": new MockResourceAccessor([
                        "test.sql": PROCEDURE_TEXT
                ])
        ])

        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureCheckSumWithoutPath = change.generateCheckSum()
        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setPath("test.sql")
        //Below check sum generation should not take path property into account
        CheckSum procedureCheckSumWithPath = change2.generateCheckSum()
        //TODO: Move this Scope.exit() call into a cleanUpSpec method
        Scope.exit(testScopeId)

        then:
        procedureCheckSumWithoutPath == procedureCheckSumWithPath
    }

    def "comment is not considered on checksum generation"() {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureCheckSumWithoutComments = change.generateCheckSum()
        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setProcedureText(PROCEDURE_TEXT)
        change2.setComments("This is a test")
        CheckSum procedureCheckSumWithComments = change2.generateCheckSum()

        then:
        procedureCheckSumWithoutComments == procedureCheckSumWithComments
    }

    def "encoding is not considered on checksum generation"() {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureCheckSumWithoutEncoding = change.generateCheckSum()
        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setProcedureText(PROCEDURE_TEXT)
        change2.setEncoding("UTF-8")
        CheckSum procedureCheckSumWithEncoding = change2.generateCheckSum()

        then:
        procedureCheckSumWithoutEncoding == procedureCheckSumWithEncoding
    }

    def "procedure text updated with whitespaces should not compute a new checksum"() {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureTextCheckSum = change.generateCheckSum()
        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setProcedureText(PROCEDURE_TEXT.concat("      \n"))
        CheckSum procedureTextModifiedCheckSum = change2.generateCheckSum()

        then:
        procedureTextCheckSum == procedureTextModifiedCheckSum
    }

    def "checksum gets updated having a change on procedure text"() {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureTextOriginalCheckSum = change.generateCheckSum()

        StringBuilder procedureTextUpdated = new StringBuilder(PROCEDURE_TEXT)
        procedureTextUpdated.append(" WHERE 1=1")
        change.setProcedureText(procedureTextUpdated.toString())
        CheckSum procedureTextUpdatedCheckSum = change.generateCheckSum()

        then:
        procedureTextOriginalCheckSum != procedureTextUpdatedCheckSum
    }

    def "validate checksum gets re-computed if procedure text gets updated "() {
        when:
        String procedureText =
                """CREATE OR REPLACE PROCEDURE testHello()
                  LANGUAGE plpgsql
                  AS \$\$
        BEGIN
                  raise notice 'valueToReplace';
        END \$\$"""

        def change = new CreateProcedureChange();
        procedureText = procedureText.replace("valueToReplace", "value1")
        change.setProcedureText(procedureText)

        def checkSumFirstReplacement = change.generateCheckSum().toString()

        procedureText = procedureText.replace("value1", "value2")
        change.setProcedureText(procedureText)

        def checkSumSecondReplacement = change.generateCheckSum().toString()

        then:
        checkSumFirstReplacement != checkSumSecondReplacement
    }

    def "relativeToChangelogFile attribute is not considered on checksum generation"() {
        when:
        CreateProcedureChange changeWithoutRelativeToChangelogFileAttribSet = new CreateProcedureChange()
        changeWithoutRelativeToChangelogFileAttribSet.setProcedureText(PROCEDURE_TEXT)
        CheckSum changeWithoutRelativeToChangelogFileAttribSetCheckSum = changeWithoutRelativeToChangelogFileAttribSet.generateCheckSum()

        CreateProcedureChange changeWithRelativeToChangelogFileAttribSet = new CreateProcedureChange()
        changeWithRelativeToChangelogFileAttribSet.setProcedureText(PROCEDURE_TEXT)
        changeWithRelativeToChangelogFileAttribSet.setRelativeToChangelogFile(true)
        CheckSum changeWithRelativeToChangelogFileAttribSetCheckSum = changeWithRelativeToChangelogFileAttribSet.generateCheckSum()

        then:
        changeWithoutRelativeToChangelogFileAttribSetCheckSum == changeWithRelativeToChangelogFileAttribSetCheckSum
    }
}
