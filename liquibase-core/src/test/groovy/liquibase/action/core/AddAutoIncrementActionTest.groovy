package liquibase.action.core

import liquibase.JUnitScope
import liquibase.action.ActionStatus
import liquibase.action.TestObjectFactory
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.ActionResult
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.MockSnapshotFactory
import liquibase.snapshot.SnapshotFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.snapshot.transformer.LimitTransformer
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.util.CollectionUtil
import spock.lang.Unroll

class AddAutoIncrementActionTest extends AbstractActionTest {

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
        action.checkStatus(scope).toString() == expected

        where:
        actionStartsWith | columnStartsWith | actionIncrementBy | columnIncrementBy | expected
        2                | 2                | 4                 | 4                 | "Applied"
        2                | 3                | 4                 | 4                 | "Incorrect: 'startWith' is incorrect ('2' vs '3')"
        2                | 2                | 4                 | 3                 | "Incorrect: 'incrementBy' is incorrect ('4' vs '3')"
        null             | null             | 1                 | 1                 | "Not Applied: Column 'testTable.testColumn' is not auto-increment"
        null             | 1                | null              | 1                 | "Applied"

    }

    def "checkStatus with no column"() {
        when:
        def scope = JUnitScope.instance.overrideSingleton(SnapshotFactory, new MockSnapshotFactory())

        def action = new AddAutoIncrementAction()
        action.columnName = new ObjectName("testTable", "testColumn")

        then:
        action.checkStatus(scope).toString() == "Unknown: Column 'testTable.testColumn' does not exist"

    }

    @Unroll("#featureName: #column on #conn")
    def "Can apply standard settings to complex names"() {
        when:
        def action = new AddAutoIncrementAction()
        action.columnName = column.name
        action.columnDataType = column.type

        then:
        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([
                columnName_asTable: action.columnName.toString()
        ])
                .addOperations(plan: plan)
                .run({
            plan.execute(scope)

            assert action.checkStatus(scope).applied
        })

        where:
        [conn, scope, snapshot, column] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it).child(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES)
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(new LimitTransformer(2), scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    snapshot.get(Column)
            ])
        }

    }

    @Unroll("#featureName: #action for #column on #conn")
    def "Valid parameter permutations work"() {
        when:
        action.columnName = column.name
        action.columnDataType = column.type

        then:
        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([
                columnName_asTable : action.columnName.toString(),
                startWith_asTable  : action.startWith,
                incrementBy_asTable: action.incrementBy
        ])
                .addOperations(plan: plan)
                .run({
            plan.execute(scope)

            assert action.checkStatus(scope).applied
        })

        where:
        [conn, scope, snapshot, column, action] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(new LimitTransformer(2), scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    snapshot.get(Column),
                    JUnitScope.instance.getSingleton(TestObjectFactory).createAllPermutations(AddAutoIncrementAction, [
                            columnName    : null,
                            columnDataType: null,
                            startWith     : [1, 2, 10],
                            incrementBy   : [1, 5, 20]
                    ]),
            ])
        }

    }
}
