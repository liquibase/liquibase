package liquibase.change.core

import liquibase.ChecksumVersion
import liquibase.GlobalConfiguration
import liquibase.Null
import liquibase.Scope
import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.database.DatabaseFactory
import liquibase.database.core.H2Database
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MockDatabase
import liquibase.exception.ValidationErrors
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.*
import liquibase.statement.core.InsertSetStatement
import liquibase.statement.core.InsertStatement
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Table
import liquibase.test.JUnitResourceAccessor
import liquibase.test.TestContext
import liquibase.util.csv.CSVReader
import spock.lang.Unroll

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class LoadDataChangeTest extends StandardChangeTest {

    MSSQLDatabase mssqlDb
    MockDatabase mockDb

    def setup() {
        ResourceAccessor resourceAccessor = TestContext.getInstance().getTestResourceAccessor()
        String offlineUrl

        mssqlDb = new MSSQLDatabase()
        mssqlDb.setConnection(DatabaseFactory.getInstance().openConnection("offline:mssql",
                "superuser", "superpass", null, resourceAccessor))

        mockDb = new MockDatabase()
        mockDb.setConnection((DatabaseConnection) null)
    }



    def "column with clob datatype is path or string"(){
        when:
        def changelog = new DatabaseChangeLog("liquibase/changelog.xml")
        ChangeSet changeSet = new ChangeSet(null, null, true, false,
                "logical or physical file name",
                null, null, false, null, changelog)

        LoadDataChange change = new LoadDataChange()
        LoadDataColumnConfig col1 = new LoadDataColumnConfig()
        col1.setType(LoadDataChange.LOAD_DATA_TYPE.CLOB)
        col1.setName("exists")
        change.addColumn(col1)

        LoadDataColumnConfig col2 = new LoadDataColumnConfig()
        col2.setType(LoadDataChange.LOAD_DATA_TYPE.CLOB)
        col2.setName("doesnt")
        change.addColumn(col2)

        LoadDataColumnConfig col3 = new LoadDataColumnConfig()
        col3.setType(LoadDataChange.LOAD_DATA_TYPE.CLOB)
        col3.setName("string")
        change.addColumn(col3)

        LoadDataColumnConfig col4 = new LoadDataColumnConfig()
        col4.setType(LoadDataChange.LOAD_DATA_TYPE.CLOB)
        col4.setName("invalid")
        change.addColumn(col4)

        LoadDataColumnConfig col5 = new LoadDataColumnConfig()
        col5.setType(LoadDataChange.LOAD_DATA_TYPE.CLOB)
        col5.setName("empty")
        change.addColumn(col5)

        change.setSchemaName("SCHEMA_NAME")
        change.setTableName("TABLE_NAME")
        change.setRelativeToChangelogFile(Boolean.TRUE)
        change.setChangeSet(changeSet)
        change.setFile("../liquibase/change/core/sample.data.for.clob.types.csv")

        def mockDBCsv = new MockDatabase() {
            @Override
            boolean supportsBatchUpdates() {
                return true
            }
        }

        SqlStatement[] stmt = change.generateStatements(mockDBCsv)

        then:
        stmt.length == 1
        assert stmt[0] instanceof BatchDmlExecutablePreparedStatement

        List<ExecutablePreparedStatementBase> insSt = ((BatchDmlExecutablePreparedStatement) stmt[0]).getIndividualStatements()
        List<LoadDataColumnConfig> columns = insSt.get(0).getColumns()

        "SCHEMA_NAME" == insSt.get(0).getSchemaName()
        "TABLE_NAME" == insSt.get(0).getTableName()
        "change/core/SQLFileTestData.sql" == columns.get(0).getValueClobFile()
        "nothing.txt" == columns.get(1).getValue()
        "sample text" == columns.get(2).getValue()
        "/dev/null/foo:a" == columns.get(3).getValue()
        null == columns.get(4).getValue()
    }

    def "loadDataEmpty database agnostic"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/empty.data.csv")
        refactoring.setSeparator(",")

        SqlStatement[] sqlStatement = refactoring.generateStatements(mssqlDb)
        then:
        sqlStatement.length == 0
    }

    def "loadDataEmpty not using InsertSetStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/empty.data.csv")
        refactoring.setSeparator(",")

        SqlStatement[] sqlStatements = refactoring.generateStatements(mockDb)

        then:
        sqlStatements.length == 0
    }


    @Unroll("multiple formats with the same data for #fileName")
    def "multiple formats with the same data using InsertSetStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile(fileName)
        if (separator != null) {
            refactoring.setSeparator(separator)
        }
        if (quotChar != null) {
            refactoring.setQuotchar(quotChar)
        }

        SqlStatement[] sqlStatement = refactoring.generateStatements(new MSSQLDatabase())
        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof InsertSetStatement

        when:
        SqlStatement[] sqlStatements = ((InsertSetStatement) sqlStatement[0]).getStatementsArray()

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
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile(fileName)
        if (separator != null) {
            refactoring.setSeparator(separator)
        }
        if (quotChar != null) {
            refactoring.setQuotchar(quotChar)
        }

        SqlStatement[] sqlStatements = refactoring.generateStatements(mockDb)

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
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/sample.data1-excel.csv")

        LoadDataColumnConfig ageConfig = new LoadDataColumnConfig()
        ageConfig.setHeader("age")
        ageConfig.setType("NUMERIC")
        refactoring.addColumn(ageConfig)

        LoadDataColumnConfig activeConfig = new LoadDataColumnConfig()
        activeConfig.setHeader("active")
        activeConfig.setType("BOOLEAN")
        refactoring.addColumn(activeConfig)

        SqlStatement[] sqlStatement = refactoring.generateStatements(new MSSQLDatabase())
        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof InsertSetStatement

        when:
        SqlStatement[] sqlStatements = ((InsertSetStatement) sqlStatement[0]).getStatementsArray()

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
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/sample.data1-excel.csv")

        LoadDataColumnConfig ageConfig = new LoadDataColumnConfig()
        ageConfig.setHeader("age")
        ageConfig.setType("NUMERIC")
        refactoring.addColumn(ageConfig)

        LoadDataColumnConfig activeConfig = new LoadDataColumnConfig()
        activeConfig.setHeader("active")
        activeConfig.setType("BOOLEAN")
        refactoring.addColumn(activeConfig)

        SqlStatement[] sqlStatements = refactoring.generateStatements(mockDb)

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
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/sample.data3.csv")

        LoadDataColumnConfig idConfig = new LoadDataColumnConfig()
        idConfig.setHeader("id")
        idConfig.setType("UUID")
        refactoring.addColumn(idConfig)

        LoadDataColumnConfig parentIdConfig = new LoadDataColumnConfig()
        parentIdConfig.setHeader("parent_id")
        parentIdConfig.setType("UUID")
        refactoring.addColumn(parentIdConfig)

        SqlStatement[] sqlStatements = refactoring.generateStatements(mockDb)

        then:
        sqlStatements.length == 2
        assert sqlStatements[0] instanceof InsertStatement
        assert sqlStatements[1] instanceof InsertStatement
        println sqlStatements[0]
        println sqlStatements[1]

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[0]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[0]).getTableName()
        "c7ac2480-bc96-11e2-a300-64315073a768" == ((InsertStatement) sqlStatements[0]).getColumnValue("id")
        new Null() == ((InsertStatement) sqlStatements[0]).getColumnValue("parent_id") // NULL sql ref

        "SCHEMA_NAME" == ((InsertStatement) sqlStatements[1]).getSchemaName()
        "TABLE_NAME" == ((InsertStatement) sqlStatements[1]).getTableName()
        "c801be90-bc96-11e2-a300-64315073a768" == ((InsertStatement) sqlStatements[1]).getColumnValue("id")
        "3c39ee40-ac78-11e4-aca7-78acc0c3521f" == ((InsertStatement) sqlStatements[1]).getColumnValue("parent_id")
    }


    def getConfirmationMessage() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("FILE_NAME")

        then:
        "Data loaded from 'FILE_NAME' into table 'TABLE_NAME'" == refactoring.getConfirmationMessage()
    }

    @Unroll
    def "generateChecksum produces different values with each field - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/sample.data1.csv")

        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setFile("liquibase/change/core/sample.data2.csv")
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:a91f2379b2b3b4c4a5a571b8e7409081" | "8:cce1423feea9e29192ef7c306eda0c94"
        ChecksumVersion.latest() | "9:55d574d66869989f7208b9f05b7409bb" | "9:b0cc70905a4b9db9211c05392fd08f08"
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
        def changelog = new DatabaseChangeLog("liquibase/changelog.xml")
        ChangeSet changeSet = new ChangeSet(null, null, true, false,
                "logical or physical file name",
                null, null, false, null, changelog)

        LoadDataChange relativeChange = new LoadDataChange()

        relativeChange.setSchemaName("SCHEMA_NAME")
        relativeChange.setTableName("TABLE_NAME")
        relativeChange.setRelativeToChangelogFile(Boolean.TRUE)
        relativeChange.setChangeSet(changeSet)
        relativeChange.setFile("change/core/sample.data1.csv")

        SqlStatement[] relativeStatements = relativeChange.generateStatements(mockDb)

        LoadUpdateDataChange nonRelativeChange = new LoadUpdateDataChange()
        nonRelativeChange.setSchemaName("SCHEMA_NAME")
        nonRelativeChange.setTableName("TABLE_NAME")
        nonRelativeChange.setChangeSet(changeSet)
        nonRelativeChange.setFile("liquibase/change/core/sample.data1.csv")

        SqlStatement[] nonRelativeStatements = nonRelativeChange.generateStatements(mockDb)

        then:
        assert relativeStatements != null
        assert nonRelativeStatements != null
        assert relativeStatements.size() == nonRelativeStatements.size()
    }

    @Unroll
    def "openSqlStream correctly opens files"() {
        when:
        def changelog = new DatabaseChangeLog("com/example/changelog.xml")

        def changeset = new ChangeSet("1", "auth", false, false, logicalFilePath, null, null, changelog)

        def change = new LoadDataChange()
        change.file = csvPath
        change.relativeToChangelogFile = relativeToChangelogFile
        change.setChangeSet(changeset)

        CSVReader csvReader = Scope.child([(Scope.Attr.resourceAccessor.name()): new JUnitResourceAccessor()], {
            return change.getCSVReader()
        } as Scope.ScopedRunnerWithReturn<CSVReader>)

        then:
        csvReader != null

        where:
        csvPath                   | logicalFilePath      | relativeToChangelogFile
        "com/example/users.csv"   | null                 | false
        "com/example/users.csv"   | "a/logical/path.xml" | false
        "users.csv"               | null                 | true
        "users.csv"               | "a/logical/path.xml" | true
    }

    @Unroll
    def "checksum does not change when no comments in CSV and comment property changes - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/sample.data1.csv")

        refactoring.setCommentLineStartsWith("") //comments disabled
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setCommentLineStartsWith("#")
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:a91f2379b2b3b4c4a5a571b8e7409081" | "8:a91f2379b2b3b4c4a5a571b8e7409081"
        ChecksumVersion.latest() | "9:55d574d66869989f7208b9f05b7409bb" | "9:55d574d66869989f7208b9f05b7409bb"
    }

    @Unroll
    def "checksum changes when there are comments in CSV - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/sample.data1-withComments.csv")

        refactoring.setCommentLineStartsWith("") //comments disabled
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setCommentLineStartsWith("#")
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:becddfbcfda2ec516371ed36aaf1137a" | "8:e51a6408e921cfa151c50c7d90cf5baa"
        ChecksumVersion.latest() | "9:c02972964ae29d51fa8e7801951fbb70" | "9:91298c1042fcb57394a242e8c838ce51"
    }

    @Unroll
    def "checksum same for CSV files with comments and file with removed comments manually - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        LoadDataChange refactoring = new LoadDataChange()
        refactoring.setSchemaName("SCHEMA_NAME")
        refactoring.setTableName("TABLE_NAME")
        refactoring.setFile("liquibase/change/core/sample.data1-withComments.csv")

        refactoring.setCommentLineStartsWith("#")
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setFile("liquibase/change/core/sample.data1-removedComments.csv")
        refactoring.setCommentLineStartsWith("") //disable comments just in case
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        ChecksumVersion.V8 | "8:e51a6408e921cfa151c50c7d90cf5baa" | "8:e51a6408e921cfa151c50c7d90cf5baa"
        ChecksumVersion.latest() | "9:91298c1042fcb57394a242e8c838ce51" | "9:91298c1042fcb57394a242e8c838ce51"
    }

    def "usePreparedStatements set to false produces InsertSetStatement"() throws Exception {
        when:
        LoadDataChange loadDataChange = new LoadDataChange()
        loadDataChange.setSchemaName("SCHEMA_NAME")
        loadDataChange.setTableName("TABLE_NAME")
        loadDataChange.setUsePreparedStatements(Boolean.FALSE)
        loadDataChange.setFile("liquibase/change/core/sample.data1.csv")

        SqlStatement[] sqlStatement = loadDataChange.generateStatements(new MSSQLDatabase())

        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof InsertSetStatement

        when:
        SqlStatement[] sqlStatements = ((InsertSetStatement) sqlStatement[0]).getStatementsArray()

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
        LoadDataChange loadDataChange = new LoadDataChange()
        loadDataChange.setSchemaName("SCHEMA_NAME")
        loadDataChange.setTableName("TABLE_NAME")
        loadDataChange.setUsePreparedStatements(Boolean.TRUE)
        loadDataChange.setFile("liquibase/change/core/sample.data1.csv")

        SqlStatement[] sqlStatement = loadDataChange.generateStatements(new MSSQLDatabase() { public boolean supportsBatchUpdates() { return true } })

        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof ExecutablePreparedStatement

        when:
        SqlStatement[] sqlStatements = ((ExecutablePreparedStatement) sqlStatement[0]).getIndividualStatements()

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
        LoadDataChange loadDataChange = new LoadDataChange()
        loadDataChange.setSchemaName("SCHEMA_NAME")
        loadDataChange.setTableName("TABLE_NAME")
        loadDataChange.setUsePreparedStatements(Boolean.FALSE)
        loadDataChange.setFile("liquibase/change/core/sample.data1.csv")

        SqlStatement[] sqlStatement = loadDataChange.generateStatements(new MSSQLDatabase() { public boolean supportsBatchUpdates() { return true } })

        then:
        sqlStatement.length == 1
        assert sqlStatement[0] instanceof InsertSetStatement

        when:
        SqlStatement[] sqlStatements = ((InsertSetStatement) sqlStatement[0]).getStatementsArray()

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
            return name
        }
    }

    def columnValue(SqlStatement sqlStatement, Object colName) {
        ((InsertStatement) sqlStatement).getColumnValue(colName.toString())
    }

    def "validate load data sets values successfully"() {
        when:
        def table = testTable("table")
        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory(table)

        LoadDataChange change = new LoadDataChange()
        change.setFile("liquibase/change/core/sample.data.csv")
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
        assert ((Timestamp) dCol).toLocalDateTime() == LocalDateTime.parse("2008-03-02T12:13:00")
        assert columnValue(sqlStatements[0], Col.bool) == Boolean.TRUE
    }

    def "validate load data works successfully with empty and null and invalid values"() {
        when:
        def table = testTable("table")
        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory(table)

        LoadDataChange change = new LoadDataChange()
        change.setFile("liquibase/change/core/sample.data.with.null.and.emptys.csv")
        change.setTableName(table.name)

        SqlStatement[] sqlStatements = change.generateStatements(mockDB)

        then:
        // All other NULL or empty fields are null
        [Col.name, Col.num, Col.date, Col.bool].each {
            assert columnValue(sqlStatements[0], it) == new Null() // NULL sql ref
        }
        columnValue(sqlStatements[1], Col.name) == ""
        [Col.num, Col.num, Col.bool].each {
            assert columnValue(sqlStatements[1], it) == new Null() // NULL sql ref
        }
        columnValue(sqlStatements[2], Col.name) == "'NULL'"
        [Col.num, Col.date].each {
            assert ((DatabaseFunction)columnValue(sqlStatements[2], it)).getValue() == "'NULL'"
        }
        columnValue(sqlStatements[2], Col.bool) == false
    }

    def "validate load data set default values successfully only for empty entries"() {
        when:
        def table = testTable("table")
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/sample.data.with.null.and.emptys.csv",
                tableName: table.name
        ]).setValue([
                [column: [name: "id", header: "num", defaultValueNumeric: 1]],
                [column: [name: "name", defaultValue: "defName"]],
                [column: [name: "date", defaultValueDate: "2020-01-02 03:04:05"]],
                [column: [name: "bool", defaultValueBoolean: "true"]]
        ]), new ClassLoaderResourceAccessor())

        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory(table)

        SqlStatement[] sqlStatements = change.generateStatements(mockDB)

        then:
        columnValue(sqlStatements[0], Col.name) != "defName"
        columnValue(sqlStatements[0], Col.id) != 1
        columnValue(sqlStatements[1], Col.name) == "defName"
        columnValue(sqlStatements[1], Col.id) == 1
        def dCol2 = columnValue(sqlStatements[1], Col.date)
        assert dCol2 instanceof Timestamp
        ((Timestamp) dCol2).toLocalDateTime() == LocalDateTime.parse("2020-01-02T03:04:05")
        columnValue(sqlStatements[1], Col.bool) == Boolean.TRUE
        columnValue(sqlStatements[2], Col.name) != "defName"
        columnValue(sqlStatements[2], Col.id) != 1
    }


    def "NULL placeholder handling"() {
        when:
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/sample.data.with.nulls.csv",
                tableName: "table.name"
        ]).setValue([
                [column: [name: "name", type: "STRING"]],
                [column: [name: "middlename", type: "STRING", nullPlaceholder: ""]],
                [column: [name: "surname", nullPlaceholder: ""]],
                [column: [name: "job", type: "STRING", nullPlaceholder: "NONE"]],
        ]), new ClassLoaderResourceAccessor())

        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory()

        SqlStatement[] sqlStatements = change.generateStatements(mockDB)
        then: // any NULL value for the statement is seen here as a 'NULL' string
        //Fred,Twinkletoes,Flintstone,Crane operator
        columnValue(sqlStatements[0], WorkerCol.name) == "Fred"
        columnValue(sqlStatements[0], WorkerCol.middlename) == "Twinkletoes"
        columnValue(sqlStatements[0], WorkerCol.surname) == "Flintstone"
        columnValue(sqlStatements[0], WorkerCol.job) == "Crane operator"
        //null,null,null,null
        columnValue(sqlStatements[1], WorkerCol.name) == new Null() // NULL sql ref
        columnValue(sqlStatements[1], WorkerCol.middlename) == "null"
        columnValue(sqlStatements[1], WorkerCol.surname) == "null"
        columnValue(sqlStatements[1], WorkerCol.job) == "null"
        //,,,
        columnValue(sqlStatements[2], WorkerCol.name) == ""
        columnValue(sqlStatements[2], WorkerCol.middlename) == new Null() // NULL sql ref
        columnValue(sqlStatements[2], WorkerCol.surname) == new Null() // NULL sql ref
        columnValue(sqlStatements[2], WorkerCol.job) == ""
        //NONE,NONE,NONE,NONE
        columnValue(sqlStatements[3], WorkerCol.name) == "NONE"
        columnValue(sqlStatements[3], WorkerCol.middlename) == "NONE"
        columnValue(sqlStatements[3], WorkerCol.surname) == "NONE"
        columnValue(sqlStatements[3], WorkerCol.job) == new Null() // NULL sql ref
    }

    def "temporal values work for DATE, DATETIME and TIME column configs"() {
        when:
        LoadDataChange change = new LoadDataChange()

        change.load(new liquibase.parser.core.ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/change/core/sample.data.temporal.csv",
                tableName: "table.name"
        ]).setValue([
                [column: [name: "id", type: "NUMERIC"]],
                [column: [name: "date", type: "DATE"]],
                [column: [name: "datetime", type: "DATETIME"]],
                [column: [name: "time", type: "TIME"]],
        ]), new ClassLoaderResourceAccessor())

        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory()

        LocalTime beforeGeneration = LocalTime.now().withNano(0)
        SqlStatement[] sqlStatements = change.generateStatements(mockDB)
        LocalTime afterGeneration = LocalTime.now().withNano(0).plusSeconds(1)

        LocalDate today = LocalDate.now()

        then:
        //NULLS
        columnValue(sqlStatements[0], Col.id) == 1
        columnValue(sqlStatements[0], Col.date) == new Null() // NULL sql ref
        columnValue(sqlStatements[0], Col.datetime) == new Null() // NULL sql ref
        columnValue(sqlStatements[0], Col.time) == new Null() // NULL sql ref

        //NOWANDTODAYUTIL used
        columnValue(sqlStatements[1], Col.id) == 2
        ((Date) columnValue(sqlStatements[1], Col.date)).toLocalDate() == today
        ((Timestamp) columnValue(sqlStatements[1], Col.datetime)).toLocalDateTime() <= LocalDateTime.of(today, afterGeneration)
        ((Timestamp) columnValue(sqlStatements[1], Col.datetime)).toLocalDateTime() >= LocalDateTime.of(today, beforeGeneration)
        ((Time) columnValue(sqlStatements[1], Col.time)).toLocalTime() <= afterGeneration
        ((Time) columnValue(sqlStatements[1], Col.time)).toLocalTime() >= beforeGeneration

        //VALUE PARSED
        columnValue(sqlStatements[2], Col.id) == 3
        ((Date) columnValue(sqlStatements[2], Col.date)).toLocalDate().toString() == "2022-09-13"
        ((Timestamp)columnValue(sqlStatements[2], Col.datetime)).toLocalDateTime().toString() == "2022-09-13T12:34:56.789123"
        ((DatabaseFunction) columnValue(sqlStatements[2], Col.time)).value == "12:34:56.789123"

        columnValue(sqlStatements[3], Col.id) == 4
        ((Date) columnValue(sqlStatements[3], Col.date)).toLocalDate().toString() == "2022-09-13"
        ((Timestamp)columnValue(sqlStatements[3], Col.datetime)).toLocalDateTime().toString() == "2022-09-13T12:34:56.789"
        ((DatabaseFunction) columnValue(sqlStatements[3], Col.time)).value == "12:34:56.789"

        columnValue(sqlStatements[4], Col.id) == 5
        ((Date) columnValue(sqlStatements[4], Col.date)).toLocalDate().toString() == "2022-09-13"
        ((Timestamp)columnValue(sqlStatements[4], Col.datetime)).toLocalDateTime().toString() == "2022-09-13T12:34:56"
        ((Time) columnValue(sqlStatements[4], Col.time)).toLocalTime().toString() == "12:34:56"
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
        ValidationErrors foundErrors = change.validate(mockDB)
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

    def "allow statement generation to be overridden"() {
        given:
        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory()
        FakeLoadDataChangeExtension change = new FakeLoadDataChangeExtension()
        change.setFile("liquibase/change/core/sample.data1.csv")


        when:
        change.generateStatements(mockDB)

        then:
        change.rows.size() == 2
        change.rows[0] == [
                ["needsPreparedStatement": false, "name": "name", "value": "Bob Johnson"],
                ["needsPreparedStatement": false, "name": "username", "value": "bjohnson"]
        ]
        change.rows[1] == [
                ["needsPreparedStatement": false, "name": "name", "value": "John Doe"],
                ["needsPreparedStatement": false, "name": "username", "value": "jdoe"]
        ]
    }

    def "validate CSV column headers are trimmed when TRIM_LOAD_DATA_FILE_HEADER is set to true"() {
        when:
        LoadDataChange change = new LoadDataChange()
        SqlStatement[] sqlStatements

        Scope.child([(GlobalConfiguration.TRIM_LOAD_DATA_FILE_HEADER.key): true], {

        change.load(new ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/load-data.test.csv",
                tableName: "CONFIG_STATE"
                ]), new SearchPathResourceAccessor("target/test-classes/"))

        sqlStatements = change.generateStatements(new H2Database())
        } as Scope.ScopedRunner)

        then:
        def sqlStatement = ((InsertStatement) sqlStatements[0])
        sqlStatement.getColumnValues().keySet()[0] == "config_state_id"
        sqlStatement.getColumnValues().keySet()[1] == "code"
        sqlStatement.getColumnValues().keySet()[2] == "name"
        sqlStatement.getColumnValues().keySet()[3] == "description"
    }

    def "validate CSV column headers are NOT trimmed when TRIM_LOAD_DATA_FILE_HEADER is set to its default value"() {
        when:
        LoadDataChange change = new LoadDataChange()
        SqlStatement[] sqlStatements

        change.load(new ParsedNode(null, "loadData").addChildren([
                file     : "liquibase/load-data.test.csv",
                tableName: "CONFIG_STATE"
        ]), new SearchPathResourceAccessor("target/test-classes/"))

        sqlStatements = change.generateStatements(new H2Database())

        then:
        def sqlStatement = ((InsertStatement) sqlStatements[0])
        sqlStatement.getColumnValues().keySet()[0] == "config_state_id"
        sqlStatement.getColumnValues().keySet()[1] == " code"
        sqlStatement.getColumnValues().keySet()[2] == " name"
        sqlStatement.getColumnValues().keySet()[3] == " description"
    }

    /**
     * This test validates that blank lines in a CSV file are ignored during the load data process and that subsequent rows are processed correctly.
     */
    def "validate blank lines in CSV file are ignored but other rows are loaded as expected"() {
        when:
        def table = testTable("table")
        SnapshotGeneratorFactory.instance = new MockSnapshotGeneratorFactory(table)

        LoadDataChange change = new LoadDataChange()
        change.setFile("liquibase/change/core/sample.data.with.blank.line.csv")
        change.setTableName("table")

        SqlStatement[] sqlStatements = change.generateStatements(mockDB)

        then:
        sqlStatements.length == 2
        assert columnValue(sqlStatements[0], "username") == "bjohnson"
        assert columnValue(sqlStatements[1], "username") == "jdoe"
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
        name, num, date, bool, id, datetime, time

        String s() {
            return name()
        }
    }

    enum Cols2 {
        regular, space_left, space_right, space_both, empty
    }

    enum WorkerCol {
        name, middlename, surname, job

        String s() {
            return name();
        }
    }
    class FakeLoadDataChangeExtension extends LoadDataChange {
        def rows

        protected SqlStatement[] generateStatementsFromRows(Database database, List<LoadDataChange.LoadDataRowConfig> rows) {
            this.rows = rows.collect {
                row ->
                    row.columns.collect {
                        col ->
                            [
                                    "needsPreparedStatement": row.needsPreparedStatement(),
                                    "name"                  : col.getName(),
                                    "value"                 : col.getValueObject(),
                            ]
                    }
            }
            return []
        }
    }
}
