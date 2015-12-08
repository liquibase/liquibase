package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.Action
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplier
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.Snapshot
import liquibase.structure.ObjectNameStrategy
import liquibase.structure.ObjectReference
import liquibase.structure.TestColumnSupplier
import liquibase.structure.TestTableSupplier
import liquibase.structure.core.Catalog
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import liquibase.util.LiquibaseUtil
import org.junit.Assume
import spock.lang.Unroll

class SnapshotDatabaseObjectsActionColumnsTest extends AbstractActionTest {

    @Unroll("#featureName: #columnRef on #conn")
    def "can find fully qualified complex column names"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, columnRef)

        runStandardTest([columnName_asTable: columnRef.toString()], action, conn, scope, {
            plan, result ->
                assert result.asList(Column).size() == 1
                assert result.asObject(Object) instanceof Column
                assert result.asObject(Column).toReference() == columnRef
        })

        where:
        [conn, scope, columnRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    getColumnNamesWithTables(scope)
            ])
        }
    }

    @Unroll("#featureName: #tableRef on #conn")
    def "can find all columns in a fully qualified complex table name"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, tableRef)

        runStandardTest([tableName_asTable: tableRef], action, conn, scope, {
            plan, result ->
                assert result.asList(Column).size() > 0
                result.asList(Object).each {
                    assert it instanceof Column;
                    assert it.container == tableRef
                }
        })

        where:
        [conn, scope, tableRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)
            ])
        }
    }

    @Unroll("#featureName: #schemaRef on #conn")
    def "can find all columns in a schema"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, schemaRef)

        runStandardTest([schemaName_asTable: schemaRef], action, conn, scope, {
            plan, result ->
                assert result.asList(Column).size() > 0
                result.asList(Object).each {
                    assert it instanceof Column;
                    assert it.container.container == schemaRef
                }
        })

        where:
        [conn, scope, schemaRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    it.allContainers
            ])
        }
    }

    @Unroll("#featureName: #catalogRef on #conn")
    def "can find all columns in a catalog"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Column, catalogRef)

        runStandardTest([catalogName_asTable: catalogRef], action, conn, scope, {
            plan, result ->
                assert result.asList(Column).size() > 0
                result.asList(Object).each {
                    assert it instanceof Column;
                    assert it.name.container.container.container.name.equals(catalogRef.getSimpleName())
                }
        })

        where:
        [conn, scope, catalogRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support catalogs", ConnectionSupplier.getDatabase.supports(Catalog));

            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    it.allContainers*.container.unique(),
            ])
        }
    }

    @Unroll("#featureName: #columnRef (autoIncrement: #autoIncrement) on #conn")
    def "autoIncrement information set correctly"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(columnRef)

        runStandardTest([columnName_asTable: columnRef, autoIncrement_asTable: autoIncrement], action, conn, scope, {
            plan, result ->
                assert result.asList(Column).size() > 0
                result.asList(Object).each {
                    assert it instanceof Column;
                    assert it.toReference() == columnRef
                    if (autoIncrement) {
                        assert it.autoIncrement
                        //no jdbc interface to get auto increment start/incrementBy info
                    } else {
                        assert !it.autoIncrement
                    }
                }
        }, {
            def executor = scope.getSingleton(ActionExecutor)
            if (autoIncrement) {
                if (((AddAutoIncrementActionTest.TestDetails) new AddAutoIncrementActionTest().getTestDetails(scope)).createPrimaryKeyBeforeAutoIncrement()) {
                    executor.execute(new AddPrimaryKeysAction(new PrimaryKey(null, columnRef.container, columnRef.name)), scope)
                }
                executor.execute(new AddAutoIncrementAction(columnRef, DataType.parse("int"), new Column.AutoIncrementInformation()), scope)
            }
        })

        where:
        [conn, scope, autoIncrement, columnRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            Assume.assumeTrue("Database does not support autoIncrement", ConnectionSupplier.getDatabase.supportsAutoIncrement());

            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [true, false],
                    getColumnNamesWithTables(scope),
            ])
        }
    }

    @Unroll("#featureName: #typeString on #conn")
    def "dataType comes through correctly"() {
        when:
        def schema = conn.allContainers[0]
        def tableName = correctObjectName("testtable", Table, ConnectionSupplier.getDatabase)
        def columnName = correctObjectName("testcol", Column, ConnectionSupplier.getDatabase)
        def type = DataType.parse(typeString)

        def snapshot = new Snapshot(scope)

        def column = new Column(new ObjectReference(schema, tableName), columnName, type)
        snapshot.add(column)
        snapshot.add(new Table(new ObjectReference(schema, tableName)))

        then:
        def action = new SnapshotDatabaseObjectsAction(column.toReference())

        runStandardTest([columnName_asTable: column.getName(), type_asTable: ((Column) column).type], snapshot, action, conn, scope, {
            plan, result ->
            assert result.asList(Column).size() == 1
            def snapshotColumn = result.asObject(Column)
            assert snapshotColumn.name == columnName

            //do some minor checks based on things that are always consistent
            if (typeString == "int") {
                assert snapshotColumn.type.toString().toLowerCase().startsWith("int")
            } else if (typeString == "varchar(10)") {
                assert snapshotColumn.type.toString().toLowerCase().startsWith("varchar") && snapshotColumn.type.toString().contains("(10")
            }

            //since data types change to what the database thinks, test by adding a new columnRef with the snapshot's datatype and check that those are consistant
            def addColumnAction = new AddColumnsAction()
            def columnToAdd = snapshotColumn.clone() as Column
            columnToAdd.name = new ObjectReference(snapshotColumn.container, snapshotColumn.name + "_added")
            addColumnAction.columns = [columnToAdd]

            scope.getSingleton(ActionExecutor).execute(addColumnAction, scope)

            def newColumnSnapshot = LiquibaseUtil.snapshotObject(Column, columnToAdd, scope)

            assert snapshotColumn.type.toString() != null
            assert snapshotColumn.type.toString() == newColumnSnapshot.type.toString()
        })

        where:
        [conn, scope, typeString] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    getDataTypesToTest()
            ])
        }
    }

    protected ArrayList<String> getDataTypesToTest() {
        ["int", "bigint", "smallint", "varchar(10)", "float", "double"]
    }

    @Override
    protected Snapshot createSnapshot(Action action, ConnectionSupplier connectionSupplier, Scope scope) {
        Snapshot snapshot = new Snapshot(scope)
        for (ObjectReference tableName : getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)) {
            snapshot.add(new Table(tableName))
            for (ObjectReference columnName : getObjectNames(TestColumnSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)) {
                snapshot.add(new Column(tableName, columnName.name, "int"))
            }
        }

        return snapshot
    }

    List<ObjectReference> getColumnNamesWithTables(Scope scope) {
        getObjectNames(TestColumnSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope).collectMany {
            def colRef = it
            return getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope).collect {
                return new ObjectReference(Column, it, colRef.name)
            }
        }
    }


}
