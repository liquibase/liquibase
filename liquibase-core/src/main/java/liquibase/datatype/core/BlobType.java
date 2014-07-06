package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtils;

@DataTypeInfo(name="blob", aliases = {"longblob", "longvarbinary", "java.sql.Types.BLOB", "java.sql.Types.LONGBLOB", "java.sql.Types.LONGVARBINARY", "java.sql.Types.VARBINARY", "java.sql.Types.BINARY", "varbinary"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BlobType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());

        if (database instanceof H2Database || database instanceof HsqlDatabase) {
            if (originalDefinition.toLowerCase().startsWith("longvarbinary") || originalDefinition.startsWith("java.sql.Types.LONGVARBINARY")) {
                return new DatabaseDataType("LONGVARBINARY");
            } else {
                return new DatabaseDataType("BLOB");
            }
        }

        if (database instanceof MSSQLDatabase) {
            String param = "MAX";
            if (this.getParameters().length > 0) {
                param = this.getParameters()[0].toString();
            }
            if (param.equals("2147483647")) {
                param = "MAX";
            }
            return new DatabaseDataType("VARBINARY", param);
        }
        if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase().startsWith("blob") || originalDefinition.equals("java.sql.Types.BLOB")) {
                return new DatabaseDataType("BLOB");
            } else if (originalDefinition.toLowerCase().startsWith("varbinary") || originalDefinition.equals("java.sql.Types.VARBINARY")) {
                return new DatabaseDataType("VARBINARY", getParameters());
            } else {
                return new DatabaseDataType("LONGBLOB");
            }
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
