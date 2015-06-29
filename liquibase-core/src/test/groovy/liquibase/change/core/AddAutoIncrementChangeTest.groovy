package liquibase.change.core;

import liquibase.change.ChangeFactory
import liquibase.action.ActionStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Unroll

public class AddAutoIncrementChangeTest extends StandardChangeTest {

    def getAppliesTo() {
        expect:
        def change = new AddAutoIncrementChange();
        ChangeFactory.getInstance().getChangeMetaData(change).getAppliesTo().iterator().next() == "column"
    }


    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        then:
        change.getConfirmationMessage() == "Auto-increment added to TABLE_NAME.COLUMN_NAME"
    }

    def "check change metadata"() {
        expect:
        def change = new AddAutoIncrementChange();
        def metaData = ChangeFactory.getInstance().getChangeMetaData(change);
        metaData.getName() == "addAutoIncrement"

    }

    @Unroll
    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col")
        table.getColumns().add(new Column(Table.class, null, null, table.name, "other_col"))
        table.getColumns().add(testColumn)

        def change = new AddAutoIncrementChange()
        change.tableName = table.name
        change.columnName = testColumn.name

        then:
        assert change.checkStatus(database).status == ActionStatus.Status.unknown


        when: "Objects exist but not auto-increment"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ActionStatus.Status.notApplied

        when: "Column is auto-increment"
        testColumn.autoIncrementInformation = new Column.AutoIncrementInformation(columnStartWith, columnIncrementBy)
        change.startWith = changeStartWith
        change.incrementBy = changeIncrementBy
        then:
        change.checkStatus(database).status == expectedResult

        where:
        columnStartWith | columnIncrementBy | changeStartWith | changeIncrementBy | expectedResult
        null | null | null | null | ActionStatus.Status.applied
        2    | 4    | null | null | ActionStatus.Status.applied
        2    | 4    | 2    | null | ActionStatus.Status.applied
        2    | 4    | null | 4    | ActionStatus.Status.applied
        2    | 4    | 2    | 4    | ActionStatus.Status.applied
        3    | 5    | 1    | 5    | ActionStatus.Status.incorrect
        3    | 5    | 3    | 2    | ActionStatus.Status.incorrect

    }
}
