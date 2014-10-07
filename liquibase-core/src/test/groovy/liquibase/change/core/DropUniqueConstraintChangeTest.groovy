package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.structure.core.UniqueConstraint

public class DropUniqueConstraintChangeTest  extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new DropUniqueConstraintChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setConstraintName("UQ_CONSTRAINT");

        then:
        "Unique constraint UQ_CONSTRAINT dropped from TAB_NAME" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col").setDefaultValue("def_val").setNullable(false)
        def constraint = new UniqueConstraint("uq_test", null, null, table.name, new Column(testColumn.name))

        def change = new DropUniqueConstraintChange()
        change.tableName = table.name
        change.constraintName = constraint.name
        change.uniqueColumns = testColumn.name

        then: "table is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Table exists but not column"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Column is there without a constraint"
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(testColumn)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Column is there with a constraint"
        snapshotFactory.addObjects(constraint)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied
    }
}
