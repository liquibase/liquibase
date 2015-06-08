package liquibase.action.core

import liquibase.JUnitScope
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.structure.ObjectName
import liquibase.structure.core.Catalog
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import org.junit.Assume
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

        testMDPermutation(snapshot, conn, scope).addParameters([tableName_asTable: tableName])
                .addOperations(plan: plan)
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
        def action = new SnapshotDatabaseObjectsAction(Table, new Schema(schemaName))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters(schemaName_asTable: schemaName)
                .addOperations(plan: plan)
                .run({
            def result = plan.execute(scope) as QueryResult


            def expected = snapshot.get(Table).grep({
                it.name.container.toString() == schemaName.toString()
            })
            assertThat result.asList(Table), containsInAnyOrder(expected.toArray())
        })

        where:
        [conn, snapshot, schemaName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Schema)*.getName()
            ])
        }
    }

    @Unroll("#featureName: #schemaName on #conn")
    def "can snapshot all tables in schema with a null table name"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Table(new ObjectName(null, schemaName)))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters(schemaName_asTable: schemaName)
                .addOperations(plan: plan)
                .run({
            def result = plan.execute(scope) as QueryResult


            def expected = snapshot.get(Table).grep({
                it.getName().container.toString() == schemaName.toString()
            }).toArray()
            assertThat result.asList(Table), containsInAnyOrder(expected)
        })

        where:
        [conn, snapshot, schemaName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Schema)*.name
            ])
        }
    }

    @Unroll("#featureName: #catalogName on #conn")
    def "can snapshot all tables in catalog"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Table(new ObjectName(new ObjectName(catalogName, null), null)))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters(catalogName_asTable: catalogName)
                .addOperations(plan: plan)
                .run({
            def result = plan.execute(scope) as QueryResult

            assertThat result.asList(Table), containsInAnyOrder(snapshot.get(Table).grep({
                it.getName().asList()[2] == catalogName
            }).toArray())
        })

        where:
        [conn, snapshot, catalogName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support catalogs", it.database.getMaxContainerDepth(Table) >= 2);

            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Table)*.getName()*.container*.container*.getName().unique()
            ])
        }
    }

    @Unroll("#featureName: #schemaName on #conn")
    def "can snapshot tables related to a schema"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Schema(schemaName))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters(schemaName_asTable: schemaName)
                .addOperations(plan: plan)
                .run({
            def result = plan.execute(scope) as QueryResult

            def expected = snapshot.get(Table).grep({
                it.getName().container.toString() == schemaName.toString()
            }).toArray()
            assertThat result.asList(Table), containsInAnyOrder(expected)
        })

        where:
        [conn, snapshot, schemaName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Schema)*.getName()
            ])
        }
    }

    @Unroll("#featureName: #catalogName on #conn")
    def "can snapshot tables related to a catalog"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Catalog(catalogName))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters(catalogName_asTable: catalogName)
                .addOperations(plan: plan)
                .run({
            def result = plan.execute(scope) as QueryResult

            assertThat result.asList(Table), containsInAnyOrder(snapshot.get(Table).grep({
                it.getName().container.container == catalogName
            }).toArray())
        })

        where:
        [conn, snapshot, catalogName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    snapshot.get(Table)*.getName()*.container*.container.unique()
            ])
        }
    }

}
