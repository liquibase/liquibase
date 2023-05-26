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
import spock.lang.Unroll

import static org.junit.Assert.*

public class LoadUpdateDataChangeTest2 extends StandardChangeTest {

    @Unroll
    def "generateChecksum produces different values with each field - #version"(String originalChecksum, String updatedChecksum) {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");

        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setFile("liquibase/change/core/sample.data2.csv");
        String md5sum2 = refactoring.generateCheckSum().toString();

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        originalChecksum | updatedChecksum
        "8:a91f2379b2b3b4c4a5a571b8e7409081" | "8:cce1423feea9e29192ef7c306eda0c94"
    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false;
    }

    @Unroll
    def "checksum does not change when no comments in CSV and comment property changes"(String originalChecksum, String updatedChecksum) {
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
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        originalChecksum | updatedChecksum
        "8:a91f2379b2b3b4c4a5a571b8e7409081" | "8:a91f2379b2b3b4c4a5a571b8e7409081"
    }

    @Unroll
    def "checksum changes when there are comments in CSV"(String originalChecksum, String updatedChecksum) {
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
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        originalChecksum | updatedChecksum
        "8:becddfbcfda2ec516371ed36aaf1137a" | "8:e51a6408e921cfa151c50c7d90cf5baa"
    }

    @Unroll
    def "checksum same for CSV files with comments and file with removed comments manually - #version"(String originalChecksum, String updatedChecksum) {
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
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        originalChecksum | updatedChecksum
        "8:e51a6408e921cfa151c50c7d90cf5baa" | "8:e51a6408e921cfa151c50c7d90cf5baa"
    }
}
