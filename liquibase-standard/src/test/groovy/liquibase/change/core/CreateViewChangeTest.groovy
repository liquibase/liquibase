package liquibase.change.core

import liquibase.Scope
import liquibase.change.ChangeStatus
import liquibase.change.CheckSum
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.core.MockDatabase
import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.View
import liquibase.test.JUnitResourceAccessor
import liquibase.util.StreamUtil
import spock.lang.Unroll

class CreateViewChangeTest extends StandardChangeTest {

    public static final String SELECT_QUERY = "SELECT * FROM TestTable";

    def getConfirmationMessage() throws Exception {
        when:
        CreateViewChange change = new CreateViewChange();
        change.setViewName("VIEW_NAME");

        then:
        "View VIEW_NAME created" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def view = new View(null, null, "test_view")

        def change = new CreateViewChange()
        change.viewName = view.name

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "view created"
        snapshotFactory.addObjects(view)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }

    def "load works with nested query"() {
        when:
        def change = new CreateViewChange()
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "createView").addChild(null, "viewName", "my_view").setValue("select * from test"), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        change.viewName == "my_view"
        change.selectQuery == "select * from test"
    }

    @Unroll
    def "openSqlStream correctly opens files"() {
        when:
        def changelog = new DatabaseChangeLog("com/example/changelog.xml")

        def changeset = new ChangeSet("1", "auth", false, false, logicalFilePath, null, null, changelog)

        def change = new CreateViewChange()
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

    def "path is not considered on checksum generation"() {
        when:
        String testScopeId = Scope.enter([
                "resourceAccessor": new MockResourceAccessor([
                        "viewTest.sql": SELECT_QUERY
                ])
        ])

        CreateViewChange change = new CreateViewChange()
        change.setSelectQuery(SELECT_QUERY)
        CheckSum viewCheckSumWithoutPath = change.generateCheckSum()
        CreateViewChange change2 = new CreateViewChange()
        change2.setPath("viewTest.sql")
        CheckSum viewCheckSumWithPath = change2.generateCheckSum()
        //TODO: Move this Scope.exit() call into a cleanUpSpec method
        Scope.exit(testScopeId)

        then:
        assert viewCheckSumWithoutPath == viewCheckSumWithPath
    }

    def "encoding is not considered on checksum generation"() {
        when:
        CreateViewChange change = new CreateViewChange()
        change.setSelectQuery(SELECT_QUERY)
        CheckSum viewCheckSumWithoutEncoding = change.generateCheckSum()
        CreateViewChange change2 = new CreateViewChange()
        change2.setSelectQuery(SELECT_QUERY)
        change2.setEncoding("UTF-8")
        CheckSum viewCheckSumWithEncoding = change2.generateCheckSum()

        then:
        assert viewCheckSumWithoutEncoding == viewCheckSumWithEncoding
    }

    def "select query updated with whitespaces should not be computed as a new checksum"() {
        when:
        CreateViewChange change = new CreateViewChange()
        change.setSelectQuery(SELECT_QUERY)
        CheckSum viewTextCheckSum = change.generateCheckSum()
        CreateViewChange change2 = new CreateViewChange()
        change2.setSelectQuery(SELECT_QUERY.concat("      \n"))
        CheckSum viewTextModifiedCheckSum = change2.generateCheckSum()

        then:
        assert viewTextCheckSum == viewTextModifiedCheckSum
    }

    def "checksum gets updated having a change on select query"() {
        when:
        CreateViewChange change = new CreateViewChange()
        change.setSelectQuery(SELECT_QUERY)
        CheckSum viewTextOriginalCheckSum = change.generateCheckSum()

        StringBuilder selectQueryUpdated = new StringBuilder(SELECT_QUERY)
        selectQueryUpdated.append(" WHERE 1=1")
        change.setSelectQuery(selectQueryUpdated.toString())
        CheckSum viewTextUpdatedCheckSum = change.generateCheckSum();

        then:
        assert viewTextOriginalCheckSum.equals(viewTextUpdatedCheckSum) == false
    }

    def "validate checksum gets re-computed if select query text gets updated"() {
        when:
        String selectQueryText = "SELECT id, name FROM person WHERE id > valueToReplace;"

        selectQueryText = selectQueryText.replace("valueToReplace", "value1")
        def change = new CreateViewChange();
        change.setSelectQuery(selectQueryText)

        def checkSumFirstReplacement = change.generateCheckSum().toString()

        selectQueryText = selectQueryText.replace("value1", "value2")
        change.setSelectQuery(selectQueryText)

        def checkSumSecondReplacement = change.generateCheckSum().toString()

        then:
        checkSumFirstReplacement != checkSumSecondReplacement
    }
}
