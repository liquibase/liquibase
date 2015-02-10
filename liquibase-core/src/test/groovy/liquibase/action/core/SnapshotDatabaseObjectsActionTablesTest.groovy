package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplier
import liquibase.database.ConnectionSupplierFactory
import liquibase.database.Database
import liquibase.database.core.UnsupportedDatabase
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import spock.lang.Unroll
import testmd.logic.SetupResult

class SnapshotDatabaseObjectsActionTablesTest extends AbstractActionTest {

    def setupDatabase(ConnectionSupplier supplier, Scope scope) {
        Database database = scope.database
        if (database instanceof UnsupportedDatabase) {
            throw SetupResult.OK;
        }

        for (def tableName : supplier.getReferenceObjectNames(Table.class, false, false)) {
            if (!database.canStoreObjectName(tableName.getName(), Table)) {
                continue;
            }
            new ActionExecutor().execute(new CreateTableAction(tableName).addColumn (new ColumnDefinition("ID", "int")), scope)
        }
        throw SetupResult.OK
    }

    @Unroll("#featureName against #tableName on #conn")
    def "can snapshot fully qualified table"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Table(tableName))
        def scope = JUnitScope.getInstance(conn)

        def plan = new ActionExecutor().createPlan(action, scope)

        testMDPermutation(conn, scope).asTable([tableName:tableName])
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
                    it.getReferenceObjectNames(Table.class, false, false)
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
