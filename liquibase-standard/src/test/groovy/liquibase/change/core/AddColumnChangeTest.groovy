package liquibase.change.core

import liquibase.change.AddColumnConfig
import liquibase.change.Change
import liquibase.change.ChangeStatus
import liquibase.change.ConstraintsConfig
import liquibase.change.StandardChangeTest
import liquibase.change.visitor.ChangeVisitorFactory
import liquibase.database.core.FirebirdDatabase
import liquibase.database.core.H2Database
import liquibase.database.core.HsqlDatabase
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MariaDBDatabase
import liquibase.database.core.MockDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.datatype.DataTypeFactory
import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import spock.lang.Unroll

class AddColumnChangeTest extends StandardChangeTest {


    @Unroll
    def "valid setups are allowed"() {
        when:
        def change = new AddColumnChange()
        change.setTableName("test_table")
        change.addColumn(columnConfig)

        then:
        !change.validate(new H2Database()).hasErrors()

        where:
        columnConfig << [
                new AddColumnConfig()
                        .setType("int")
                        .setName("test_col"),
                new AddColumnConfig()
                        .setName("payload_id AS JSON_VALUE(payload,'\$.id')")
                        .setComputed(true)
        ]
    }

    def "computed columns generate expected sql"() {
        when:
        def db = new MSSQLDatabase()

        def change = new AddColumnChange()
        change.setTableName("test_table")
        change.addColumn(new AddColumnConfig()
                .setName("payload_id AS JSON_VALUE(payload,'\$.id')")
                .setComputed(true))

        def statements = change.generateStatements(db)

        then:
        SqlGeneratorFactory.getInstance().generateSql(statements, db)*.toString() == ["ALTER TABLE test_table ADD payload_id AS JSON_VALUE(payload,'\$.id');"]

    }

    def "add and remove column methods"() throws Exception {
        when:
        def columnA = new AddColumnConfig();
        columnA.setName("a");

        def columnB = new AddColumnConfig();
        columnB.setName("b");

        def change = new AddColumnChange();

        then:
        change.getColumns().size() == 0

        change.removeColumn(columnA);
        change.getColumns().size() == 0

        change.addColumn(columnA);
        change.getColumns().size() == 1

        change.removeColumn(columnB);
        change.getColumns().size() == 1

        change.removeColumn(columnA);
        change.getColumns().size() == 0
    }

    def getConfirmationMessage() throws Exception {
        when:
        def refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.addColumn(column);

        then:
        refactoring.getConfirmationMessage() == "Columns NEWCOL(TYP) added to TAB"
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col")
        def testColumnConfig = new AddColumnConfig()
        testColumnConfig.type = "varchar(5)"
        testColumnConfig.name = testColumn.name

        def testColumn2 = new Column(Table.class, null, null, table.name, "test_col2")
        def testColumnConfig2 = new AddColumnConfig()
        testColumnConfig2.type = "varchar(50)"
        testColumnConfig2.name = testColumn2.name


        table.getColumns().add(new Column(Table.class, null, null, table.name, "other_col"))
        table.getColumns().add(new Column(Table.class, null, null, table.name, "another_col"))

        def change = new AddColumnChange()
        change.tableName = table.name
        change.addColumn(testColumnConfig)

        then: "table is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "Table exists but not column"
        snapshotFactory.addObjects(table)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "Column 1 is added"
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(testColumn)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "Change expects two columns"
        change.addColumn(testColumnConfig2)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.incorrect

        when: "Column 2 is added"
        table.getColumns().add(testColumn2)
        snapshotFactory.addObjects(testColumn2)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }

