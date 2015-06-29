package liquibase.change.core

import liquibase.action.ActionStatus
import liquibase.change.StandardChangeTest;
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.DatabaseFunction
import liquibase.statement.SequenceNextValueFunction
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.util.ISODateFormat
import spock.lang.Unroll

public class AddDefaultValueChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");

        then:
        change.getConfirmationMessage() == "Default value added to TABLE_NAME.COLUMN_NAME"
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

        def change = new AddDefaultValueChange()
        change.tableName = table.name
        change.columnName = testColumn.name
        change.columnDataType = "int"
        change."$methodName" = changeDefaultValue

        then:
        assert change.checkStatus(database).status == ActionStatus.Status.unknown

        when: "Objects exist no default value"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ActionStatus.Status.notApplied

        when: "Column has a default value"
        testColumn.defaultValue = snapshotDefaultValue
        then:
        assert change.checkStatus(database).status == expectedResult

        where:
        snapshotDefaultValue | changeDefaultValue | methodName | expectedResult
        "car"                                                                                  | "car"                                     | "defaultValue"             | ActionStatus.Status.applied
        "car"                                                                                  | "boat"                                    | "defaultValue"             | ActionStatus.Status.incorrect
        2                                                                                      | 2                                         | "defaultValueNumeric"      | ActionStatus.Status.applied
        2.1                                                                                    | 2.1                                       | "defaultValueNumeric"      | ActionStatus.Status.applied
        2.1                                                                                    | 8                                         | "defaultValueNumeric"      | ActionStatus.Status.incorrect
        new java.sql.Date(new ISODateFormat().parse("1970-02-13T21:24:58.913").getTime())      | "1970-02-13"                              | "defaultValueDate"         | ActionStatus.Status.applied
        new java.sql.Date(new ISODateFormat().parse("1970-02-13T21:24:58.913").getTime())      | "1870-02-13"                              | "defaultValueDate"         | ActionStatus.Status.incorrect
        new java.sql.Timestamp(new ISODateFormat().parse("1970-02-13T21:24:58.913").getTime()) | "1970-02-13T21:24:58.913"                 | "defaultValueDate"         | ActionStatus.Status.applied
        true                                                                                   | true                                      | "defaultValueBoolean"      | ActionStatus.Status.applied
        true                                                                                   | false                                     | "defaultValueBoolean"      | ActionStatus.Status.incorrect
        false                                                                                  | false                                     | "defaultValueBoolean"      | ActionStatus.Status.applied
        false                                                                                  | true                                      | "defaultValueBoolean"      | ActionStatus.Status.incorrect
        new DatabaseFunction("now()")                                                          | new DatabaseFunction("now()")             | "defaultValueComputed"     | ActionStatus.Status.applied
        new DatabaseFunction("now()")                                                          | new DatabaseFunction("later()")           | "defaultValueComputed"     | ActionStatus.Status.incorrect
        new SequenceNextValueFunction("seq_test")                                              | new SequenceNextValueFunction("seq_test") | "defaultValueSequenceNext" | ActionStatus.Status.applied
        new SequenceNextValueFunction("seq_other")                                             | new SequenceNextValueFunction("seq_test") | "defaultValueSequenceNext" | ActionStatus.Status.incorrect
    }
}
