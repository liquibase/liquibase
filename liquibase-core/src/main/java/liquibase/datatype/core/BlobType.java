package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="blob", aliases = {"longblob", "longvarbinary", "java.sql.Types.BLOB", "java.sql.Types.LONGBLOB", "java.sql.Types.LONGVARBINARY", "varbinary"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BlobType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof CacheDatabase || database instanceof H2Database || database instanceof HsqlDatabase) {
            return new DatabaseDataType("LONGVARBINARY");
        }
        if (database instanceof MaxDBDatabase) {
            return new DatabaseDataType("LONG BYTE");
        }
        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType("VARBINARY", "MAX");
        }
        if (database instanceof MySQLDatabase) {
            return new DatabaseDataType("LONGBLOB");
        }
        if (database instanceof PostgresDatabase) {
            return new DatabaseDataType("BYTEA");
        }
        if (database instanceof SybaseASADatabase) {
            return new DatabaseDataType("LONG BINARY");
        }
        if (database instanceof SybaseDatabase) {
            return new DatabaseDataType("IMAGE");
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("BLOB");
        }

        if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("BLOB");
        }
        return super.toDatabaseDataType(database);
    }

    //sqlite
    //        } else if (columnTypeString.toLowerCase(Locale.ENGLISH).contains("blob") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("binary")) {
//            type = new BlobType("BLOB");

}
