package liquibase.action.core

import liquibase.JUnitScope
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.SnapshotFactory
import liquibase.structure.ObjectReference
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
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
        new CreateTableAction(new Table(new ObjectReference("cat", "schem", "tab"))).describe() == "createTable(tableName=cat.schem.tab)"
    }

    @Unroll("#featureName #tableName with #columnName")
    def "create simple table"() {
        expect:
        def action = new CreateTableAction(new Table(tableName)).addColumn(columnName, "int")

        def scope = JUnitScope.getInstance(conn.getDatabase())
        def plan = new ActionExecutor().createPlan(action, scope)

        TestMD.test(this.specificationContext, conn.getDatabase().class)
                .withPermutation([connection: conn, tableName_asTable: tableName.toString(), columnName_asTable: columnName.toString()])
                .addOperations(plan: plan)
                .setup({
            throw SetupResult.OK
        })
                .cleanup({
            new ActionExecutor().execute(new DropTablesAction(tableName as ObjectReference), scope)
        })
                .run({
            plan.execute(scope)
            assert scope.getSingleton(SnapshotFactory.class).has(new Table(tableName), scope)
            assert scope.getSingleton(SnapshotFactory.class).has(new Column(Table, tableName, columnName.toString()), scope)

        })

        where:
        [conn, tableName, columnName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            return CollectionUtil.permutations([
                    [it],
                    it.getReferenceObjectNames(Table.class),
                    it.getReferenceObjectNames(Column.class),
            ])
        }
    }
}
