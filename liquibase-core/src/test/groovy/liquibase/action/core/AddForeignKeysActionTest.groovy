package liquibase.action.core

import liquibase.JUnitScope
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.snapshot.transformer.NoOpTransformer
import liquibase.structure.ObjectName
import liquibase.structure.TestColumnSupplier
import liquibase.structure.TestForeignKeySupplier
import liquibase.structure.TestTableSupplier
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import org.junit.Assume
import spock.lang.Unroll

class AddForeignKeysActionTest extends AbstractActionTest {

    @Unroll("#featureName: add #fkName on #tableName.#columnName.name to #conn")
    def "Can apply single column with standard settings but complex names"() {
        when:
        def action = new AddForeignKeysAction()

        def pkTable = new Table(new ObjectName(tableName.container, tableName.name + "_2"))
        def fkTable = new Table(tableName)

        def foreignKey = new ForeignKey(fkName, [new ObjectName(fkTable.name, columnName.name)], [new ObjectName(pkTable.name, columnName.name+"_2")])

        action.foreignKeys = [foreignKey]


        def errors = scope.getSingleton(ActionExecutor).validate(action, scope)
        Assume.assumeFalse(errors.toString(), errors.hasErrors())

        then:
        def plan = scope.getSingleton(ActionExecutor).createPlan(action, scope)

        testMDPermutation(conn, scope)
                .addParameters([
                fkName_asTable: fkName.toString(),
                tableName_asTable: tableName.toString(),
                columnName_asTable: columnName.toString()
        ])
                .addOperations(plan: plan)
                .run({

            def createFKTableAction = new CreateTableAction(fkTable)
            createFKTableAction.addColumn(new ObjectName(fkTable.name, "id"), "int")
            createFKTableAction.addColumn(new ObjectName(fkTable.name, columnName.name), "int")
            scope.getSingleton(ActionExecutor.class).execute(createFKTableAction, scope)

            def createPKTableAction = new CreateTableAction(pkTable)
            createPKTableAction.addColumn(new ObjectName(pkTable.name, "id"), "int")
            createPKTableAction.addColumn(new ObjectName(pkTable.name, columnName.name+"_2"), "int")
            createPKTableAction.primaryKey = new PrimaryKey(new ObjectName(pkTable.name, null), createPKTableAction.columns[1].simpleName)
            scope.getSingleton(ActionExecutor.class).execute(createPKTableAction, scope)


            plan.execute(scope)

            assert scope.getSingleton(ActionExecutor).checkStatus(action, scope).applied
        })

        where:
        [conn, scope, columnName, tableName, fkName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it).child(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    new TestColumnSupplier().getObjectNames(scope),
                    new TestTableSupplier().getObjectNames(scope),
                    new TestForeignKeySupplier().getObjectNames(scope),
            ])
        }

    }

}