    def "load method works"() {
        when:
        def node = new ParsedNode(null, "addColumn")
                .addChildren([tableName: "table_name"])
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_1", type: "int", beforeColumn: "before_col"]))
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_2", type: "int", position: "3"]))
        def change = new AddColumnChange()
        try {
            change.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        change.tableName == "table_name"
        change.columns.size() == 2
        change.columns[0].name == "col_1"
        change.columns[0].type == "int"
        change.columns[0].beforeColumn == "before_col"

        change.columns[1].name == "col_2"
        change.columns[1].type == "int"
        change.columns[1].position == 3
    }

    def "with AfterColumn and removeChangeSetProperty the column is added at the last position"() {

        given:
        def db = new PostgresDatabase()

        def change = new AddColumnChange()
        change.setTableName("test_table")
        def newColumn = new AddColumnConfig()
        newColumn.setName("new_col")
        newColumn.setAfterColumn("id")
        change.addColumn(newColumn)

        when:
        def statements = change.generateStatements(db)

        then:
        SqlGeneratorFactory.getInstance().generateSql(statements, db)*.toString() == ["ALTER TABLE test_table ADD new_col AFTER id;"]

        when:
        def removeChangeSetPropertyNode = new ParsedNode(null, "removeChangeSetProperty")
                .addChildren([change: "addColumn", dbms: "postgres", remove: "afterColumn"])
        def changeVisitor = ChangeVisitorFactory.getInstance().create("addColumn");
        changeVisitor.load(removeChangeSetPropertyNode, resourceSupplier.simpleResourceAccessor)
        change.modify(changeVisitor)

        statements = change.generateStatements(db)

        then:
        SqlGeneratorFactory.getInstance().generateSql(statements, db)*.toString() == ["ALTER TABLE test_table ADD new_col;"]

    }

    @Unroll
    def "modify method works for removing afterColumn"() {
        when:
        def removeChangeSetPropertyNode = new ParsedNode(null, "removeChangeSetProperty")
                .addChildren([change: "addColumn", dbms: dbms, remove: "afterColumn"])
        def changeVisitor = ChangeVisitorFactory.getInstance().create("addColumn");
        changeVisitor.load(removeChangeSetPropertyNode, resourceSupplier.simpleResourceAccessor)
        def node = new ParsedNode(null, "addColumn")
                .addChildren([tableName: "table_name"])
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_1", type: "int", beforeColumn: "before_col"]))
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_2", type: "int", afterColumn: after_column]))
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_3", type: "int", position: "3"]))
        def change = new AddColumnChange()

        try {
            change.load(node, resourceSupplier.simpleResourceAccessor)
            change.modify(changeVisitor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        change.tableName == "table_name"
        change.columns.size() == 3

        change.columns[0].name == "col_1"
        change.columns[0].type == "int"
        change.columns[0].beforeColumn == "before_col"

        change.columns[1].name == "col_2"
        change.columns[1].type == "int"
        change.columns[1].afterColumn == expected

        change.columns[2].name == "col_3"
        change.columns[2].type == "int"
        change.columns[2].position == 3

        where:
        dbms                              | after_column  || expected
        "postgres"                        | "after_col"   || null
        "postgres,any_unsupported_db"     | "after_col"   || null
    }

    @Unroll
    def "modify method works for removing beforeColumn"() {
        when:
        def removeChangeSetPropertyNode = new ParsedNode(null, "removeChangeSetProperty")
                .addChildren([change: "addColumn", dbms: dbms, remove: "beforeColumn"])
        def changeVisitor = ChangeVisitorFactory.getInstance().create("addColumn");
        changeVisitor.load(removeChangeSetPropertyNode, resourceSupplier.simpleResourceAccessor)
        def node = new ParsedNode(null, "addColumn")
                .addChildren([tableName: "table_name"])
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_1", type: "int", beforeColumn: before_column]))
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_2", type: "int", afterColumn: "after_col"]))
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_3", type: "int", position: "3"]))
        def change = new AddColumnChange()

        try {
            change.load(node, resourceSupplier.simpleResourceAccessor)
            change.modify(changeVisitor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        change.tableName == "table_name"
        change.columns.size() == 3

        change.columns[0].name == "col_1"
        change.columns[0].type == "int"
        change.columns[0].beforeColumn == expected

        change.columns[1].name == "col_2"
        change.columns[1].type == "int"
        change.columns[1].afterColumn == "after_col"

        change.columns[2].name == "col_3"
        change.columns[2].type == "int"
        change.columns[2].position == 3

        where:
        dbms                              | before_column  || expected
        "postgres"                        | "before_col"   || null
        "postgres,any_unsupported_db"     | "before_col"   || null
    }

    @Unroll
    def "modify method works for removing position"() {
        when:
        def removeChangeSetPropertyNode = new ParsedNode(null, "removeChangeSetProperty")
                .addChildren([change: "addColumn", dbms: dbms, remove: "position"])
        def changeVisitor = ChangeVisitorFactory.getInstance().create("addColumn");
        changeVisitor.load(removeChangeSetPropertyNode, resourceSupplier.simpleResourceAccessor)
        def node = new ParsedNode(null, "addColumn")
                .addChildren([tableName: "table_name"])
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_1", type: "int", beforeColumn: "before_col"]))
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_2", type: "int", afterColumn: "after_col"]))
                .addChild(new ParsedNode(null, "column").addChildren([name: "col_3", type: "int", position: "3"]))
        def change = new AddColumnChange()

        try {
            change.load(node, resourceSupplier.simpleResourceAccessor)
            change.modify(changeVisitor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        change.tableName == "table_name"
        change.columns.size() == 3

        change.columns[0].name == "col_1"
        change.columns[0].type == "int"
        change.columns[0].beforeColumn == "before_col"

        change.columns[1].name == "col_2"
        change.columns[1].type == "int"
        change.columns[1].afterColumn == "after_col"

        change.columns[2].name == "col_3"
        change.columns[2].type == "int"
        change.columns[2].position == expected

        where:
        dbms                              | position    || expected
        "postgres"                        | "3"         || null
        "postgres,any_unsupported_db"     | "3"         || null
    }

    protected void addColumnsToSnapshot(Table table, Change change, MockSnapshotGeneratorFactory snapshotFactory) {
        for (columnDef in ((AddColumnChange) change).getColumns()) {
            def column = new Column(Table.class, table.schema.catalogName, table.schema.name, table.name, columnDef.name)
            table.getColumns().add(column)
            snapshotFactory.addObjects(column)

            if (columnDef.constraints != null && columnDef.constraints.isPrimaryKey()) {
                def pk = new PrimaryKey(null, table.schema.catalogName, table.schema.name, table.name, new Column(column.name))
                snapshotFactory.addObjects(pk)
                table.setPrimaryKey(pk)
            }

            if (columnDef.isAutoIncrement()) {
                def autoIncrementInfo

                if (columnDef.getStartWith() != null || columnDef.getIncrementBy() != null) {
                    autoIncrementInfo = new Column.AutoIncrementInformation(columnDef.startWith, columnDef.incrementBy)
                } else {
                    autoIncrementInfo = new Column.AutoIncrementInformation()
                }
                column.setAutoIncrementInformation(autoIncrementInfo)
            }
        }
    }

    @Unroll
    def "column with delete cascade generates the expected sql for #database database"() {
        when:
        def change = new AddColumnChange()
        def liquibaseType = DataTypeFactory.getInstance().fromDescription("INT", database)
        def databaseType = liquibaseType.toDatabaseDataType(database)
        change.setTableName("test_table")

        def constraintsConfig = new ConstraintsConfig()
        constraintsConfig.setForeignKeyName("test_fk")
        constraintsConfig.setReferencedColumnNames("ref_col")
        constraintsConfig.setReferencedTableName("ref_table")
        constraintsConfig.setDeleteCascade(true)
        change.addColumn(new AddColumnConfig()
                .setName("test_column")
                .setType('INT')
                .setConstraints(constraintsConfig))

        def statements = change.generateStatements(database)

        then:
        SqlGeneratorFactory.getInstance().generateSql(statements, database)*.toString() == [String.format("ALTER TABLE test_table ADD test_column %s%s;", databaseType, columnNull), "ALTER TABLE test_table ADD CONSTRAINT test_fk FOREIGN KEY (test_column) REFERENCES ref_table (ref_col) ON DELETE CASCADE;"]

        where:
        database                | columnNull
        new PostgresDatabase()  | ""
        new OracleDatabase()    | ""
        new MySQLDatabase()     | " NULL"
        new MSSQLDatabase()     | ""
        new H2Database()        | ""
        new MariaDBDatabase()   | " NULL"
        new FirebirdDatabase()  | ""
        new HsqlDatabase()      | ""
    }
}
