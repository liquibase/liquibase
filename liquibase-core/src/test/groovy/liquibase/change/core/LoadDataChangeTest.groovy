package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeSet
import liquibase.database.DatabaseConnection
import liquibase.database.DatabaseFactory
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MockDatabase
import liquibase.exception.ValidationErrors
import liquibase.parser.core.ParsedNodeException
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.ExecutablePreparedStatement
import liquibase.statement.ExecutablePreparedStatementBase
import liquibase.statement.SqlStatement
import liquibase.statement.core.InsertSetStatement
import liquibase.statement.core.InsertStatement
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Table
import liquibase.test.TestContext
import spock.lang.Unroll

import java.sql.Timestamp
import java.time.LocalDateTime

public class LoadDataChangeTest extends StandardChangeTest {

    MSSQLDatabase mssqlDb;
    MockDatabase mockDb;

    def setup() {
        ResourceAccessor resourceAccessor = TestContext.getInstance().getTestResourceAccessor()
        String offlineUrl

        mssqlDb = new MSSQLDatabase();
        mssqlDb.setConnection(DatabaseFactory.getInstance().openConnection("offline:mssql",
                "superuser", "superpass", null, resourceAccessor));

        mockDb = new MockDatabase();
        mockDb.setConnection((DatabaseConnection) null)
    }


    def "loadDataEmpty database agnostic"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/empty.data.csv");
        refactoring.setSeparator(",");

