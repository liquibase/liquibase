package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.ExecuteSqlAction
import liquibase.actionlogic.ActionExecutor
import liquibase.actionlogic.QueryResult
import liquibase.database.ConnectionSupplier
import liquibase.database.ConnectionSupplierFactory
import liquibase.database.Database
import liquibase.database.core.UnsupportedDatabase
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import spock.lang.Specification
import spock.lang.Unroll
import testmd.TestMD
import testmd.logic.SetupResult

class SnapshotDatabaseObjectsActionTablesTest extends Specification {

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

    def cleanupDatabase(ConnectionSupplier supplier, scope) {
        Database database = scope.get(Scope.Attr.database, Database)
        if (database instanceof UnsupportedDatabase) {
            return;
        }

        for (def tableName : supplier.getReferenceObjectNames(Table.class, false, false)) {
            if (!database.canStoreObjectName(tableName.name, Table)) {
                continue;
            }
            new ActionExecutor().execute(new DropTableAction(tableName), scope)
        }
    }

    @Unroll("#featureName against #tableName and #connectionSupplier")
    def "can snapshot fully qualified table"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(Table, new Table(tableName))
        def database = conn.database;
        def scope = JUnitScope.getInstance(database)

        def plan = new ActionExecutor().createPlan(action, scope)

        TestMD.test(this.class, "${database.shortName}", database.class).permutation([database: database.class.name, tableName: tableName])
                .asTable("tableName")
                .addResult("plan", plan.describe())
                .setup({
            if (database instanceof UnsupportedDatabase) {
                throw new SetupResult.CannotVerify("Unsupported Database");
            }

            if (!scope.database.canStoreObjectName(tableName.name, Table)) {
                throw new SetupResult.Skip("Case Insensitive Database")
            }

            conn.connect(scope)
            setupDatabase(conn, scope)
        }).cleanup({ cleanupDatabase(conn, scope) })
                .run({
            QueryResult result = plan.execute(scope)

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

}
