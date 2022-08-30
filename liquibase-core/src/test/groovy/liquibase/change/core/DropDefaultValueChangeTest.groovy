package liquibase.change.core

import liquibase.change.AddColumnConfig
import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table

public class DropDefaultValueChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropDefaultValueChange change = new DropDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        then:
        "Default value dropped from TABLE_NAME.COL_HERE" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col").setDefaultValue("def_val")
        def testColumnConfig = new AddColumnConfig()
        testColumnConfig.type = "varchar(5)"
        testColumnConfig.name = testColumn.name

        def change = new DropDefaultValueChange()
        change.tableName = table.name
        change.columnName = testColumn.name

        then: "table is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "Table exists but not column"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "Column is there with a default value"
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(testColumn)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "Column is there without a default value"
        testColumn.defaultValue = null
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }

}
