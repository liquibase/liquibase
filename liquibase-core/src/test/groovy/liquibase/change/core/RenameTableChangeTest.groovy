package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Table

public class RenameTableChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RenameTableChange()
        change.setOldTableName("OLD_NAME");
        change.setNewTableName("NEW_NAME");

        then:
        "Table OLD_NAME renamed to NEW_NAME" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def oldTable = new Table(null, null, "test_table")
        def newTable = new Table(null, null, "new_table")

        def change = new RenameTableChange()
        change.oldTableName= oldTable.name
        change.newTableName= newTable.name

        then: "neither table is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "old table is there"
        snapshotFactory.addObjects(oldTable)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "old and new tables are there"
        snapshotFactory.addObjects(newTable)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "just new table is there"
        snapshotFactory.removeObjects(oldTable)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }
}
