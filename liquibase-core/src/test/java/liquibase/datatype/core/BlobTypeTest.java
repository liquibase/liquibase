package liquibase.datatype.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.core.*;
import liquibase.datatype.DatabaseDataType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.Assert.assertEquals;

public class BlobTypeTest {


    @ParameterizedTest
    @CsvSource({
            "varbinary,VARBINARY,",
            "varbinary(50),VARBINARY(50),50",
            "binary(50),BINARY(50),50",
            "java.sql.Types.VARBINARY,VARBINARY,",
            "blob, BLOB,"
    })
    public void toDatabaseDataType_should_succeed_for_DB2Database(String liquibaseType, String expectedDatabaseType, Integer max) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            if (max != null) {
                blobType.addParameter(max);
            }
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new DB2Database());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({
            "varbinary,VARBINARY,",
            "varbinary(50),VARBINARY(50),50",
            "binary(50),BINARY(50),50",
            "java.sql.Types.VARBINARY,VARBINARY,",
            "blob, BLOB,"
    })
    public void toDatabaseDataType_should_succeed_for_Db2zDatabase(String liquibaseType, String expectedDatabaseType, Integer max) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            if (max != null) {
                blobType.addParameter(max);
            }
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new Db2zDatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({
            "large object,BINARY LARGE OBJECT",
            "varbinary,VARBINARY",
            "java.sql.Types.VARBINARY,VARBINARY",
            "longvarbinary,LONGVARBINARY",
            "java.sql.Types.LONGVARBINARY,LONGVARBINARY",
            "binary,BINARY",
            "blob,BLOB",
    })
    public void toDatabaseDataType_should_succeed_for_H2Database(String liquibaseType, String expectedDatabaseType) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new H2Database());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({
            "large object,BINARY LARGE OBJECT,",
            "varbinary,VARBINARY,",
            "varbinary(40),VARBINARY(40),40",
            "java.sql.Types.VARBINARY,VARBINARY,",
            "longvarbinary,LONGVARBINARY,",
            "java.sql.Types.LONGVARBINARY,LONGVARBINARY,",
            "binary,BINARY,",
            "blob,BLOB,",
    })
    public void toDatabaseDataType_should_succeed_for_HsqlDatabase(String liquibaseType, String expectedDatabaseType, Integer max) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            if (max != null) {
                blobType.addParameter(max);
            }
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new HsqlDatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({
            "varbinary,varbinary(1),",
            "[varbinary],varbinary(1),",
            "varbinary(8000),varbinary(8000),8000",
            "[varbinary](8000),varbinary(8000),8000",
            "image,image,",
            "blob,varbinary(MAX),"
    })
    public void toDatabaseDataType_should_succeed_for_MSSQLDatabase(String liquibaseType, String expectedDatabaseType, Integer max) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            if (max != null) {
                blobType.addParameter(max);
            }
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new MSSQLDatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({
            "varbinary,VARBINARY,",
            "varbinary(8000),VARBINARY(8000),8000",
            "tinyblob,TINYBLOB,",
            "mediumblob,MEDIUMBLOB,",
            "binary,BINARY,",
            "binary(50),BINARY(50),50",
            "java.sql.Types.BLOB,BLOB,",
            "blob,BLOB,",
            "longblob,LONGBLOB,"
    })
    public void toDatabaseDataType_should_succeed_for_MySQLDatabase(String liquibaseType, String expectedDatabaseType, Integer max) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            if (max != null) {
                blobType.addParameter(max);
            }
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new MySQLDatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({
            "java.sql.Types.BLOB,OID,",
            "blob,OID,",
            "bytea,BYTEA,"
    })
    public void toDatabaseDataType_should_succeed_for_PostgresDatabase(String liquibaseType, String expectedDatabaseType, Integer max) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            if (max != null) {
                blobType.addParameter(max);
            }
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new PostgresDatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({"blob,LONG BINARY"})
    public void toDatabaseDataType_should_succeed_for_SybaseASADatabase(String liquibaseType, String expectedDatabaseType) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new SybaseASADatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({"blob,IMAGE"})
    public void toDatabaseDataType_should_succeed_SybaseDatabase(String liquibaseType, String expectedDatabaseType) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new SybaseDatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({"blob,BLOB"})
    public void toDatabaseDataType_should_succeed_for_FirebirdDatabase(String liquibaseType, String expectedDatabaseType) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new FirebirdDatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }

    @ParameterizedTest
    @CsvSource({
            "bfile,BFILE,",
            "raw,RAW,",
            "raw(300),RAW(300),300",
            "binary,RAW,",
            "varbinary,RAW,",
            "binary(300),RAW(300),300",
            "varbinary(300),RAW(300),300",
            "blob,BLOB,"
    })
    public void toDatabaseDataType_should_succeed_for_OracleDatabase(String liquibaseType, String expectedDatabaseType, Integer max) throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            BlobType blobType = new BlobType();
            if (max != null) {
                blobType.addParameter(max);
            }
            blobType.finishInitialization(liquibaseType);
            DatabaseDataType dbType = blobType.toDatabaseDataType(new OracleDatabase());

            assertEquals(expectedDatabaseType, dbType.getType());
        });
    }
}