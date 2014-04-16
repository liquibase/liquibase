package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.DatabaseFunction
import liquibase.statement.ForeignKeyConstraint
import liquibase.statement.SequenceNextValueFunction
import liquibase.statement.core.CreateTableStatement
import liquibase.structure.core.Table
import spock.lang.Unroll

import static org.junit.Assert.fail

public class CreateTableChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new CreateTableChange()
        change.setTableName("TAB_NAME")

        then:
        change.getConfirmationMessage() == "Table TAB_NAME created"
    }

    @Unroll("statements have correct default values after #method(#defaultValue)")
    def "statements have correct default values"() throws Exception {
        when:
        def change = new CreateTableChange()
        def columnConfig = new ColumnConfig()
        columnConfig.setName("id")
        columnConfig.setType("int")
        change.addColumn(columnConfig)
        if (method == "defaultValue") {
            columnConfig.setDefaultValue(defaultValue)
        } else if (method == "defaultValueBoolean") {
            columnConfig.setDefaultValueBoolean(defaultValue)
        } else if (method == "defaultValueNumeric") {
            columnConfig.setDefaultValueNumeric(defaultValue)
        } else if (method == "defaultValueDate") {
            columnConfig.setDefaultValueDate(defaultValue)
        } else if (method == "defaultValueComputed") {
            columnConfig.setDefaultValueComputed(defaultValue)
        } else if (method == "defaultValueSequenceNext") {
            columnConfig.setDefaultValueSequenceNext(defaultValue)
        } else {
            fail "Unknown method ${method}"
        }

        then:
        def statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0]
        if (expectedValue == "SAME") {
            assert statement.getDefaultValue("id") == defaultValue
        } else {
            assert statement.getDefaultValue("id") == expectedValue
        }

        where:
        defaultValue | method | expectedValue
        null                                      | "defaultValue"             | "SAME"
        "DEFAULTVALUE"                            | "defaultValue"             | "SAME"
        Boolean.TRUE                              | "defaultValueBoolean"      | "SAME"
        Boolean.FALSE                             | "defaultValueBoolean"      | "SAME"
        "true"                                    | "defaultValueBoolean"      | Boolean.TRUE
        "false"                                   | "defaultValueBoolean"      | Boolean.FALSE
        42L                                       | "defaultValueNumeric"      | "SAME"
        15.23                                     | "defaultValueNumeric"      | 15.23
        "52"                                      | "defaultValueNumeric"      | 52L
        "382.3131"                                | "defaultValueNumeric"      | 382.3131
        "2007-01-02"                              | "defaultValueDate"         | new java.sql.Date(107, 0, 2)
        "2008-03-04T13:14:15"                     | "defaultValueDate"         | new java.sql.Timestamp(108, 2, 4, 13, 14, 15, 0)
        new DatabaseFunction("NOW()")             | "defaultValueComputed"     | "SAME"
        new SequenceNextValueFunction("seq_name") | "defaultValueSequenceNext" | "SAME"
    }


    def createInverse() {
        when:
        def change = new CreateTableChange()
        change.setTableName("TestTable")

        then:
        def inverses = change.createInverses()
        inverses.length == 1
        assert inverses[0] instanceof DropTableChange
        ((DropTableChange) inverses[0]).tableName == "TestTable"
    }

    def "tablespace defaults to null"() {
        when:
        def change = new CreateTableChange()

        then:
        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0]
        statement.getTablespace() == null
    }

    def "tablespace can be set"() throws Exception {
        when:
        def change = new CreateTableChange()
        change.setTablespace(tablespace)

        then:
        def statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0]
        statement.getTablespace() == tablespace

        where:
        tablespace << [null, "TESTTABLESPACE"]
    }

    def "foreign key deferrability sets correctly"() {
        when:
        def change = new CreateTableChange()
        ColumnConfig columnConfig = new ColumnConfig()
        columnConfig.setName("id")
        columnConfig.setType("int")
        ConstraintsConfig constraints = new ConstraintsConfig()
        constraints.setForeignKeyName("fk_test")
        constraints.setReferences("test(id)")
        constraints.setDeferrable(deferrable)
        constraints.setInitiallyDeferred(initiallyDeferred)
        columnConfig.setConstraints(constraints)
        change.addColumn(columnConfig)

        then:
        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0]
        ForeignKeyConstraint keyConstraint = statement.getForeignKeyConstraints().iterator().next()
        keyConstraint.isDeferrable() == deferrable
        keyConstraint.isInitiallyDeferred() == initiallyDeferred

        where:
        deferrable | initiallyDeferred
        true | true
        false | true
        true | false
        false | false
    }

    def "foreign keys default not deferrable"() throws Exception {
        when:
        def change = new CreateTableChange()
        ColumnConfig columnConfig = new ColumnConfig()
        columnConfig.setName("id")
        columnConfig.setType("int")
        ConstraintsConfig constraints = new ConstraintsConfig()
        constraints.setReferences("test(id)")
        constraints.setForeignKeyName("fk_test")
        columnConfig.setConstraints(constraints)
        change.addColumn(columnConfig)

        then:
        def statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0]
        ForeignKeyConstraint keyConstraint = statement.getForeignKeyConstraints().iterator().next()
        assert !keyConstraint.isDeferrable()
        assert !keyConstraint.isInitiallyDeferred()
    }

    def "checkStatus"() {
        when:
        def change = new CreateTableChange()
        change.tableName = "test_table"
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        then: "when no tables"
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "another table exists but not the target table"
        snapshotFactory.addObjects(new Table(null, null, "other_table"))
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "expected table exists"
        snapshotFactory.addObjects(new Table(null, null, "test_table"))
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

    }
}