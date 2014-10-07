package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Table

public class DropForeignKeyConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setConstraintName("FK_NAME");

        then:
        "Foreign key FK_NAME dropped" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def baseTable = new Table(null, null, "base_table")
        def baseColumn = new Column(Table.class, null, null, baseTable.name, "base_col")
        baseTable.getColumns().add(new Column(Table.class, null, null, baseTable.name, "other_col"))
        baseTable.getColumns().add(baseColumn)

        def refTable = new Table(null, null, "ref_table")
        def refColumn = new Column(Table.class, null, null, refTable.name, "ref_col")

        def fk = new ForeignKey("fk_test", null, null, baseTable.name, new Column(baseColumn.name)).setPrimaryKeyTable(refTable).addPrimaryKeyColumn(new Column(refColumn.name))

        snapshotFactory.addObjects(baseTable, refColumn)

        def change = new DropForeignKeyConstraintChange()
        change.baseTableName = baseTable.name
        change.constraintName = fk.name

        then: "no table yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Objects exist no FK"
        snapshotFactory.addObjects(baseTable, refTable)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "FK exists"
        snapshotFactory.addObjects(fk)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied
    }
}
