package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.Action
import liquibase.action.TestObjectFactory
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import spock.lang.Specification
import spock.lang.Unroll

class AddAutoIncrementActionTest extends AbstractActionTest {

    @Unroll("#featureName: #column on #conn")
    def "Can add auto increment on databases that support it"() {
        when:
        action.columnName = new ObjectName(column.relation.name, column.name.name)
        action.columnDataType = column.type

        then:
        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([
                columnName_asTable: action.columnName.toString(),
                startWith_asTable         : action.startWith,
                incrementBy_asTable       : action.incrementBy
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
        [conn, scope, snapshot, column, action] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(scope)
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
