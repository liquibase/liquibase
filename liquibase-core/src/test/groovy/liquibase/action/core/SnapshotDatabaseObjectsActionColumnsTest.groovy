package liquibase.action.core

import liquibase.JUnitScope
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.structure.core.Catalog
import liquibase.structure.core.Column
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import org.junit.Assume
import spock.lang.Unroll

class SnapshotDatabaseObjectsActionColumnsTest extends AbstractActionTest {

    @Unroll("#featureName: #column on #conn")
    def "can find a single column in a table"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, new Column(Table, column.getRelation().getName(), column.getSimpleName()))
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

    @Unroll("#featureName: #table on #conn")
    def "can find all columns in a table"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, new Table(table.getName()))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([tableName_asTable: table.getName()])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() > 0
            result.asList(Object).each {
                assert  it instanceof  Column;
                assert it.getRelation().getName().equals(table.getName())
            }
        })

        where:
        [conn, snapshot, table] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    new ArrayList(snapshot.get(Table)),
            ])
        }
    }

    @Unroll("#featureName: #schema on #conn")
    def "can find all columns in a schema"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, new Schema(schema.getName()))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([schemaName_asTable: schema.getName()])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() > 0
            result.asList(Object).each {
                assert  it instanceof  Column;
                assert it.getRelation().getName().getContainer().equals(schema.getName())
            }
        })

        where:
        [conn, snapshot, schema] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    new ArrayList(snapshot.get(Schema)),
            ])
        }
    }

    @Unroll("#featureName: #catalog on #conn")
    def "can find all columns in a catalog"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, new Catalog(catalog.getName()))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([catalogName_asTable: catalog.getName()])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() > 0
            result.asList(Object).each {
                assert  it instanceof  Column;
                assert it.getRelation().getName().getContainer().getContainer().getName().equals(catalog.getSimpleName())
            }
        })

        where:
        [conn, snapshot, catalog] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support catalogs", it.database.getMaxContainerDepth(Table) >= 2);

            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(it, JUnitScope.instance)
            return CollectionUtil.permutations([
                    [it],
                    [snapshot],
                    new ArrayList(snapshot.get(Catalog)),
            ])
        }
    }
}
