package liquibase.change.core

import liquibase.ChecksumVersion
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
        change.load(new ParsedNode(null, "createProcedure").setValue("create procedure sql"), new MockResourceAccessor())
        change.validate(new OracleDatabase())

        then:
        change.serialize().toString() == "createProcedure[preserveNullValues=true,procedureText=create procedure sql]"
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

    @Unroll
    def "dbms checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureCheckSumWithoutDbms = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setProcedureText(PROCEDURE_TEXT)
        change2.setDbms("postgresql")
        CheckSum procedureCheckSumWithDbms = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change2.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        procedureCheckSumWithoutDbms.toString() == originalChecksum
        procedureCheckSumWithDbms.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:d991ad6445f09f7160d9784f0d71dc8b" | "8:44416adece5a36bcbc9f8a77c9595640"
        ChecksumVersion.latest() | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13" | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13"
    }

    @Unroll
    def "path checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        String testScopeId = Scope.enter([
                "resourceAccessor": new MockResourceAccessor([
                        "test.sql": PROCEDURE_TEXT
                ])
        ])

        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureCheckSumWithoutPath = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setPath("test.sql")
        //Below check sum generation should not take path property into account to v9
        CheckSum procedureCheckSumWithPath = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change2.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        //TODO: Move this Scope.exit() call into a cleanUpSpec method
        Scope.exit(testScopeId)

        then:
        procedureCheckSumWithoutPath.toString() == originalChecksum
        procedureCheckSumWithPath.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:d991ad6445f09f7160d9784f0d71dc8b" | "8:35783a67e899c9802efd095760240fab"
        ChecksumVersion.latest() | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13" | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13"
    }

    @Unroll
    def "comment checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureCheckSumWithoutComments = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setProcedureText(PROCEDURE_TEXT)
        change2.setComments("This is a test")
        CheckSum procedureCheckSumWithComments = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change2.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        procedureCheckSumWithoutComments.toString() == originalChecksum
        procedureCheckSumWithComments.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:d991ad6445f09f7160d9784f0d71dc8b" | "8:6a007be59d0d80d1c1f8f35247ae7359"
        ChecksumVersion.latest() | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13" | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13"
    }

    @Unroll
    def "encoding checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureCheckSumWithoutEncoding = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setProcedureText(PROCEDURE_TEXT)
        change2.setEncoding("UTF-8")
        CheckSum procedureCheckSumWithEncoding = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change2.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        procedureCheckSumWithoutEncoding.toString() == originalChecksum
        procedureCheckSumWithEncoding.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:d991ad6445f09f7160d9784f0d71dc8b" | "8:05f6ea846d5521f2be45d394c2eaee8b"
        ChecksumVersion.latest() | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13" | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13"
    }

    @Unroll
    def "procedure text updated with whitespaces checksum - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureTextCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateProcedureChange change2 = new CreateProcedureChange()
        change2.setProcedureText(PROCEDURE_TEXT.concat("      \n"))
        CheckSum procedureTextModifiedCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change2.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        procedureTextCheckSum.toString() == originalChecksum
        procedureTextModifiedCheckSum.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:d991ad6445f09f7160d9784f0d71dc8b" | "8:e181cbb03382a46f7f8b86ad68cc8407"
        ChecksumVersion.latest() | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13" | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13"
    }

    @Unroll
    def "checksum change on procedure text - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText(PROCEDURE_TEXT)
        CheckSum procedureTextOriginalCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        StringBuilder procedureTextUpdated = new StringBuilder(PROCEDURE_TEXT)
        procedureTextUpdated.append(" WHERE 1=1")
        change.setProcedureText(procedureTextUpdated.toString())
        CheckSum procedureTextUpdatedCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        procedureTextOriginalCheckSum.toString() == originalChecksum
        procedureTextUpdatedCheckSum.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:d991ad6445f09f7160d9784f0d71dc8b" | "8:2e38724b39f5204a13470316128969db"
        ChecksumVersion.latest() | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13" | "9:55e1d2b3ce3a9741bf3217059e8cd75d"
    }

    @Unroll
    def "validate checksum gets re-computed if procedure text gets updated - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
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

        def checkSumFirstReplacement = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>).toString()

        procedureText = procedureText.replace("value1", "value2")
        change.setProcedureText(procedureText)

        def checkSumSecondReplacement = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>).toString()

        then:
        checkSumFirstReplacement == originalChecksum
        checkSumSecondReplacement == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:be6d54614a75bc0568533de0f9dcebf5" | "8:373c05c9a7e370614c04ab26177c4771"
        ChecksumVersion.latest() | "9:fab319ffe0437e2b9c893665d5f3c17c" | "9:157ec13f574823dd684ccc48860d19e9"
    }

    @Unroll
    def "relativeToChangelogFile attribute checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateProcedureChange changeWithoutRelativeToChangelogFileAttribSet = new CreateProcedureChange()
        changeWithoutRelativeToChangelogFileAttribSet.setProcedureText(PROCEDURE_TEXT)
        CheckSum changeWithoutRelativeToChangelogFileAttribSetCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return changeWithoutRelativeToChangelogFileAttribSet.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateProcedureChange changeWithRelativeToChangelogFileAttribSet = new CreateProcedureChange()
        changeWithRelativeToChangelogFileAttribSet.setProcedureText(PROCEDURE_TEXT)
        changeWithRelativeToChangelogFileAttribSet.setRelativeToChangelogFile(true)
        CheckSum changeWithRelativeToChangelogFileAttribSetCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return changeWithRelativeToChangelogFileAttribSet.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        changeWithoutRelativeToChangelogFileAttribSetCheckSum.toString() == originalChecksum
        changeWithRelativeToChangelogFileAttribSetCheckSum.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:d991ad6445f09f7160d9784f0d71dc8b" | "8:110a3651bf1da1fd80e3ad8dfa3f160e"
        ChecksumVersion.latest() | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13" | "9:be11b6c3ed2d0fcede7bbdf6cbe2db13"
    }

    def "v8 checksum generation"() {
        when:
        CreateProcedureChange change = new CreateProcedureChange()
        change.setProcedureText('CREATE PROCEDURE insert_data( a integer, b integer)\nLANGUAGE SQL\nAS $$\nINSERT INTO tbl VALUES (a);\nINSERT INTO tbl VALUES (b);\n$$;"')
        CheckSum checkSum = Scope.child([(Scope.Attr.checksumVersion.name()): ChecksumVersion.V8], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        checkSum.toString() == "8:4c7c88bbd9a57bdb528502a08a9dd681"
    }
}
