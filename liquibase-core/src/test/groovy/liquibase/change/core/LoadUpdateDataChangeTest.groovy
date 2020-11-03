package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.database.core.PostgresDatabase
import liquibase.database.DatabaseConnection
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.SqlStatement
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.test.JUnitResourceAccessor
import liquibase.database.core.MSSQLDatabase


import static org.junit.Assert.*

public class LoadUpdateDataChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("FILE_NAME");

        then:
        "Data loaded from FILE_NAME into TABLE_NAME" == refactoring.getConfirmationMessage()
    }

	def "loadUpdateEmpty database agnostic"() throws Exception {
		when:
		LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
		refactoring.setSchemaName("SCHEMA_NAME");
		refactoring.setTableName("TABLE_NAME");
		refactoring.setFile("liquibase/change/core/empty.data.csv");
		refactoring.setSeparator(",");

		SqlStatement[] sqlStatement = refactoring.generateRollbackStatements(new MSSQLDatabase());
		
		then:
		sqlStatement.length == 0
	}

    def "loadUpdate generates InsertOrUpdateStatements"() throws Exception {
        when:
        MockDatabase database = new MockDatabase();
        database.setConnection((DatabaseConnection) null)

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");

        SqlStatement[] statements = change.generateStatements(database);

        then:
        assert statements != null
        assert statements[0] instanceof InsertOrUpdateStatement
        assert !statements[0].getOnlyUpdate()
    }

    def "loadUpdate generates InsertOrUpdateStatements for Postgres"() throws Exception {
        when:
        PostgresDatabase database = new PostgresDatabase();

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/jhi_text.csv");
        change.setResourceAccessor(new ClassLoaderResourceAccessor());

        LoadDataColumnConfig idConfig = new LoadDataColumnConfig();
        idConfig.setHeader("id");
        idConfig.setType("NUMERIC");
        change.addColumn(idConfig);

        LoadDataColumnConfig pickupConfig = new LoadDataColumnConfig();
        pickupConfig.setHeader("selected_pickup_date");
        pickupConfig.setType("DATETIME");
        change.addColumn(pickupConfig);

        LoadDataColumnConfig effectiveConfig = new LoadDataColumnConfig();
        effectiveConfig.setHeader("effective_pickup_date");
        effectiveConfig.setType("DATETIME");
        change.addColumn(effectiveConfig);

        LoadDataColumnConfig textConfig = new LoadDataColumnConfig();
        textConfig.setHeader("textfield");
        textConfig.setType("CLOB");
        change.addColumn(textConfig);

        SqlStatement[] statements = change.generateStatements(database);

        then:
        assert statements != null
        assert statements[0] instanceof InsertOrUpdateStatement
        assert !statements[0].getOnlyUpdate()
    }

    def "loadUpdate generates InsertOrUpdateStatements with onlyUpdate"() throws Exception {
        when:
        MockDatabase database = new MockDatabase();
        database.setConnection((DatabaseConnection) null)

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");
        change.setOnlyUpdate(true);

        SqlStatement[] statements = change.generateStatements(database);

        then:
        assert statements != null
        assert statements[0] instanceof InsertOrUpdateStatement
        assert statements[0].getOnlyUpdate()
    }

    def "generateChecksum produces different values with each field"() {
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");

        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setFile("liquibase/change/core/sample.data2.csv");
        String md5sum2 = refactoring.generateCheckSum().toString();

        assertTrue(!md5sum1.equals(md5sum2));
        assertEquals(md5sum2, refactoring.generateCheckSum().toString());

    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false;
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def change = new LoadUpdateDataChange()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check loadUpdateData status"
    }

    def "checksum does not change when no comments in CSV and comment property changes"() {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");
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
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
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
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
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
}
