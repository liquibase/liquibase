package liquibase.change.core

import liquibase.change.AddColumnConfig
import liquibase.change.Change
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Column
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import spock.lang.Unroll

public class AddColumnChangeTest extends StandardChangeTest {


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

    def "verifyUpdate"() {
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
        assert !change.verifyExecuted(database).verifiedPassed

        when: "Table exists but not column"
        snapshotFactory.addObjects(table)
        then:
        assert !change.verifyExecuted(database).verifiedPassed

        when: "Column 1 is added"
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(testColumn)
        then:
        assert change.verifyExecuted(database).verifiedPassed

        when: "Change expects two columns"
        change.addColumn(testColumnConfig2)
        then:
        assert !change.verifyExecuted(database).verifiedPassed

        when: "Column 2 is added"
        table.getColumns().add(testColumn2)
        snapshotFactory.addObjects(testColumn2)
        then:
        assert change.verifyExecuted(database).verifiedPassed
    }

    @Unroll
    def "verifyUpdate with default value"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(((AddColumnChange) change).getCatalogName(), ((AddColumnChange) change).getSchemaName(), ((AddColumnChange) change).getTableName())
        snapshotFactory.addObjects(table)

        then:
        assert !change.verifyExecuted(database).verifiedPassed

        when: "Column is added"
        addColumnsToSnapshot(table, change, snapshotFactory)

        then:
        assert change.verifyExecuted(database).verifiedPassed

        where:
        change << changeSupplier
                .getSupplier(AddColumnChange.class).getAllParameterPermutations(new MockDatabase())
                .findAll({changeSupplier.isValid(it, new MockDatabase())})
                .findAll({ ((AddColumnChange) it).getColumns().findAll({it.defaultValueObject != null}).size() > 0 })
    }

    @Unroll
    def "verifyUpdate with auto-increment"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(((AddColumnChange) change).getCatalogName(), ((AddColumnChange) change).getSchemaName(), ((AddColumnChange) change).getTableName())
        snapshotFactory.addObjects(table)

        then:
        assert !change.verifyExecuted(database).verifiedPassed

        when: "Column is added"
        addColumnsToSnapshot(table, change, snapshotFactory)

        then:
        assert change.verifyExecuted(database).verifiedPassed

        where:
        change << changeSupplier
                .getSupplier(AddColumnChange.class).getAllParameterPermutations(new MockDatabase())
                .findAll({changeSupplier.isValid(it, new MockDatabase())})
                .findAll({ ((AddColumnChange) it).getColumns().findAll({it.isAutoIncrement() }).size() > 0})
    }

    @Unroll
    def "verifyUpdate with primary key"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        then: "table is not there yet"
        assert !change.verifyExecuted(database).verifiedPassed

        when: "Table exists but not column"
        def table = new Table(((AddColumnChange) change).getCatalogName(), ((AddColumnChange) change).getSchemaName(), ((AddColumnChange) change).getTableName())
        snapshotFactory.addObjects(table)
        then:
        assert !change.verifyExecuted(database).verifiedPassed

        when: "Column is added"
        addColumnsToSnapshot(table, change, snapshotFactory)

        then:
        assert change.verifyExecuted(database).verifiedPassed

        where:
        change << changeSupplier
                .getSupplier(AddColumnChange.class).getAllParameterPermutations(new MockDatabase())
                .findAll({changeSupplier.isValid(it, new MockDatabase())})
                .findAll({ ((AddColumnChange) it).getColumns().findAll({it.getConstraints() != null && it.getConstraints().isPrimaryKey()}).size() > 0 })
    }

    protected void addColumnsToSnapshot(Table table, Change change, MockSnapshotGeneratorFactory snapshotFactory) {
        for (columnDef in ((AddColumnChange) change).getColumns()) {
            def column = new Column(Table.class, table.schema.catalogName, table.schema.name, table.name, columnDef.name)
            table.getColumns().add(column)
            snapshotFactory.addObjects(column)

            if (columnDef.constraints != null && columnDef.constraints.isPrimaryKey()) {
                snapshotFactory.addObjects(new PrimaryKey(null, table.schema.catalogName, table.schema.name, table.name, column.name))
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

    def "verifyNotExecuted"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def table = new Table(null, null, "test_table")
        def testColumn = new Column(Table.class, null, null, table.name, "test_col")
        table.getColumns().add(new Column(Table.class, null, null, table.name, "other_col"))

        def change = new AddColumnChange()
        change.tableName = table.name
        change.addColumn(new AddColumnConfig().setName(testColumn.name))

        then: "column is not there"
        assert change.verifyNotExecuted(database).verifiedPassed

        when: "Column is there"
        table.getColumns().add(testColumn)
        snapshotFactory.addObjects(testColumn)
        then:
        assert change.verifyNotExecuted(database).verifiedFailed
    }

}
