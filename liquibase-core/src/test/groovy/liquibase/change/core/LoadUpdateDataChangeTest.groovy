package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.change.StandardChangeTest;
import liquibase.sdk.database.MockDatabase
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.SqlStatement
import liquibase.statement.core.InsertOrUpdateStatement;
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


    def "loadUpdate generates InsertOrUpdateStatements"() throws Exception {
        when:
        MockDatabase database = new MockDatabase();

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");
        change.setResourceAccessor(new ClassLoaderResourceAccessor());

        SqlStatement[] statements = change.generateStatements(database);

        then:
        assert statements != null
        assert statements[0] instanceof InsertOrUpdateStatement
    }

    def "generateChecksum produces different values with each field"() {
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");
        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());

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
}