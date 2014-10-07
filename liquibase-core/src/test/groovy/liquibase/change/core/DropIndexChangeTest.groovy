package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest;
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import liquibase.structure.core.Table

public class DropIndexChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        then:
        "Index IDX_NAME dropped from table TABLE_NAME" == refactoring.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def test_col = new Column(Table.class, null, null, table.name, "test_col")
        table.getColumns().add(new Column(Table.class, null, null, table.name, "other_col"))
        table.getColumns().add(test_col)

        def index = new Index("idx_test", null, null, table.name, new Column(test_col.name))

        def change = new DropIndexChange()
        change.indexName = index.name
        change.tableName = table.name

        then: "no table yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Objects exist no index"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "index exists"
        snapshotFactory.addObjects(index)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied
    }
}