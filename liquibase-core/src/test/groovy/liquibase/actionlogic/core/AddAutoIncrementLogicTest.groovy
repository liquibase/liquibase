package liquibase.actionlogic.core

import liquibase.JUnitScope
import liquibase.action.core.AddAutoIncrementAction
import liquibase.actionlogic.ActionExecutor
import liquibase.snapshot.MockSnapshotFactory
import liquibase.snapshot.SnapshotFactory
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import spock.lang.Specification
import spock.lang.Unroll

class AddAutoIncrementLogicTest extends Specification {

    @Unroll
    def "checkStatus"() {
        when:
        def columnName = new ObjectName("testTable", "testColumn")
        def column = new Column(columnName)
        if (columnStartsWith != null && columnIncrementBy != null) {
            column.autoIncrementInformation = new Column.AutoIncrementInformation(columnStartsWith, columnIncrementBy)
        }
        def mockSnapshotFactory = new MockSnapshotFactory(column)
        def scope = JUnitScope.instance.overrideSingleton(SnapshotFactory, mockSnapshotFactory)

        def action = new AddAutoIncrementAction()
        action.columnName = columnName
        action.columnDataType = "int"
        action.startWith = actionStartsWith
        action.incrementBy = actionIncrementBy

        then:
        new ActionExecutor().checkStatus(action, scope).toString() == expected

        where:
        actionStartsWith | columnStartsWith | actionIncrementBy | columnIncrementBy | expected
        2                | 2                | 4                 | 4                 | "Applied"
        2                | 3                | 4                 | 4                 | "Incorrect: 'startWith' is incorrect (expected '2' got '3')"
        2                | 2                | 4                 | 3                 | "Incorrect: 'incrementBy' is incorrect (expected '4' got '3')"
        null             | null             | 1                 | 1                 | "Not Applied: Column 'testTable.testColumn' is not auto-increment"
        null             | 1                | null              | 1                 | "Applied"

    }

    def "checkStatus with no column"() {
        when:
        def scope = JUnitScope.instance.overrideSingleton(SnapshotFactory, new MockSnapshotFactory())

        def action = new AddAutoIncrementAction()
        action.columnName = new ObjectName("testTable", "testColumn")

        then:
        scope.getSingleton(ActionExecutor).checkStatus(action, scope).toString() == "Unknown: Column 'testTable.testColumn' does not exist"

    }
}
