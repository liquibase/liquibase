package liquibase.change.core

import liquibase.ChecksumVersion
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

    @Unroll
    def "path checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        String testScopeId = Scope.enter([
                "resourceAccessor": new MockResourceAccessor([
                        "viewTest.sql": SELECT_QUERY
                ])
        ])

        CreateViewChange change = new CreateViewChange()
        change.setSelectQuery(SELECT_QUERY)
        CheckSum viewCheckSumWithoutPath = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateViewChange change2 = new CreateViewChange()
        change2.setPath("viewTest.sql")
        CheckSum viewCheckSumWithPath = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change2.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        //TODO: Move this Scope.exit() call into a cleanUpSpec method
        Scope.exit(testScopeId)

        then:
        viewCheckSumWithoutPath.toString() == originalChecksum
        viewCheckSumWithPath.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:19e580c57d8e6c8b638c255dd4555fd5" | "8:2ef85f391e6cf6996a5795d730d4af4d"
        ChecksumVersion.latest() | "9:d2614f3f24d8b212245aba3cb89dfe99" | "9:d2614f3f24d8b212245aba3cb89dfe99"
    }

    @Unroll
    def "encoding checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateViewChange change = new CreateViewChange()
        change.setSelectQuery(SELECT_QUERY)
        CheckSum viewCheckSumWithoutEncoding = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateViewChange change2 = new CreateViewChange()
        change2.setSelectQuery(SELECT_QUERY)
        change2.setEncoding("UTF-8")
        CheckSum viewCheckSumWithEncoding = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change2.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        viewCheckSumWithoutEncoding.toString() == originalChecksum
        viewCheckSumWithEncoding.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:19e580c57d8e6c8b638c255dd4555fd5" | "8:6d47606e02671a961a31a48f8739cf54"
        ChecksumVersion.latest() | "9:d2614f3f24d8b212245aba3cb89dfe99" | "9:d2614f3f24d8b212245aba3cb89dfe99"
    }

    @Unroll
    def "select query updated with whitespaces checksum - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateViewChange change = new CreateViewChange()
        change.setSelectQuery(SELECT_QUERY)
        CheckSum viewTextCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum
        CreateViewChange change2 = new CreateViewChange()
        change2.setSelectQuery(SELECT_QUERY.concat("      \n"))
        CheckSum viewTextModifiedCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change2.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        viewTextCheckSum.toString() == originalChecksum
        viewTextModifiedCheckSum.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:19e580c57d8e6c8b638c255dd4555fd5" | "8:02b22847badc10d15e9cacb4e4edf775"
        ChecksumVersion.latest() | "9:d2614f3f24d8b212245aba3cb89dfe99" | "9:d2614f3f24d8b212245aba3cb89dfe99"
    }

    @Unroll
    def "checksum change on select query - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateViewChange change = new CreateViewChange()
        change.setSelectQuery(SELECT_QUERY)
        CheckSum viewTextOriginalCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        StringBuilder selectQueryUpdated = new StringBuilder(SELECT_QUERY)
        selectQueryUpdated.append(" WHERE 1=1")
        change.setSelectQuery(selectQueryUpdated.toString())
        CheckSum viewTextUpdatedCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        viewTextOriginalCheckSum.toString() == originalChecksum
        viewTextUpdatedCheckSum.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:19e580c57d8e6c8b638c255dd4555fd5" | "8:83489fed19b2a50e0bae541e4be8abf7"
        ChecksumVersion.latest() | "9:d2614f3f24d8b212245aba3cb89dfe99" | "9:1fb192f209a2761a741abe655e6e4af7"
    }

    @Unroll
    def "validate checksum if select query text gets updated - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        String selectQueryText = "SELECT id, name FROM person WHERE id > valueToReplace;"

        selectQueryText = selectQueryText.replace("valueToReplace", "value1")
        def change = new CreateViewChange();
        change.setSelectQuery(selectQueryText)

        def checkSumFirstReplacement = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>).toString()

        selectQueryText = selectQueryText.replace("value1", "value2")
        change.setSelectQuery(selectQueryText)

        def checkSumSecondReplacement = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return change.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>).toString()

        then:
        checkSumFirstReplacement.toString() == originalChecksum
        checkSumSecondReplacement.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:50cd0b94ea36da7e33fd0ad5a20cf0f4" | "8:96b0957737a2d4ae418b28b01e4a3537"
        ChecksumVersion.latest() | "9:61486c8c3c630ee048619555ec12aac0" | "9:cf223f541fd57a336ee1df7f630defc8"
    }

    @Unroll
    def "relativeToChangelogFile attribute checksum generation - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        CreateViewChange changeWithoutRelativeToChangelogFileAttribSet = new CreateViewChange()
        changeWithoutRelativeToChangelogFileAttribSet.setSelectQuery(SELECT_QUERY)
        CheckSum changeWithoutRelativeToChangelogFileAttribSetCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return changeWithoutRelativeToChangelogFileAttribSet.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        CreateViewChange changeWithRelativeToChangelogFileAttribSet = new CreateViewChange()
        changeWithRelativeToChangelogFileAttribSet.setSelectQuery(SELECT_QUERY)
        changeWithRelativeToChangelogFileAttribSet.setRelativeToChangelogFile(true)
        CheckSum changeWithRelativeToChangelogFileAttribSetCheckSum = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return changeWithRelativeToChangelogFileAttribSet.generateCheckSum()
        } as Scope.ScopedRunnerWithReturn<CheckSum>) as CheckSum

        then:
        changeWithoutRelativeToChangelogFileAttribSetCheckSum.toString() == originalChecksum
        changeWithRelativeToChangelogFileAttribSetCheckSum.toString() == updatedChecksum

        where:
        version                  | originalChecksum                     | updatedChecksum
        ChecksumVersion.V8       | "8:19e580c57d8e6c8b638c255dd4555fd5" | "8:0526f22d0b4d1162af5d2ae2059cc6c4"
        ChecksumVersion.latest() | "9:d2614f3f24d8b212245aba3cb89dfe99" | "9:d2614f3f24d8b212245aba3cb89dfe99"
    }
}
