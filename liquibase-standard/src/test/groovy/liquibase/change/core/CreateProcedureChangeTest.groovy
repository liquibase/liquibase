package liquibase.change.core

import liquibase.Scope
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.exception.ValidationErrors
import liquibase.parser.core.ParsedNode
import liquibase.database.core.MockDatabase
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.sqlgenerator.core.CreateProcedureGenerator
import liquibase.test.JUnitResourceAccessor
import liquibase.util.StreamUtil
import spock.lang.Unroll

public class CreateProcedureChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        CreateProcedureChange refactoring = new CreateProcedureChange();

        then:
        "Stored procedure created" == refactoring.getConfirmationMessage()
    }

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
        sqlPath | logicalFilePath | relativeToChangelogFile
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
        database               | dbms   | expectedValidationErrorMsg
        new PostgresDatabase() | "post"                           | String.format("%s is not a supported DB", dbms)
        new PostgresDatabase() | "postgresql"                     | ""
        new MockDatabase()     | "postgresql, h2, mssql, !sqlite" | ""
        new PostgresDatabase() | "none"                           | ""
        new PostgresDatabase() | "all"                            | ""
    }
}
