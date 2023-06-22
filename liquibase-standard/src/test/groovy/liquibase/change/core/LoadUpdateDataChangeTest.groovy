package liquibase.change.core

import liquibase.ChecksumVersion
import liquibase.Scope
import liquibase.change.ChangeStatus
import liquibase.database.core.PostgresDatabase
import liquibase.database.DatabaseConnection
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.SqlStatement
import liquibase.statement.core.InsertOrUpdateStatement
import liquibase.database.core.MSSQLDatabase
import spock.lang.Unroll

public class LoadUpdateDataChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("FILE_NAME");

        then:
        "Data loaded from 'FILE_NAME' into table 'TABLE_NAME'" == refactoring.getConfirmationMessage()
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

    @Unroll
    def "generateChecksum produces different values with each field - #version"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");

        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setFile("liquibase/change/core/sample.data2.csv");
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

    @Unroll
    def "checksum does not change when no comments in CSV and comment property changes"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");
        //refactoring.setFileOpener(new JUnitResourceAccessor());

        refactoring.setCommentLineStartsWith("") //comments disabled
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setCommentLineStartsWith("#");
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
    def "checksum changes when there are comments in CSV"(ChecksumVersion version, String originalChecksum, String updatedChecksum) {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-withComments.csv");

        refactoring.setCommentLineStartsWith("") //comments disabled
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setCommentLineStartsWith("#");
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
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-withComments.csv");

        refactoring.setCommentLineStartsWith("#");
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setFile("liquibase/change/core/sample.data1-removedComments.csv");
        refactoring.setCommentLineStartsWith(""); //disable comments just in case
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
}
