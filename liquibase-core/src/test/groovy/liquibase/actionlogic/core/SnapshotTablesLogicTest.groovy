package liquibase.actionlogic.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.ExecuteSqlAction
import liquibase.action.core.SnapshotDatabaseObjectsAction
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

class SnapshotTablesLogicTest extends Specification {

    static testTables = ["TEST_TABLE", "test_table", "OTHER_TABLE"]

    def createTables(ConnectionSupplier connectionSupplier, scope) {
        Database database = scope.get(Scope.Attr.database, Database)
        if (database instanceof UnsupportedDatabase) {
            throw SetupResult.OK;
        }

        for (def tableName : testTables) {
            if (!database.canStoreObjectName(tableName, false, Table)) {
                continue;
            }
            new ActionExecutor().execute(new ExecuteSqlAction("create table $tableName (id int, name varchar(255))"), scope)
        }
        throw SetupResult.OK
    }

    def cleanupTables(ConnectionSupplier connectionSupplier, scope) {
        return {
            Database database = scope.get(Scope.Attr.database, Database)
            if (database instanceof UnsupportedDatabase) {
                return;
            }

            for (def tableName : testTables) {
                if (!database.canStoreObjectName(tableName, false, Table)) {
                    continue;
                }
                new ActionExecutor().execute(new ExecuteSqlAction("drop table $tableName"), scope)
            }
        }
    }

    @Unroll("#featureName against #tableName and #connectionSupplier")
    def "can snapshot case sensitive table in primary catalog and schema"() {
        expect:
        def database = connectionSupplier.openDatabase()

        def catalogName = ((ConnectionSupplier) connectionSupplier).getPrimaryCatalog()
        def schemaName =  ((ConnectionSupplier) connectionSupplier).getPrimarySchema()

        def action = new SnapshotDatabaseObjectsAction(Table, new Table().setName(tableName).setSchema(catalogName, schemaName))
        def scope = JUnitScope.getInstance(database)

        def plan = new ActionExecutor().createPlan(action, scope)

        TestMD.test(this.class, "case sensitive table in primary catalog and schema__${database.shortName}", database.class).permutation([database: database.class.name, catalogName: catalogName, schemaName: schemaName, tableName: tableName])
                .asTable("catalogName", "schemaName", "tableName")
                .addResult("actions", plan.describe())
                .setup({
                    if (!database.canStoreObjectName(tableName, false, Table)) {
                        throw new SetupResult.Skip("Case Insensitive Database")
                    }

                    createTables(connectionSupplier, scope)
                }).cleanup(cleanupTables(connectionSupplier, scope))
                .run({
            if (database instanceof UnsupportedDatabase) {
                return;
            }
            QueryResult result = plan.execute(scope)

            assert result.asList(Table).size() == 1
            assert result.asObject(Object) instanceof Table
            assert result.asObject(Table).getName() == tableName
        })

        where:
        [connectionSupplier, tableName] << CollectionUtil.permutations([
                JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers,
                testTables])
    }

}
