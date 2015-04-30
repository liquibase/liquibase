package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplier
import liquibase.database.ConnectionSupplierFactory
import liquibase.database.Database
import liquibase.database.core.UnsupportedDatabase
import liquibase.snapshot.Snapshot
import liquibase.snapshot.TestSnapshotFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import spock.lang.Unroll
import testmd.logic.SetupResult

class SnapshotDatabaseObjectsActionColumnsTest extends AbstractActionTest {

    @Unroll("#featureName: #column on #conn")
    def "can snapshot fully qualified column"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, new Column(column.getName()).setRelation(new Table(column.getRelation().getName())))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([tableName_asTable: column.getRelation().getName(), columnName_asTable: column.getName().getName()])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() == 1
            assert result.asObject(Object) instanceof Column
            assert result.asObject(Column).getName().toShortString() == column.getName().toShortString()
            assert result.asObject(Column).getRelation().getName() == column.getRelation().getName()
        })

        where:
        [conn, snapshot, column] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    new ArrayList(snapshot.get(Column)),
            ])
        }
    }

}
