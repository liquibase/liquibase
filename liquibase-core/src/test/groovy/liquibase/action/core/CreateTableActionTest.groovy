package liquibase.action.core

import liquibase.JUnitScope
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.SnapshotFactory
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.structure.core.Relation
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
        new CreateTableAction(new ObjectName("cat", "schem", "tab")).describe() == "createTable(tableName=cat.schem.tab)"
    }

    @Unroll("#featureName #tableName.#columnName")
    def "create simple table"() {
        expect:
        def action = new CreateTableAction(tableName).addColumn(columnName, "int")

        def scope = JUnitScope.getInstance(conn.getDatabase())
        def plan = new ActionExecutor().createPlan(action, scope)

        TestMD.test(this.class, conn.databaseShortName, conn.getDatabase().class)
                .withPermutation([connection: conn, tableName_asTable: tableName.toString(), columnName_asTable: columnName.toString()])
                .addOperations(plan: plan)
                .setup({
            conn.connect(scope)
            throw SetupResult.OK
        })
                .cleanup({
            new ActionExecutor().execute(new DropTableAction(tableName as ObjectName), scope)
        })
                .run({
            plan.execute(scope)
            assert scope.getSingleton(SnapshotFactory.class).has(new Table(tableName), scope)
            assert scope.getSingleton(SnapshotFactory.class).has(new Column(columnName).setRelation(new Table(tableName)), scope)

        })

        where:
        [conn, tableName, columnName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            return CollectionUtil.permutations([
                    [it],
                    it.getReferenceObjectNames(Table.class, false, false),
                    it.getReferenceObjectNames(Column.class, false, false),
            ])
        }
    }
}
