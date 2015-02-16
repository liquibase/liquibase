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
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import spock.lang.Unroll
import testmd.logic.SetupResult

class SnapshotDatabaseObjectsActionTablesTest extends AbstractActionTest {

    @Unroll("#featureName #tableName on #conn")
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

    @Unroll("#featureName against #tableName on #conn")
    def "can snapshot all tables in a schema"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Table(tableName))
        def database = conn.database;
        def scope = JUnitScope.getInstance(database)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(conn, scope)
                .asTable(tableName: tableName)
                .addResult("plan", plan.describe())
                .run({
            def result = plan.execute(scope) as QueryResult

            assert result.asList(Table).size() == 1
            assert result.asObject(Object) instanceof Table
            assert result.asObject(Table).getName() == tableName
        })

        where:
        [conn, tableName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            return CollectionUtil.permutations([
                    [it],
                    it.getReferenceObjectNames(Table.class, false, true)
            ])
        }
    }
}
