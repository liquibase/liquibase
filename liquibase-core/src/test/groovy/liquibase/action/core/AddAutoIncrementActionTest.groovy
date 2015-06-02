package liquibase.action.core

import liquibase.JUnitScope
import liquibase.action.Action
import liquibase.action.TestObjectFactory
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import spock.lang.Specification
import spock.lang.Unroll

class AddAutoIncrementActionTest extends AbstractActionTest {

    @Unroll("#featureName: #column on #conn")
    def "Can add auto increment on databases that support it"() {
        expect:
        def scope = JUnitScope.getInstance(conn)
        action.set(AddAutoIncrementAction.Attr.columnName, column.name)
        action.set(AddAutoIncrementAction.Attr.columnDataType, column.dataType)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([
                columnName_asTable: column.getName(),
                startWith         : action.get("startWith"),
                incrementBy       : action.get("incrementBy")
        ])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            println result
//            assert result.asList(Column).size() == 1
//            assert result.asObject(Object) instanceof Column
//            assert result.asObject(Column).getName().toShortString() == column.getName().toShortString()
//            assert result.asObject(Column).getRelation().getName() == column.getRelation().getName()
        })

        where:
        [conn, snapshot, column, action] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Column),
                    JUnitScope.instance.getSingleton(TestObjectFactory).createAllPermutations(AddAutoIncrementAction, [
                            columnName    : null,
                            columnDataType: null,
                            startWith     : [1, 2, 10],
                            incrementBy   : [1, 5, 20]
                    ], JUnitScope.instance),
            ])
        }

    }
}
