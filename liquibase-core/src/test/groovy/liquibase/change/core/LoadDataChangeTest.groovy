package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeSet
import liquibase.database.DatabaseFactory
import liquibase.database.core.MSSQLDatabase
import liquibase.parser.core.ParsedNodeException
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.SqlStatement
import liquibase.statement.core.InsertSetStatement
import liquibase.statement.core.InsertStatement
import liquibase.test.JUnitResourceAccessor
import liquibase.test.TestContext
import spock.lang.Unroll

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
    }


    def "loadDataEmpty database agnostic"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/empty.data.csv");
        refactoring.setSeparator(",");

        refactoring.setResourceAccessor(new JUnitResourceAccessor());


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

        refactoring.setResourceAccessor(new JUnitResourceAccessor());

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

        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());

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

        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());

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
        fileName | separator | quotChar
        "liquibase/change/core/sample.data1.tsv" | "\t" | null
        "liquibase/change/core/sample.quotchar.tsv" | "\t" | "'"
        "liquibase/change/core/sample.data1.csv" | "," | null
        "liquibase/change/core/sample.data1.csv" | null | null
    }

    def "generateStatement_excel using InsertSetStatement"() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-excel.csv");
        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());
        //refactoring.setResourceAccessor(new JUnitResourceAccessor());

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
        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());
        //refactoring.setResourceAccessor(new JUnitResourceAccessor());

        LoadDataColumnConfig ageConfig = new LoadDataColumnConfig();
        ageConfig.setHeader("age");
        ageConfig.setType("NUMERIC");
        refactoring.addColumn(ageConfig);

        LoadDataColumnConfig activeConfig = new LoadDataColumnConfig();
        activeConfig.setHeader("active");
        activeConfig.setType("BOOLEAN");
        refactoring.addColumn(activeConfig);

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());

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


    def getConfirmationMessage() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("FILE_NAME");

        then:
        "Data loaded from FILE_NAME into TABLE_NAME" == refactoring.getConfirmationMessage()
    }

    def "generateChecksum produces different values with each field"() {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");
        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());
        //refactoring.setFileOpener(new JUnitResourceAccessor());

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
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def change = new LoadDataChange()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check loadData status"
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
                "liquibase/change/fakeChangeSet.xml",
                null, null, false, null, null);

        LoadDataChange relativeChange = new LoadDataChange();

        relativeChange.setSchemaName("SCHEMA_NAME");
        relativeChange.setTableName("TABLE_NAME");
        relativeChange.setRelativeToChangelogFile(Boolean.TRUE);
        relativeChange.setChangeSet(changeSet);
        relativeChange.setFile("core/sample.data1.csv");
        relativeChange.setResourceAccessor(new ClassLoaderResourceAccessor());

        SqlStatement[] relativeStatements = relativeChange.generateStatements(mockDb);

        LoadUpdateDataChange nonRelativeChange = new LoadUpdateDataChange();
        nonRelativeChange.setSchemaName("SCHEMA_NAME");
        nonRelativeChange.setTableName("TABLE_NAME");
        nonRelativeChange.setChangeSet(changeSet);
        nonRelativeChange.setFile("liquibase/change/core/sample.data1.csv");
        nonRelativeChange.setResourceAccessor(new ClassLoaderResourceAccessor());

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
        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());
        //refactoring.setFileOpener(new JUnitResourceAccessor());

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
        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());
        //refactoring.setFileOpener(new JUnitResourceAccessor());

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
        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());
        //refactoring.setFileOpener(new JUnitResourceAccessor());

        refactoring.setCommentLineStartsWith("#");
        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setFile("liquibase/change/core/sample.data1-removedComments.csv");
        refactoring.setCommentLineStartsWith(""); //disable comments just in case
        String md5sum2 = refactoring.generateCheckSum().toString();

        then:
        assert md5sum1.equals(md5sum2)
    }
}
