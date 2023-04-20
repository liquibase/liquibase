package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase
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
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "Objects exist no default value"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "Column has a default value"
        testColumn.defaultValue = snapshotDefaultValue
        then:
        assert change.checkStatus(database).status == expectedResult

        where:
        snapshotDefaultValue | changeDefaultValue | methodName | expectedResult
        "car"                                                                                  | "car"                                     | "defaultValue"             | ChangeStatus.Status.complete
        "car"                                                                                  | "boat"                                    | "defaultValue"             | ChangeStatus.Status.incorrect
        2                                                                                      | 2                                         | "defaultValueNumeric"      | ChangeStatus.Status.complete
        2.1                                                                                    | 2.1                                       | "defaultValueNumeric"      | ChangeStatus.Status.complete
        2.1                                                                                    | 8                                         | "defaultValueNumeric"      | ChangeStatus.Status.incorrect
        new java.sql.Date(new ISODateFormat().parse("1970-02-13T21:24:58.913").getTime())      | "1970-02-13"                              | "defaultValueDate"         | ChangeStatus.Status.complete
        new java.sql.Date(new ISODateFormat().parse("1970-02-13T21:24:58.913").getTime())      | "1870-02-13"                              | "defaultValueDate"         | ChangeStatus.Status.incorrect
        new java.sql.Timestamp(new ISODateFormat().parse("1970-02-13T21:24:58.913").getTime()) | "1970-02-13T21:24:58.913"                 | "defaultValueDate"         | ChangeStatus.Status.complete
        true                                                                                   | true                                      | "defaultValueBoolean"      | ChangeStatus.Status.complete
        true                                                                                   | false                                     | "defaultValueBoolean"      | ChangeStatus.Status.incorrect
        false                                                                                  | false                                     | "defaultValueBoolean"      | ChangeStatus.Status.complete
        false                                                                                  | true                                      | "defaultValueBoolean"      | ChangeStatus.Status.incorrect
        new DatabaseFunction("now()")                                                          | new DatabaseFunction("now()")             | "defaultValueComputed"     | ChangeStatus.Status.complete
        new DatabaseFunction("now()")                                                          | new DatabaseFunction("later()")           | "defaultValueComputed"     | ChangeStatus.Status.incorrect
        new SequenceNextValueFunction("seq_test")                                              | new SequenceNextValueFunction("seq_test") | "defaultValueSequenceNext" | ChangeStatus.Status.complete
        new SequenceNextValueFunction("seq_other")                                             | new SequenceNextValueFunction("seq_test") | "defaultValueSequenceNext" | ChangeStatus.Status.incorrect
    }
}
