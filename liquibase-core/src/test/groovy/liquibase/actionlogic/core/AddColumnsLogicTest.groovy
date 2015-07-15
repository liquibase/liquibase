package liquibase.actionlogic.core;

import liquibase.JUnitScope;
import liquibase.action.core.AddColumnsAction
import liquibase.actionlogic.ActionExecutor;
import liquibase.snapshot.MockSnapshotFactory;
import liquibase.snapshot.SnapshotFactory;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddColumnStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table
import spock.lang.Specification;
import spock.lang.Unroll;

public class AddColumnsLogicTest extends Specification {

    @Unroll
    def "checkStatus with a single simple column"() {
        when:
        def tableName = new ObjectName("testTable")

        def column = new Column(new ObjectName(tableName, "columnName"), "int")
        def table = new Table(tableName)
        def snapshotFactory = new MockSnapshotFactory(column, table)
        def scope = JUnitScope.instance.overrideSingleton(SnapshotFactory, snapshotFactory)

        def appliedAction = new AddColumnsAction()
        appliedAction.columns = [new Column(new ObjectName(tableName, "columnName"), "int")]

        def notAppliedAction = new AddColumnsAction()
        notAppliedAction.columns = [new Column(new ObjectName(tableName, "otherColumnName"), "int")]

        def logic = scope.getSingleton(ActionExecutor)

        then:
        assert logic.checkStatus(appliedAction, scope).applied
        logic.checkStatus(notAppliedAction, scope).toString() == "Not Applied: Column 'testTable.otherColumnName' not found"


        when: "remarks are correctly checked"
        appliedAction.columns[0].remarks = "Some remarks"
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Incorrect: 'remarks' is incorrect (expected 'Some remarks' got 'null')"
        when:
        column.remarks = "Some remarks"
        then:
        assert logic.checkStatus(appliedAction, scope).applied


        when: "defaultValues are correctly checked"
        appliedAction.columns[0].defaultValue = "default value string"
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Incorrect: 'defaultValue' is incorrect (expected 'default value string' got 'null')"
        when:
        column.defaultValue = "default value string"
        then:
        assert logic.checkStatus(appliedAction, scope).applied
        when:
        appliedAction.columns[0].defaultValue = 3
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Incorrect: 'defaultValue' is incorrect (expected '3' got 'default value string')"
        when:
        column.defaultValue = 3
        then:
        assert logic.checkStatus(appliedAction, scope).applied


        when: "nullable is correctly checked"
        appliedAction.columns[0].nullable = true
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Incorrect: 'nullable' is incorrect (expected 'true' got 'null')"
        when:
        appliedAction.columns[0].nullable = false
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Incorrect: 'nullable' is incorrect (expected 'false' got 'null')"
        when:
        column.nullable = true
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Incorrect: 'nullable' is incorrect (expected 'false' got 'true')"
        when:
        column.nullable = false
        then:
        assert logic.checkStatus(appliedAction, scope).applied


        when: "primaryKey is correctly checked"
        appliedAction.primaryKey = new PrimaryKey()
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Not Applied: No primary key on 'testTable'"
        when:
        def pk = new PrimaryKey(new ObjectName(tableName, "PK_TEST"), "other_pk_column")
        snapshotFactory.add(pk)
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Incorrect: Column 'testTable.columnName' is not part of the primary key"
        when:
        pk.columns.add(column.name)
        then:
        assert logic.checkStatus(appliedAction, scope).applied

        when: "foreign keys are correctly checked"
        appliedAction.foreignKeys.add(new ForeignKey(null, [column.name], [new ObjectName("otherTable", "otherColumn")]))
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Not Applied: Foreign Key not created on 'testTable'"
        when:
        snapshotFactory.add(new ForeignKey(new ObjectName(tableName, "FK_TEST"), [new ObjectName(tableName, "other_pk_column")], [new ObjectName("otherTable", "other_pk_column")]))
        then:
        logic.checkStatus(appliedAction, scope).toString() == "Not Applied: Foreign Key not created on 'testTable'"
        when:
        snapshotFactory.add(new ForeignKey(new ObjectName(tableName, "FK_TEST"), [column.name], [new ObjectName("otherTable", "otherColumn")]))
        then:
        assert logic.checkStatus(appliedAction, scope).applied
    }

    @Unroll
    def "checkStatus with a multiple columns"() {
        when:
        def tableName = new ObjectName("testTable")

        def column1 = new Column(new ObjectName(tableName, "column1"))
        def column2 = new Column(new ObjectName(tableName, "column2"))
        def scope = JUnitScope.instance.overrideSingleton(SnapshotFactory, new MockSnapshotFactory(column1, column2))

        def appliedAction = new AddColumnsAction()
        appliedAction.columns = [new Column(new ObjectName(tableName, "column1"), "int"), new Column(new ObjectName(tableName, "column2"), "int")]

        def notAppliedAction = new AddColumnsAction()
        notAppliedAction.columns = [new Column(new ObjectName(tableName, "column1"), "int"), new Column(new ObjectName(tableName, "columnX"), "int")]

        def logic = scope.getSingleton(ActionExecutor)

        then:
        assert logic.checkStatus(appliedAction, scope).applied
        logic.checkStatus(notAppliedAction, scope).toString() == "Not Applied: Column 'testTable.columnX' not found"
    }

}
