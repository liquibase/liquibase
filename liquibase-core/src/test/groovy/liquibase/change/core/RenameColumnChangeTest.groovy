package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table

public class RenameColumnChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RenameColumnChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setOldColumnName("oldColName");
        change.setNewColumnName("newColName");

        then:
        "Column TABLE_NAME.oldColName renamed to newColName" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumnOld = new Column(Table.class, null, null, table.name, "test_col")
        def testColumnNew = new Column(Table.class, null, null, table.name, "test_col_new")

        def change = new RenameColumnChange()
        change.tableName = table.name
        change.oldColumnName = testColumnOld.name
        change.newColumnName = testColumnNew.name

        then: "neither table is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "table exists but not old or new column"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "old column is there"
        table.getColumns().add(testColumnOld)
        snapshotFactory.addObjects(testColumnOld)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "old and new columns are there"
        table.getColumns().add(testColumnNew)
        snapshotFactory.addObjects(testColumnNew)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "just new column is there"
        table.getColumns().remove(testColumnOld)
        snapshotFactory.removeObjects(testColumnOld)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }
}
