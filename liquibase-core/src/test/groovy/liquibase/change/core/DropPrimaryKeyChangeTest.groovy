package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table

public class DropPrimaryKeyChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setConstraintName("PK_NAME");

        then:
        "Primary key dropped from TABLE_NAME" == change.getConfirmationMessage()

    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col")
        def pk = new PrimaryKey("pk_test", null, null, table.name, new Column(testColumn.name))

        def change = new DropPrimaryKeyChange()
        change.tableName = table.name
        change.constraintName = pk.name

        then: "table is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Table exists but not column"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Column is there but not primary key"
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(testColumn)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Column is primary key"
        table.setPrimaryKey(pk)
        snapshotFactory.addObjects(pk)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied
    }
}
