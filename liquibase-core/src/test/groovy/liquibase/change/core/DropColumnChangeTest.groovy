package liquibase.change.core

import liquibase.change.AddColumnConfig
import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest;
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table

public class DropColumnChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        then:
        "Column TABLE_NAME.COL_HERE dropped" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col")
        def testColumnConfig = new AddColumnConfig()
        testColumnConfig.type = "varchar(5)"
        testColumnConfig.name = testColumn.name

        def testColumn2 = new Column(Table.class, null, null, table.name, "test_col2")
        def testColumnConfig2 = new AddColumnConfig()
        testColumnConfig2.type = "varchar(50)"
        testColumnConfig2.name = testColumn2.name


        table.getColumns().add(new Column(Table.class, null, null, table.name, "other_col"))
        table.getColumns().add(new Column(Table.class, null, null, table.name, "another_col"))

        def change = new DropColumnChange()
        change.tableName = table.name
        change.columnName = testColumn.name

        then: "table is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Table exists but not column"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Column is there"
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(testColumn)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied
   }
}