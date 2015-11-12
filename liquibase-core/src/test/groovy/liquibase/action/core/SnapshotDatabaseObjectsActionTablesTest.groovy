package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.Action
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplier
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.Snapshot
import liquibase.snapshot.TestSnapshotFactory
import liquibase.snapshot.transformer.NoOpTransformer
import liquibase.structure.ObjectNameStrategy
import liquibase.structure.ObjectReference
import liquibase.structure.TestTableSupplier
import liquibase.structure.core.Catalog
import liquibase.structure.core.Column
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import org.junit.Assume
import spock.lang.Unroll

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder

class SnapshotDatabaseObjectsActionTablesTest extends AbstractActionTest {

    @Unroll("#featureName: #tableRef on #conn")
    def "can snapshot fully qualified table"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, tableRef)

        runStandardTest([tableName_asTable: tableRef], action, conn, scope, { plan, result ->
            assert result.asList(Table).size() == 1
            assert result.asObject(Object) instanceof Table
            assert result.asObject(Table).getName() == tableRef.name
        })

        where:
        [scope, conn, tableRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)
            return CollectionUtil.permutations([
                    [scope],
                    [it],
                    getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)
            ])
        }
    }

    @Unroll("#featureName: #schemaRef on #conn")
    def "can snapshot all tables in schema"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, schemaRef)

        runStandardTest([schemaName_asTable: schemaRef], action, conn, scope, { plan, result ->
            def expected = result.asList(Table).grep({
                it.container == schemaRef
            })
            assertThat result.asList(Table), containsInAnyOrder(expected.toArray())
        })

        where:
        [scope, conn, schemaRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [scope],
                    [it],
                    it.allContainers
            ])
        }
    }

    @Unroll("#featureName: #schemaRef on #conn")
    def "can snapshot all tables in schema using a null table name reference"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new ObjectReference(Table, new ObjectReference(Table, schemaRef, null)))

        runStandardTest([schemaName_asTable: schemaRef], action, conn, scope, { plan, result ->
            def expected = result.asList(Table).grep({
                it.container == schemaRef
            })
            assertThat result.asList(Table), containsInAnyOrder(expected.toArray())
        })

        where:
        [scope, conn, schemaRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [scope],
                    [it],
                    it.allContainers
            ])
        }
    }

    @Unroll("#featureName: #catalogRef on #conn")
    def "can snapshot all tables in catalog"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new ObjectReference(Table, new ObjectReference(new ObjectReference(catalogRef, null), null)))

        runStandardTest([catalogName_asTable: catalogRef], action, conn, scope, {
            plan, result ->
                def expected = result.asList(Table).grep({
                    it.container.container == catalogRef
                })

                assertThat result.asList(Table), containsInAnyOrder(expected.toArray())
        })

        where:
        [conn, snapshot, catalogRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support catalogs", it.database.supports(Catalog))
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [scope],
                    [it],
                    it.allContainers*.container.grep({ it != null }).unique()
            ])
        }
    }

    @Override
    protected Snapshot createSnapshot(Action action, ConnectionSupplier connectionSupplier, Scope scope) {
        Snapshot snapshot = new Snapshot(scope)
        for (ObjectReference tableName : getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)) {
            snapshot.add(new Table(tableName))
            snapshot.add(new Column(tableName, correctObjectName("id", Column, scope.getDatabase()), "int"))
        }
        return snapshot
    }
}
