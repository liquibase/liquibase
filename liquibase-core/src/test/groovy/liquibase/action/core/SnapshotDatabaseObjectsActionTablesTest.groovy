package liquibase.action.core

import liquibase.JUnitScope
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.structure.ObjectName
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import spock.lang.Unroll

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder

class SnapshotDatabaseObjectsActionTablesTest extends AbstractActionTest {

    @Unroll("#featureName: #tableName on #conn")
    def "can snapshot fully qualified table"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Table(tableName))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope).asTable([tableName:tableName])
                .addResult("plan", plan.describe())
                .run({
            def result = plan.execute(scope) as QueryResult

            assert result.asList(Table).size() == 1
            assert result.asObject(Object) instanceof Table
            assert result.asObject(Table).getName() == tableName
        })

        where:
        [conn, snapshot, tableName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Table)*.getName()
            ])
        }
    }

    @Unroll("#featureName: #schemaName on #conn")
    def "can snapshot all tables in schema"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Table(new ObjectName(null, new ObjectName(schemaName))))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .asTable(schemaName: schemaName)
                .addResult("plan", plan.describe())
                .run({
            def result = plan.execute(scope) as QueryResult

            assertThat result.asList(Table), containsInAnyOrder(snapshot.get(Table).grep({it.getName().asList()[1] == schemaName}).toArray() )
        })

        where:
        [conn, snapshot, schemaName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Table)*.getName()*.getContainer()*.getName().unique()
            ])
        }
    }

    @Unroll("#featureName: #catalogName on #conn")
    def "can snapshot all tables in catalog"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Table(new ObjectName(null, new ObjectName(catalogName, null))))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .asTable(catalogName: catalogName)
                .addResult("plan", plan.describe())
                .run({
            def result = plan.execute(scope) as QueryResult

            assertThat result.asList(Table), containsInAnyOrder(snapshot.get(Table).grep({it.getName().asList()[2] == catalogName}).toArray() )
        })

        where:
        [conn, snapshot, catalogName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Table)*.getName()*.getContainer()*.getContainer()*.getName().unique()
            ])
        }
    }
}
