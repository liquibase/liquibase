package liquibase.change.core

import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.DatabaseFunction
import liquibase.statement.SequenceNextValueFunction
import liquibase.structure.core.Column
import liquibase.structure.core.Table
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

    @Unroll("#featureName with snapshot/change default values of #snapshotDefaultValue / #changeDefaultValue")
    def "verifyExecuted"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col")
        table.getColumns().add(new Column(Table.class, null, null, table.name, "other_col"))
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(table)

        def change = new AddDefaultValueChange()
        change.tableName = table.name
        change.columnName = testColumn.name
        change.columnDataType = "int"
        change."$methodName" = changeDefaultValue

        then:
        assert change.verifyExecuted(database).verifiedFailed

        when: "Objects exist no default value"
        snapshotFactory.addObjects(table)
        then:
        assert change.verifyExecuted(database).verifiedFailed

        when: "Column has a default value"
        testColumn.defaultValue = snapshotDefaultValue
        then:
        assert change.verifyExecuted(database).verifiedPassed == expectedExecutedResult

        where:
        snapshotDefaultValue | changeDefaultValue | methodName | expectedExecutedResult | expectedNotExecutedResult
        "car"                                      | "car"                                     | "defaultValue"             | true  | false
        "car"                                      | "boat"                                    | "defaultValue"             | false | true
        2                                          | 2                                         | "defaultValueNumeric"      | true  | false
        2.1                                        | 2.1                                       | "defaultValueNumeric"      | true  | false
        2.1                                        | 8                                         | "defaultValueNumeric"      | false | true
        new java.sql.Date(3813898913)              | "1970-02-13"                              | "defaultValueDate"         | true  | false
        new java.sql.Date(3813898913)              | "1870-02-13"                              | "defaultValueDate"         | false | true
        new java.sql.Timestamp(3813898913)         | "1970-02-13T21:24:58.913"                 | "defaultValueDate"         | true  | false
        true                                       | true                                      | "defaultValueBoolean"      | true  | false
        true                                       | false                                     | "defaultValueBoolean"      | false | true
        false                                      | false                                     | "defaultValueBoolean"      | true  | false
        false                                      | true                                      | "defaultValueBoolean"      | false | true
        new DatabaseFunction("now()")              | new DatabaseFunction("now()")             | "defaultValueComputed"     | true  | false
        new DatabaseFunction("now()")              | new DatabaseFunction("later()")           | "defaultValueComputed"     | false | true
        new SequenceNextValueFunction("seq_test")  | new SequenceNextValueFunction("seq_test") | "defaultValueSequenceNext" | true  | false
        new SequenceNextValueFunction("seq_other") | new SequenceNextValueFunction("seq_test") | "defaultValueSequenceNext" | false | true
    }
}
