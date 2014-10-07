package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.structure.core.UniqueConstraint

public class AddUniqueConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddUniqueConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");

        then:
        change.getConfirmationMessage() == "Unique constraint added to TABLE_NAME(COL_HERE)"
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col")
        table.getColumns().add(new Column(Table.class, null, null, table.name, "other_col"))
        table.getColumns().add(testColumn)

        def change = new AddUniqueConstraintChange()
        change.tableName = table.name
        change.columnNames = testColumn.name

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "Column exists but not index"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "Column is unique"
        def constraint = new UniqueConstraint(null, null, null, table.name, new Column(testColumn.name))
        snapshotFactory.addObjects(constraint)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }

}