        SqlStatement[] sqlStatement = refactoring.generateStatements(mssqlDb);
        then:
        sqlStatement.length == 0
    }

    def "loadDataEmpty not using InsertSetStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/empty.data.csv");
        refactoring.setSeparator(",");

        SqlStatement[] sqlStatements = refactoring.generateStatements(mockDb);

        then:
        sqlStatements.length == 0
    }


    @Unroll("multiple formats with the same data for #fileName")
    def "multiple formats with the same data using InsertSetStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile(fileName);
        if (separator != null) {
            refactoring.setSeparator(separator);
        }
        if (quotChar != null) {
            refactoring.setQuotchar(quotChar);
        }

        SqlStatement[] sqlStatement = refactoring.generateStatements(new MSSQLDatabase());
        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof InsertSetStatement

        when:
        SqlStatement[] sqlStatements = ((InsertSetStatement) sqlStatement[0]).getStatementsArray();

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof InsertStatement
        assert sqlStatements[1] instanceof InsertStatement

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[0]).getTableName()
        "Bob Johnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("name")
        "bjohnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("username")

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[1]).getTableName()
        "John Doe" == ((InsertStatement) sqlStatements[1]).getColumnValue("name")
        "jdoe" == ((InsertStatement) sqlStatements[1]).getColumnValue("username")

        where:
        fileName                                    | separator | quotChar
        "liquibase/change/core/sample.data1.tsv"    | "\t"      | null
        "liquibase/change/core/sample.quotchar.tsv" | "\t"      | "'"
        "liquibase/change/core/sample.data1.csv"    | ","       | null
        "liquibase/change/core/sample.data1.csv"    | null      | null
    }

    @Unroll("multiple formats with the same data for #fileName")
    def "multiple formats with the same data not using InsertSetStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile(fileName);
        if (separator != null) {
            refactoring.setSeparator(separator);
        }
        if (quotChar != null) {
            refactoring.setQuotchar(quotChar);
        }

        SqlStatement[] sqlStatements = refactoring.generateStatements(mockDb);

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof InsertStatement
        assert sqlStatements[1] instanceof InsertStatement

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[0]).getTableName()
        "Bob Johnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("name")
        "bjohnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("username")

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[1]).getTableName()
        "John Doe" == ((InsertStatement) sqlStatements[1]).getColumnValue("name")
        "jdoe" == ((InsertStatement) sqlStatements[1]).getColumnValue("username")

        where:
        fileName                                    | separator | quotChar
        "liquibase/change/core/sample.data1.tsv"    | "\t"      | null
        "liquibase/change/core/sample.quotchar.tsv" | "\t"      | "'"
        "liquibase/change/core/sample.data1.csv"    | ","       | null
        "liquibase/change/core/sample.data1.csv"    | null      | null
    }

    def "generateStatement_excel using InsertSetStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-excel.csv");

        LoadDataColumnConfig ageConfig = new LoadDataColumnConfig();
        ageConfig.setHeader("age");
        ageConfig.setType("NUMERIC");
        refactoring.addColumn(ageConfig);

        LoadDataColumnConfig activeConfig = new LoadDataColumnConfig();
        activeConfig.setHeader("active");
        activeConfig.setType("BOOLEAN");
        refactoring.addColumn(activeConfig);

        SqlStatement[] sqlStatement = refactoring.generateStatements(new MSSQLDatabase());
        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof InsertSetStatement

        when:
        SqlStatement[] sqlStatements = ((InsertSetStatement) sqlStatement[0]).getStatementsArray();

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof InsertStatement
        assert sqlStatements[1] instanceof InsertStatement

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[0]).getTableName()
        "Bob Johnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("name")
        "bjohnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("username")
        "15" == ((InsertStatement) sqlStatements[0]).getColumnValue("age").toString()
        Boolean.TRUE == ((InsertStatement) sqlStatements[0]).getColumnValue("active")

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[1]).getTableName()
        "John Doe" == ((InsertStatement) sqlStatements[1]).getColumnValue("name")
        "jdoe" == ((InsertStatement) sqlStatements[1]).getColumnValue("username")
        "21" == ((InsertStatement) sqlStatements[1]).getColumnValue("age").toString()
        Boolean.FALSE == ((InsertStatement) sqlStatements[1]).getColumnValue("active")
    }

    def "generateStatement_excel not using InsertStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-excel.csv");

        LoadDataColumnConfig ageConfig = new LoadDataColumnConfig();
        ageConfig.setHeader("age");
        ageConfig.setType("NUMERIC");
        refactoring.addColumn(ageConfig);

        LoadDataColumnConfig activeConfig = new LoadDataColumnConfig();
        activeConfig.setHeader("active");
        activeConfig.setType("BOOLEAN");
        refactoring.addColumn(activeConfig);

        SqlStatement[] sqlStatements = refactoring.generateStatements(mockDb);

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof InsertStatement
        assert sqlStatements[1] instanceof InsertStatement

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[0]).getTableName()
        "Bob Johnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("name")
        "bjohnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("username")
        "15" == ((InsertStatement) sqlStatements[0]).getColumnValue("age").toString()
        Boolean.TRUE == ((InsertStatement) sqlStatements[0]).getColumnValue("active")

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[1]).getTableName()
        "John Doe" == ((InsertStatement) sqlStatements[1]).getColumnValue("name")
        "jdoe" == ((InsertStatement) sqlStatements[1]).getColumnValue("username")
        "21" == ((InsertStatement) sqlStatements[1]).getColumnValue("age").toString()
        Boolean.FALSE == ((InsertStatement) sqlStatements[1]).getColumnValue("active")
    }

    def "generateStatement_uuid not using InsertStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data3.csv");

        LoadDataColumnConfig idConfig = new LoadDataColumnConfig();
        idConfig.setHeader("id");
        idConfig.setType("UUID");
        refactoring.addColumn(idConfig);

        LoadDataColumnConfig parentIdConfig = new LoadDataColumnConfig();
        parentIdConfig.setHeader("parent_id");
        parentIdConfig.setType("UUID");
        refactoring.addColumn(parentIdConfig);

        SqlStatement[] sqlStatements = refactoring.generateStatements(mockDb);

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof InsertStatement
        assert sqlStatements[1] instanceof InsertStatement
        println sqlStatements[0]
        println sqlStatements[1]

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[0]).getTableName()
        "c7ac2480-bc96-11e2-a300-64315073a768" == ((InsertStatement) sqlStatements[0]).getColumnValue("id")
        "NULL" == ((InsertStatement) sqlStatements[0]).getColumnValue("parent_id")

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[1]).getTableName()
        "c801be90-bc96-11e2-a300-64315073a768" == ((InsertStatement) sqlStatements[1]).getColumnValue("id")
        "3c39ee40-ac78-11e4-aca7-78acc0c3521f" == ((InsertStatement) sqlStatements[1]).getColumnValue("parent_id")
    }


    def getConfirmationMessage() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("FILE_NAME");

        then:
        "Data loaded from 'FILE_NAME' into table 'TABLE_NAME'" == refactoring.getConfirmationMessage()
    }

    def "generateChecksum produces different values with each field"() {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");

        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setFile("liquibase/change/core/sample.data2.csv");
        String md5sum2 = refactoring.generateCheckSum().toString();

        then:
        assert !md5sum1.equals(md5sum2)
        refactoring.generateCheckSum().toString() == md5sum2
    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false
    }

    def "checkStatus"() {
        when:
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def change = new LoadDataChange()

        then:
        assert change.checkStatus(mockDb).status == ChangeStatus.Status.unknown
        assert change.checkStatus(mockDb).message == "Cannot check loadData status"
    }

    def "load works"() {
        when:
        def change = new LoadDataChange()
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "loadData").setValue([
                    [column: [name: "id"]],
                    [column: [name: "new_col", header: "new_col_header"]],
            ]), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.columns.size() == 2
        change.columns[0].name == "id"
        change.columns[0].header == null

        change.columns[1].name == "new_col"
        change.columns[1].header == "new_col_header"
    }

    def "relativeToChangelogFile works"() throws Exception {
        when:
        ChangeSet changeSet = new ChangeSet(null, null, true, false,
                "liquibase/empty.changelog.xml",
                null, null, false, null, null);

        LoadDataChange relativeChange = new LoadDataChange();

        relativeChange.setSchemaName("SCHEMA_NAME");
        relativeChange.setTableName("TABLE_NAME");
        relativeChange.setRelativeToChangelogFile(Boolean.TRUE);
        relativeChange.setChangeSet(changeSet);
        relativeChange.setFile("change/core/sample.data1.csv");

        SqlStatement[] relativeStatements = relativeChange.generateStatements(mockDb);

        LoadUpdateDataChange nonRelativeChange = new LoadUpdateDataChange();
        nonRelativeChange.setSchemaName("SCHEMA_NAME");
        nonRelativeChange.setTableName("TABLE_NAME");
        nonRelativeChange.setChangeSet(changeSet);
        nonRelativeChange.setFile("liquibase/change/core/sample.data1.csv");

        SqlStatement[] nonRelativeStatements = nonRelativeChange.generateStatements(mockDb);

        then:
        assert relativeStatements != null
        assert nonRelativeStatements != null
        assert relativeStatements.size() == nonRelativeStatements.size()
    }

    def "checksum does not change when no comments in CSV and comment property changes"() {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");

        refactoring.setCommentLineStartsWith("") //comments disabled
        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setCommentLineStartsWith("#");
        String md5sum2 = refactoring.generateCheckSum().toString();

        then:
        assert md5sum1.equals(md5sum2)
    }

    def "checksum changes when there are comments in CSV"() {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-withComments.csv");

        refactoring.setCommentLineStartsWith("") //comments disabled
        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setCommentLineStartsWith("#");
        String md5sum2 = refactoring.generateCheckSum().toString();

        then:
        assert !md5sum1.equals(md5sum2)
    }

    def "checksum same for CSV files with comments and file with removed comments manually"() {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-withComments.csv");

        refactoring.setCommentLineStartsWith("#");
        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setFile("liquibase/change/core/sample.data1-removedComments.csv");
        refactoring.setCommentLineStartsWith(""); //disable comments just in case
        String md5sum2 = refactoring.generateCheckSum().toString();

        then:
        assert md5sum1.equals(md5sum2)
    }

    def "usePreparedStatements set to false produces InsertSetStatement"() throws Exception {
        when:
        LoadDataChange loadDataChange = new LoadDataChange();
        loadDataChange.setSchemaName("SCHEMA_NAME");
        loadDataChange.setTableName("TABLE_NAME");
        loadDataChange.setUsePreparedStatements(Boolean.FALSE);
        loadDataChange.setFile("liquibase/change/core/sample.data1.csv");

        SqlStatement[] sqlStatement = loadDataChange.generateStatements(new MSSQLDatabase());

        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof InsertSetStatement

        when:
        SqlStatement[] sqlStatements = ((InsertSetStatement) sqlStatement[0]).getStatementsArray();

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof InsertStatement
        assert sqlStatements[1] instanceof InsertStatement

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[0]).getTableName()
        "Bob Johnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("name")
        "bjohnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("username")

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[1]).getTableName()
        "John Doe" == ((InsertStatement) sqlStatements[1]).getColumnValue("name")
        "jdoe" == ((InsertStatement) sqlStatements[1]).getColumnValue("username")
    }
    def "usePreparedStatements set to true produces PreparedStatement"() throws Exception {
        when:
        LoadDataChange loadDataChange = new LoadDataChange();
        loadDataChange.setSchemaName("SCHEMA_NAME");
        loadDataChange.setTableName("TABLE_NAME");
        loadDataChange.setUsePreparedStatements(Boolean.TRUE);
        loadDataChange.setFile("liquibase/change/core/sample.data1.csv");

        SqlStatement[] sqlStatement = loadDataChange.generateStatements(new MSSQLDatabase() { public boolean supportsBatchUpdates() { return true; } });

        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof ExecutablePreparedStatement

        when:
        SqlStatement[] sqlStatements = ((ExecutablePreparedStatement) sqlStatement[0]).getIndividualStatements();

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof ExecutablePreparedStatement
        assert sqlStatements[1] instanceof ExecutablePreparedStatement

        "SCHEMA_NAME" == ((ExecutablePreparedStatementBase) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((ExecutablePreparedStatementBase) sqlStatements[0]).getTableName()


        "SCHEMA_NAME" == ((ExecutablePreparedStatementBase) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((ExecutablePreparedStatementBase) sqlStatements[1]).getTableName()

    }

    def "DB Batch Update Support usePrepared False produces InsertSetStatement"() throws Exception {
        when:
        LoadDataChange loadDataChange = new LoadDataChange();
        loadDataChange.setSchemaName("SCHEMA_NAME");
        loadDataChange.setTableName("TABLE_NAME");
        loadDataChange.setUsePreparedStatements(Boolean.FALSE);
        loadDataChange.setFile("liquibase/change/core/sample.data1.csv");

        SqlStatement[] sqlStatement = loadDataChange.generateStatements(new MSSQLDatabase() { public boolean supportsBatchUpdates() { return true; } });

        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof InsertSetStatement

        when:
        SqlStatement[] sqlStatements = ((InsertSetStatement) sqlStatement[0]).getStatementsArray();

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof InsertStatement
        assert sqlStatements[1] instanceof InsertStatement

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[0]).getTableName()
        "Bob Johnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("name")
        "bjohnson" == ((InsertStatement) sqlStatements[0]).getColumnValue("username")

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[1]).getTableName()
        "John Doe" == ((InsertStatement) sqlStatements[1]).getColumnValue("name")
        "jdoe" == ((InsertStatement) sqlStatements[1]).getColumnValue("username")
    }

    Table addColumns(Table table, ColDef... colunms) {
        colunms.each {
            table.addColumn(new Column(Table.class, table.schema.catalogName, table.schema.name, table.name
                    , it.name).setType(new DataType(it.type))
            )
        }
        table
    }

    Table newTable(String tableName, String schemaName = null, String catName = "DEFAULT") {
        new Table(catName, schemaName, tableName)
    }

    Table testTable(String tableName, String schemaName = null, String catName = "DEFAULT") {
        def table = newTable(tableName, schemaName, catName)
        addColumns(table, new ColDef(Col.name, "varchar(123)"),
                new ColDef(Col.num, "decimal()"),
                new ColDef(Col.id, "int"),
                new ColDef(Col.date, "timestamp"),
                new ColDef(Col.bool, "bit"))
    }

    def mockDB = new MockDatabase() {
        @Override
        public String correctObjectName(final String name, final Class<? extends DatabaseObject> objectType) {
            return name;
        }
    }

    def columnValue(SqlStatement sqlStatement, Object colName) {
        ((InsertStatement) sqlStatement).getColumnValue(colName.toString())
    }

    def "empty values"() {
        when:
        def table = testTable("table")
        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory(table)

        LoadDataChange change = new LoadDataChange()
        change.setFile("liquibase/change/core/sample.data.with.emptys.csv")
        change.setTableName(table.name)

        SqlStatement[] sqlStatements = change.generateStatements(mockDB)

        then:
        change.columns[0].getTypeEnum() == LoadDataChange.LOAD_DATA_TYPE.STRING
        change.columns[1].getTypeEnum() == LoadDataChange.LOAD_DATA_TYPE.NUMERIC
        change.columns[2].getTypeEnum() == LoadDataChange.LOAD_DATA_TYPE.DATE
        change.columns[3].getTypeEnum() == LoadDataChange.LOAD_DATA_TYPE.BOOLEAN

        columnValue(sqlStatements[0], Col.name) == "Fred"
        columnValue(sqlStatements[0], Col.num) == 2
        def dCol = columnValue(sqlStatements[0], Col.date)
        assert dCol instanceof Timestamp
        ((Timestamp) dCol).toLocalDateTime() == LocalDateTime.parse("2008-03-02T12:13:00")
        columnValue(sqlStatements[0], Col.bool) == Boolean.TRUE

        // All other NULL or empty fields are null
        [Col.name, Col.num, Col.num, Col.bool].each {
            columnValue(sqlStatements[1], it) == "NULL"
            columnValue(sqlStatements[2], it) == "NULL"
        }
    }

    def "defaultXXX"() {
        when:
        def table = testTable("table")
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/sample.data.with.emptys.csv",
                tableName: table.name
        ]).setValue([
                [column: [name: "id", header: "num", defaultValueNumeric: 1]],
                [column: [name: "name", defaultValue: "defName"]],
                [column: [name: "date", defaultValueDate: "2020-01-02 03:04:05"]],
                [column: [name: "bool", defaultValueBoolean: "true"]]
        ]), new ClassLoaderResourceAccessor())

        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory(table)

        SqlStatement[] sqlStatements = change.generateStatements(mockDB);

        then:
        columnValue(sqlStatements[0], Col.name) == "Fred"
        columnValue(sqlStatements[0], Col.id) == 2
        def dCol = columnValue(sqlStatements[0], Col.date)
        assert dCol instanceof Timestamp
        ((Timestamp) dCol).toLocalDateTime() == LocalDateTime.parse("2008-03-02T12:13:00")
        columnValue(sqlStatements[0], Col.bool) == Boolean.TRUE
        // All  NULL fields are still null
        for (Col n : [Col.name, Col.id, Col.date, Col.bool]) {
            columnValue(sqlStatements[1], n) == null
        }
        columnValue(sqlStatements[2], Col.name) == "defName"
        columnValue(sqlStatements[2], Col.id) == 1
        def dCol2 = columnValue(sqlStatements[2], Col.date)
        assert dCol2 instanceof Timestamp
        ((Timestamp) dCol2).toLocalDateTime() == LocalDateTime.parse("2020-01-02T03:04:05")
        columnValue(sqlStatements[2], Col.bool) == Boolean.TRUE
    }

    def "defaults"() {
        when:
        def table = testTable("table")
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/sample.data.with.emptys.csv",
                tableName: table.name
        ]).setValue([
                [column: [name: "id", header: "num", defaultValue: 1]],
                [column: [name: "name", defaultValue: "defName"]],
                [column: [name: "date", defaultValue: "2020-01-02 03:04:05"]],
                [column: [name: "bool", defaultValue: "t"]]
        ]), new ClassLoaderResourceAccessor())

        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory(table)

        SqlStatement[] sqlStatements = change.generateStatements(mockDB);

        then:
        columnValue(sqlStatements[0], Col.name) == "Fred"
        columnValue(sqlStatements[0], Col.id) == 2
        def dCol = columnValue(sqlStatements[0], Col.date)
        assert dCol instanceof Timestamp
        ((Timestamp) dCol).toLocalDateTime() == LocalDateTime.parse("2008-03-02T12:13:00")
        columnValue(sqlStatements[0], Col.bool) == Boolean.TRUE
        // All  NULL fields are still null
        [Col.name, Col.id, Col.date, Col.bool].each {
            columnValue( sqlStatements[1], it) == null
        }
        columnValue(sqlStatements[2], Col.name) == "defName"
        columnValue(sqlStatements[2], Col.id) == 1
        def dCol2 = columnValue(sqlStatements[2], Col.date)
        assert dCol2 instanceof Timestamp
        ((Timestamp) dCol2).toLocalDateTime() == LocalDateTime.parse("2020-01-02T03:04:05")
        columnValue(sqlStatements[2], Col.bool) == Boolean.TRUE
    }

    def "string with space + DB def"() {
        when:
        Table table = newTable("t");
        Cols2.values().each {
            addColumns(table, new ColDef(it, "varchar(123)"))
        }
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/strings.csv",
                tableName: table.name, quotchar: "'"]), new ClassLoaderResourceAccessor())

        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory(table)

        SqlStatement[] sqlStatements = change.generateStatements(mockDB);

        then:
        columnValue(sqlStatements[i], Cols2.regular) == regular
        columnValue(sqlStatements[i], Cols2.space_left) == left
        columnValue(sqlStatements[i], Cols2.space_right) == right
        columnValue(sqlStatements[i], Cols2.space_both) == both
        columnValue(sqlStatements[i], Cols2.empty) == ""

        where:
        i | regular | left    | right   | both
        0 | "NULL"  | ""      | " "     | ""
        1 | "NULL"  | " null" | "null " | " null "   // NULL variants
        2 | ""      | " '"    | "' "    | " ' "      // quoted empty string
        3 | " "     | " ' "   | " ' "   | " ' ' "    // quoted space
        4 | "a"     | " a"    | "a "    | " a "      // a
        5 | "a"     | " 'a"   | "a' "   | " 'a' "    // quoted a
    }

    def "inconsistent NULL handling"() {
        when:
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/sample.data.with.emptys.csv",
                tableName: "table.name"
        ]).setValue([
                [column: [name: "name", type: "STRING"]],
                [column: [name: "date"]],
        ]), new ClassLoaderResourceAccessor())

        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory()

        SqlStatement[] sqlStatements = change.generateStatements(mockDB)
        then:
        columnValue(sqlStatements[1], Col.name) == "NULL"
        columnValue(sqlStatements[1], Col.num) == "NULL"    // FIX this was ""
        columnValue(sqlStatements[1], Col.date) == "NULL"   // FIX this was" "

        columnValue(sqlStatements[3], Col.name) == " s"
        columnValue(sqlStatements[3], Col.num) == " s"    // FIX this was "s"
        columnValue(sqlStatements[3], Col.date) == " s"   // FIX this was "s"
    }

    def "all columns defined" () {
        when:
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/sample.data1.csv",
                tableName: "table.name"
        ]).setValue([
                [column: [name: "a", header:"name", index:1, type: "STRING"]],
                [column: [name: "username", type: "STRING"]]
        ]), new ClassLoaderResourceAccessor())
        ValidationErrors foundErrors = change.validate(mockDB);
        SqlStatement[] sqlStatements = change.generateStatements(mockDB)

        then:
        assert foundErrors.getWarningMessages().size() == 1
        foundErrors.getWarningMessages().get(0) == "Since attribute 'header' is also defined, " +
        "'index' ignored for loadData / column[1] (name:'a')"
        columnValue(sqlStatements[0], "a") == "Bob Johnson"
        columnValue(sqlStatements[0], "username") == "bjohnson"
    }

    def validate_Columns() {
        when:
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/sample.data1.csv",
                tableName: ""
        ]).setValue([
                [column: [name: "a", header:"", index:1, type: "STRING", defaultValue: ""]],
                [column: [name: "", type: ""]],
                [column: []]
        ]), new ClassLoaderResourceAccessor())

        ValidationErrors errors = change.validate(mockDB)
        then:
        errors.getErrorMessages().size() == 1
        errors.getErrorMessages().get(i) == message

        errors.getWarningMessages().size() == 1
        errors.getWarningMessages().get(0) ==
                "Since attribute 'header' is also defined, 'index' ignored for loadData / column[1] (name:'a')"

        where:
        i | message
        0 | "tableName is empty for loadData on mock"
    }


    class ColDef {
        ColDef(Object n, String type) {
            this.name = n.toString()
            this.type = type
        }
        String name
        String type
    }

    enum Col {
        name, num, date, bool, id

        String s() {
            return name();
        }
    }

    enum Cols2 {
        regular, space_left, space_right, space_both, empty
    }

}


