package liquibase.action.core;

import liquibase.JUnitScope;
import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.actionlogic.ActionExecutor;
import liquibase.actionlogic.QueryResult;
import liquibase.database.ConnectionSupplier;
import liquibase.database.ConnectionSupplierFactory;
import liquibase.database.Database;
import liquibase.database.core.UnsupportedDatabase
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.CollectionUtil
import liquibase.util.StringUtils
import spock.lang.Specification;
import spock.lang.Unroll;
import testmd.TestMD;
import testmd.logic.SetupResult;

class SnapshotColumnsActionTest extends Specification {

    static testTables = ["TEST_TABLE", "test_table", "OTHER_TABLE"]
    static testColumns = ["TEST_COLUMN", "test_column", "OTHER_COLUMN"]

    def setupDatabase(ConnectionSupplier connectionSupplier, scope) {
        Database database = scope.get(Scope.Attr.database, Database)
        if (database instanceof UnsupportedDatabase) {
            throw SetupResult.OK;
        }

        def columnDefs = testColumns.findAll({return database.canStoreObjectName(it, false, Column)}).collect({return "${it} int"})

        for (def tableName : testTables) {
            if (!database.canStoreObjectName(tableName, false, Table)) {
                continue;
            }

            new ActionExecutor().execute(new ExecuteSqlAction("create table $tableName ("+ StringUtils.join(columnDefs, ", ")+")"), scope)
        }
        throw SetupResult.OK
    }

    def cleanupDatabase(ConnectionSupplier connectionSupplier, scope) {
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

    @Unroll("#featureName against #tableName . #columnName and #connectionSupplier")
    def "can snapshot case sensitive table and column combination in primary catalog and schema"() {
        expect:
        def database = connectionSupplier.openDatabase()

        def catalogName = ((ConnectionSupplier) connectionSupplier).getPrimaryCatalog()
        def schemaName =  ((ConnectionSupplier) connectionSupplier).getPrimarySchema()

        def action = new SnapshotDatabaseObjectsAction(Column, new Column().setRelation(new Table().setName(tableName).setSchema(catalogName, schemaName)).setName(columnName))
        def scope = JUnitScope.getInstance(database)

        def plan = new ActionExecutor().createPlan(action, scope)

        TestMD.test(this.class, "case sensitive table and column combination in primary catalog and schema__${database.shortName}", database.class).permutation([database: database.class.name, catalogName: catalogName, schemaName: schemaName, tableName: tableName, columnName: columnName])
        .asTable("catalogName", "schemaName", "tableName", "columnName")
                .addResult("actions", plan.describe())
                .setup({
        if (!database.canStoreObjectName(tableName, false, Table) || !database.canStoreObjectName(columnName, false, Column)) {
            throw new SetupResult.Skip("Case Insensitive Database")
        }

        setupDatabase(connectionSupplier, scope)
        }).cleanup({cleanupDatabase(connectionSupplier, scope)})
                .run({
        if (database instanceof UnsupportedDatabase) {
            return;
        }
        QueryResult result = plan.execute(scope)

        assert result.asList(Column).size() == 1
        assert result.asObject(Object) instanceof Column
        assert result.asObject(Column).getName() == columnName
        })

        where:
        [connectionSupplier, tableName, columnName] << CollectionUtil.permutations([
                JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers,
                testTables,
                testColumns])
    }

}
