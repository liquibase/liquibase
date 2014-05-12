package liquibase.change.core

import liquibase.change.Change
import liquibase.change.ChangeFactory
import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement
import spock.lang.Unroll;

import static org.junit.Assert.*;

import liquibase.test.JUnitResourceAccessor;
import org.junit.Test;

public class LoadDataChangeTest extends StandardChangeTest {


    def loadDataEmpty() throws Exception {
        when:
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/empty.data.csv");
        refactoring.setSeparator(",");

        refactoring.setResourceAccessor(new JUnitResourceAccessor());

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());

        then:
        sqlStatements.length == 0
    }

    @Unroll("multiple formats with the same data for #fileName")
    def "multiple formats with the same data"() throws Exception {
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

    def generateStatement_excel() throws Exception {
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
        change.load(new ParsedNode(null, "loadData")
                .addChildren([
                [column: [name: "id"]],
                [column: [name: "new_col", header:"new_col_header"]],
        ]), resourceSupplier.simpleResourceAccessor)

        then:
        change.columns.size() == 2
        change.columns[0].name == "id"
        change.columns[0].header == null

        change.columns[1].name == "new_col"
        change.columns[1].header == "new_col_header"
    }
}