package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.DatabaseFunction
import liquibase.statement.ForeignKeyConstraint
import liquibase.statement.SequenceNextValueFunction
import liquibase.statement.core.CreateTableStatement
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.PrimaryKey
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
        defaultValue                              | method                     | expectedValue
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
        true       | true
        false      | true
        true       | false
        false      | false
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
        def table = new Table(null, null, "test_table")
        def column1 = new Column(Table.class, table.schema.catalogName, table.schema.name, table.name, "column_1").setType(new DataType("int")).setRelation(table)
        def column2 = new Column(Table.class, table.schema.catalogName, table.schema.name, table.name, "column_2").setType(new DataType("boolean")).setRelation(table)
        def column3 = new Column(Table.class, table.schema.catalogName, table.schema.name, table.name, "column_3").setType(new DataType("varchar(10)")).setRelation(table)

        def change = new CreateTableChange()
        change.tableName = table.name
        change.addColumn(new ColumnConfig(column1).setType(column1.type.toString()))
        change.addColumn(new ColumnConfig(column2).setType(column2.type.toString()))
        change.addColumn(new ColumnConfig(column3).setType(column3.type.toString()))

        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        then: "when no tables"
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "another table exists but not the target table"
        snapshotFactory.addObjects(new Table(null, null, "other_table"))
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "expected table exists but is missing columns"
        table.getColumns().add(column1)
        table.getColumns().add(column2)
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.incorrect

        when: "all columns exist"
        table.getColumns().add(column3)
        snapshotFactory.addObjects(column3)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "column is supposed to be primary key but table is not"
        change.getColumns().get(0).setConstraints(new ConstraintsConfig().setPrimaryKey(true))
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.incorrect

        when: "table has primary key as expected"
        def pk = new PrimaryKey("pk_test", table.schema.catalogName, table.schema.name, table.name, new Column(column1.name))
        table.setPrimaryKey(pk)
        snapshotFactory.addObjects(pk)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "nullability is different"
        change.getColumns().get(1).setConstraints(new ConstraintsConfig().setNullable(false))
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.incorrect

        when: "column nullability matches"
        table.getColumns().get(1).nullable = false
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }

    def "load can take nested 'column' nodes, not just 'columns' nodes"() {
        when:
        def node = new ParsedNode(null, "createTable").addChildren([tableName: "table_name"])
                .addChildren([column: [name: "column1", type: "type1"]])
                .addChildren([column: [name: "column2", type: "type2"]])
        def change = new CreateTableChange()
        try {
            change.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.tableName == "table_name"
        change.columns.size() == 2
        change.columns[0].name == "column1"
        change.columns[0].type == "type1"

        change.columns[1].name == "column2"
        change.columns[1].type == "type2"
    }

    def "load can take a nested 'columns' collection nodes"() {
        when:
        def node = new ParsedNode(null, "createTable").addChildren([tableName: "table_name"])
                .addChild(null, "columns", [[column: [name: "column1", type: "type1"]], [column: [name: "column2", type: "type2"]]])
        def change = new CreateTableChange()
        try {
            change.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.tableName == "table_name"
        change.columns.size() == 2
        change.columns[0].name == "column1"
        change.columns[0].type == "type1"

        change.columns[1].name == "column2"
        change.columns[1].type == "type2"
    }

}