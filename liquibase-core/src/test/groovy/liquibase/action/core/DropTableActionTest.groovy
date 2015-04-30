package liquibase.action.core

import liquibase.JUnitScope
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplierFactory
import spock.lang.Specification
import spock.lang.Unroll
import testmd.TestMD
import testmd.logic.SetupResult

class DropTableActionTest extends Specification {
    def "empty constructor"() {
        expect:
        new DropTableAction().describe() == "dropTable()"
    }

    def "parametrized constructor"() {
        expect:
        new DropTableAction("cat", "schem", "tab").describe() == "dropTable(catalogName=cat, schemaName=schem, tableName=tab)"
    }

    @Unroll
    def "drop table in different schemas"() {
        expect:
        for (def conn : JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers) {
            if (catalogName == "_primary") catalogName = conn.primaryCatalog
            if (schemaName == "_primary") schemaName = conn.primarySchema
            if (catalogName == "_alt") catalogName = conn.alternateCatalog
            if (schemaName == "_alt") schemaName = conn.alternateSchema

            def action = new DropTableAction(catalogName, schemaName, tableName)


            def scope = JUnitScope.getInstance(conn.getDatabase())
            def plan = new ActionExecutor().createPlan(action, scope)

            TestMD.test(this.class, "create simple table ${conn.databaseShortName}", conn.getDatabase().class)
                    .withPermutation([connection: conn, catalogName_asTable: catalogName, schemaName_asTable: schemaName, tableName_asTable: tableName])
                    .addOperations(plan: plan)
                    .setup({
                conn.connect(scope)
                new ActionExecutor().execute(new CreateTableAction(catalogName, schemaName, tableName).addColumn("id", "int"), scope)
                throw SetupResult.OK
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
