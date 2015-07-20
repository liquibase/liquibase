package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.snapshot.transformer.CustomTransformer
import liquibase.snapshot.transformer.LimitTransformer
import liquibase.snapshot.transformer.NoOpTransformer
import liquibase.snapshot.transformer.RoundRobinTransformer
import liquibase.snapshot.transformer.TransformerList
import liquibase.structure.DatabaseObject
import liquibase.structure.ObjectName
import liquibase.structure.core.Catalog
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import liquibase.util.LiquibaseUtil
import org.junit.Assume
import spock.lang.Unroll

class SnapshotDatabaseObjectsActionColumnsTest extends AbstractActionTest {

    @Unroll("#featureName: #column on #conn")
    def "can find fully qualified complex column names"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, column.getObjectReference())

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([columnName_asTable: column.getName()])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() == 1
            assert result.asObject(Object) instanceof Column
            assert result.asObject(Column).getName().container != null
            assert result.asObject(Column).getName() == column.getName()
        })

        where:
        [conn, scope, snapshot, column] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support autoIncrement", it.database.supportsAutoIncrement());

            def scope = JUnitScope.getInstance(it).child(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES)

            def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(NoOpTransformer.instance, scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    new ArrayList(snapshot.get(Column)),
            ])
        }
    }

    @Unroll("#featureName: #table on #conn")
    def "can find all columns in a fully qualified complex table name"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, table.getObjectReference())

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([tableName_asTable: table.getName()])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() > 0
            result.asList(Object).each {
                assert it instanceof Column;
                assert it.getName().container.equals(table.getName(), true)
            }
        })

        where:
        [conn, scope, snapshot, table] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support autoIncrement", it.database.supportsAutoIncrement());

            def scope = JUnitScope.getInstance(it)
                    .child(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES)

            def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(NoOpTransformer.instance, scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    new ArrayList(snapshot.get(Table)),
            ])
        }
    }

    @Unroll("#featureName: #schema on #conn")
    def "can find all columns in a schema"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, schema.getObjectReference())

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([schemaName_asTable: schema.getName()])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() > 0
            result.asList(Object).each {
                assert it instanceof Column;
                assert it.getName().container.container.equals(schema.getName(), true)
            }
        })

        where:
        [conn, scope, snapshot, schema] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support autoIncrement", it.database.supportsAutoIncrement());

            def scope = JUnitScope.getInstance(it)
                    .child(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES)

            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(NoOpTransformer.instance, scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    new ArrayList(snapshot.get(Schema)),
            ])
        }
    }

    @Unroll("#featureName: #catalog on #conn")
    def "can find all columns in a catalog"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, catalog.getObjectReference())

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([catalogName_asTable: catalog.getName()])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() > 0
            result.asList(Object).each {
                assert it instanceof Column;
                assert it.name.container.container.container.name.equals(catalog.getSimpleName())
            }
        })

        where:
        [conn, scope, snapshot, catalog] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support autoIncrement", it.database.supportsAutoIncrement());
            Assume.assumeTrue("Database does not support catalogs", it.database.getMaxContainerDepth(Table) >= 2);

            def scope = JUnitScope.getInstance(it)
                    .child(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES)

            def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(NoOpTransformer.instance, scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope]
                            [snapshot],
                    new ArrayList(snapshot.get(Catalog)),
            ])
        }
    }

    @Unroll("#featureName: #column on #conn")
    def "autoIncrement information set correctly"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, column.getObjectReference())

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([columnName_asTable: column.getName()])
                .addParameters([isAutoIncrement_asTable: ((Column) column).autoIncrement])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() > 0
            result.asList(Object).each {
                assert it instanceof Column;
                assert it.name.equals(column.name, true)
                if (column.autoIncrement) {
                    assert it.autoIncrement
                    //no jdbc interface to get auto increment start/incrementBy info?
//                    assert it.autoIncrementInformation.startWith == new BigInteger(2)
//                    assert it.autoIncrementInformation.incrementBy == new BigInteger(3)
                } else {
                    assert !it.autoIncrement
                }
            }
        })

        where:
        [conn, scope, snapshot, column] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support autoIncrement", it.database.supportsAutoIncrement());

            def scope = JUnitScope.getInstance(it)

            def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(new TransformerList(
                    new LimitTransformer(2),
                    new RoundRobinTransformer([Column] as Class[], new CustomTransformer<Column>() {
                        @Override
                        Column transformObject(Column object, Scope transformScope) {
                            object.autoIncrementInformation=new Column.AutoIncrementInformation(2, 3)
                            return object
                        }
                    }, NoOpTransformer.instance)), scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    new ArrayList(snapshot.get(Column)),
            ])
        }
    }

    @Unroll("#featureName: #typeString on #conn")
    def "dataType comes through correctly"() {
        when:
        column.type = DataType.parse(typeString)
        def executor = scope.getSingleton(ActionExecutor.class) as ActionExecutor

        then:
        def action = new SnapshotDatabaseObjectsAction(Column, column.getObjectReference())


        def plan = executor.createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([columnName_asTable: column.getName()])
                .addParameters([type_asTable: ((Column) column).type])
                .addOperations(plan: plan)
                .run({
            QueryResult result = plan.execute(scope)

            assert result.asList(Column).size() == 1
            def snapshotColumn = result.asObject(Column)
            assert snapshotColumn.name.matches(column.name)

            //do some minor checks based on things that are always consistent
            if (typeString == "int") {
                assert snapshotColumn.type.toString().toLowerCase().startsWith("int")
            } else if (typeString == "varchar(10)") {
                assert snapshotColumn.type.toString().toLowerCase().startsWith("varchar") && snapshotColumn.type.toString().contains("(10")
            }

            //since data types change to what the database thinks, test by adding a new column with the snapshot's datatype and check that those are consistant
            def addColumnAction = new AddColumnsAction()
            def columnToAdd = snapshotColumn.clone() as Column
            columnToAdd.name = new ObjectName(snapshotColumn.name.container, snapshotColumn.simpleName+"_added")
            addColumnAction.columns = [columnToAdd]

            executor.execute(addColumnAction, scope)

            def newColumnSnapshot = LiquibaseUtil.snapshotObject(Column, columnToAdd.getObjectReference(), scope)

            assert snapshotColumn.type.toString() != null
            assert snapshotColumn.type.toString() == newColumnSnapshot.type.toString()
        })

        where:
        [conn, scope, snapshot, column, typeString] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(new LimitTransformer(1), scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    [snapshot.get(Column).iterator().next()],
                    getDataTypesToTest()
            ])
        }
    }

    protected ArrayList<String> getDataTypesToTest() {
        ["int", "bigint", "smallint", "varchar(10)", "float", "double"]
    }
}
