package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.ExecuteSqlAction
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplierFactory
import spock.lang.Specification
import spock.lang.Unroll
import testmd.TestMD
import testmd.logic.SetupResult

class CreateTableActionTest extends Specification {

    def "empty constructor"() {
        expect:
        new CreateTableAction().describe() == "createTable()"
    }

    def "parametrized constructor"() {
        expect:
        new CreateTableAction("cat", "schem", "tab").describe() == "createTable(catalogName=cat, schemaName=schem, tableName=tab)"
    }

    @Unroll
    def "create simple table in different schemas"() {
        expect:
        for (def conn : JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers) {
            if (catalogName == "_primary") catalogName = conn.primaryCatalog
            if (schemaName == "_primary") schemaName = conn.primarySchema
            if (catalogName == "_alt") catalogName = conn.alternateCatalog
            if (schemaName == "_alt") schemaName = conn.alternateSchema

            def action = new CreateTableAction(catalogName, schemaName, tableName)
                    .addColumn(new ColumnDefinition("id", "int"))

            def scope = JUnitScope.getInstance(conn.getDatabase())
            def plan = new ActionExecutor().createPlan(action, scope)

            TestMD.test(this.class, "create simple table ${conn.databaseShortName}", conn.getDatabase().class)
                    .permutation([connection: conn, catalogName: catalogName, schemaName: schemaName, tableName: tableName])
                    .asTable("catalogName", "schemaName", "tableName")
                    .addResult("plan", plan)
                    .forceRun()
                    .setup({
                conn.connect(scope)
                throw SetupResult.OK
            })
                    .cleanup({
                new ActionExecutor().execute(new DropTableAction(catalogName, schemaName, tableName), scope)
            })
                    .run({
                plan.execute(scope)
            })

        }


        where:
        catalogName | schemaName | tableName
        null        | null       | "test_table"
        null        | "_primary" | "test_table"
        "_primary"  | "_primary" | "test_table"
        null        | "_alt"     | "test_table"
        "_primary"  | "_alt"     | "test_table"
        "_alt"      | "_alt"     | "test_table"
    }
}
