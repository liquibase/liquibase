package liquibase.change.core

import liquibase.change.AddColumnConfig
import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table

public class DropNotNullConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        then:
        "Null constraint dropped from TABLE_NAME.COL_HERE" == change.getConfirmationMessage()

    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col").setDefaultValue("def_val").setNullable(false)
        def testColumnConfig = new AddColumnConfig()
        testColumnConfig.type = "varchar(5)"
        testColumnConfig.name = testColumn.name

        def change = new DropNotNullConstraintChange()
        change.tableName = table.name
        change.columnName = testColumn.name

        then: "table is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "Table exists but not column"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "Column is there with a not null constraint"
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(testColumn)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "Column is there without a not null constraint"
        testColumn.nullable = true
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Column is there with an unknown (null) null constraint"
        testColumn.nullable = null
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }
}