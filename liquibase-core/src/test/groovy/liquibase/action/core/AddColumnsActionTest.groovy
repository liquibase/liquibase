package liquibase.action.core

import liquibase.JUnitScope
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.TestSnapshotFactory
import liquibase.snapshot.transformer.LimitTransformer
import liquibase.snapshot.transformer.NoOpTransformer
import liquibase.structure.ObjectName
import liquibase.structure.TestColumnSupplier
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import liquibase.util.CollectionUtil
import org.junit.Assert
import org.junit.Assume
import spock.lang.Unroll
class AddColumnsActionTest extends AbstractActionTest {

    @Unroll("#featureName: add #columnName to #table on #conn")
    def "Can apply single column with standard settings but complex names"() {
        when:
        def action = new AddColumnsAction()
        action.columns = [new Column(new ObjectName(table.name, columnName), "int")]

        then:
        def plan = scope.getSingleton(ActionExecutor).createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([
                tableName_asTable: table.name.toString(),
                columnName_asTable: columnName.toString()
        ])
                .addOperations(plan: plan)
                .run({
            plan.execute(scope)

            assert scope.getSingleton(ActionExecutor).checkStatus(action, scope).applied
        })

        where:
        [conn, scope, snapshot, columnName, table] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it).child(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES)
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(NoOpTransformer.instance, scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    new TestColumnSupplier().getObjectNames(scope).collect {it.name+"_2"},
                    snapshot.get(Table)
            ])
        }

    }

    @Unroll("#featureName: add #columnNames to #table on #conn")
    def "Can apply multiple columns with standard settings but complex names"() {
        when:
        def action = new AddColumnsAction()
        action.columns = [new Column(new ObjectName(table.name, columnNames[0]), "int"), new Column(new ObjectName(table.name, columnNames[1]), "int")]
        ActionExecutor executor = scope.getSingleton(ActionExecutor.class)

        then:
        def plan = executor.createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([
                tableName_asTable: table.name.toString(),
                columnNames_asTable: columnNames.toString()
        ])
                .addOperations(plan: plan)
                .run({
            plan.execute(scope)

            assert executor.checkStatus(action, scope).applied
        })

        where:
        [conn, scope, snapshot, columnNames, table] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it).child(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES)
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(NoOpTransformer.instance, scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    new TestColumnSupplier().getObjectNames(scope).collect {[it.name+"_2", it.name+"_3"]},
                    snapshot.get(Table)
            ])
        }
    }

    @Unroll("#featureName: add #columnName to #table on #conn")
    def "Can apply single column with various settings"() {
        when:
        def action = new AddColumnsAction()

        if (defaultValue == "WITH_DEFAULT_VALUE") {
            if (dataType == "int") {
                defaultValue = 3
            } else if (dataType == "varchar(10)") {
                defaultValue = "test value"
            } else {
                Assert.fail("Unknown dataType: "+dataType)
            }
        }

        if (primaryKey == "Unnamed PK") {
            primaryKey = new PrimaryKey(new ObjectName(table.name, null), columnName)
        } else if (primaryKey == "Named PK") {
            primaryKey = new PrimaryKey(new ObjectName(table.name, scope.getDatabase().canStoreObjectName("test_pk", false, PrimaryKey) ? "test_pk" : "TEST_PK"), columnName)
        }

        def columnDef = new Column(new ObjectName(table.name, columnName), dataType)
        columnDef.defaultValue = defaultValue
        columnDef.remarks = remarks;
//        columnDef.addAfterColumn;
//        columnDef.addBeforeColumn;
//        columnDef.addAtPosition;
        columnDef.autoIncrementInformation = autoIncrementInformation;
        columnDef.nullable = isNullable;
//        columnDef.constraints;

        action.primaryKey = primaryKey

        action.columns = [columnDef]

        action.foreignKeys = foreignKeys.each {
            it.foreignKeyColumns.each {it.container = table.name } //set columns to be on this table
            it.primaryKeyColumns.each {it.container.container = table.name.container} //set referenced table to be in the same container as this test
        }


        def executor = scope.getSingleton(ActionExecutor.class)
        then:
        def errors = executor.validate(action, scope)
        Assume.assumeFalse(errors.toString(), errors.hasErrors())
        Assume.assumeTrue("Auto-increment only applies to int types", columnDef.autoIncrementInformation == null || dataType.contains("int"))

        def plan = executor.createPlan(action, scope)

        testMDPermutation(snapshot, conn, scope)
                .addParameters([
                tableName_asTable: table.name.toString(),
                columnName_asTable: columnName.toString(),
                dataType_asTable: dataType,
                defaultValue_asTable: defaultValue,
                remarks_asTable: remarks,
                primaryKey_asTable: primaryKey,
                isNullable_asTable: isNullable,
                foreignKeys_asTable: foreignKeys,
                autoIncrementInformation_asTable: autoIncrementInformation,
        ])
                .addOperations(plan: plan)
                .run({
            plan.execute(scope)

            assert executor.checkStatus(action, scope).applied
        })

        where:
        [conn, scope, snapshot, columnName, table, dataType, defaultValue, remarks, primaryKey, isNullable, autoIncrementInformation, foreignKeys] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)
            def snapshot = JUnitScope.instance.getSingleton(TestSnapshotFactory).createSnapshot(new LimitTransformer(2), scope)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    [snapshot],
                    [scope.getDatabase().canStoreObjectName("column_name", false, Column) ? "column_name" : "COLUMN_NAME"],
                    snapshot.get(Table),
                    ["int", "varchar(10)"],
                    [null, "WITH_DEFAULT_VALUE"],
                    [null, "Remarks Here"],
                    [null, "Unnamed PK", "Named PK"],
                    [null, true, false],
                    [null, new Column.AutoIncrementInformation(), new Column.AutoIncrementInformation(2, 3)],
                    [[], [new ForeignKey(null, [new ObjectName("this_col")], [new ObjectName("other_table", "other_col")])]]

            ])
        }

    }
}
