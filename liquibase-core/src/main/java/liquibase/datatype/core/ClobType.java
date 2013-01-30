package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="clob", aliases = {"text", "longtext", "java.sql.Types.CLOB"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class ClobType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof CacheDatabase) {
            return new DatabaseDataType("LONGVARCHAR");
        }   else if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("BLOB SUB_TYPE TEXT");
        } else if (database instanceof MaxDBDatabase || database instanceof SybaseASADatabase) {
            return new DatabaseDataType("LONG VARCHAR");
        } else if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType("NVARCHAR", "MAX");
        } else if (database instanceof MySQLDatabase) {
            return new DatabaseDataType("LONGTEXT");
        } else if (database instanceof PostgresDatabase || database instanceof SQLiteDatabase || database instanceof SybaseDatabase) {
            return new DatabaseDataType("TEXT");
        } else if (database instanceof OracleDatabase || database instanceof H2Database || database instanceof HsqlDatabase) {
            return new DatabaseDataType("CLOB");
        }
        return super.toDatabaseDataType(database);
    }


    //sqlite
    //        } else if (columnTypeString.equals("TEXT") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("uuid") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("uniqueidentifier") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).equals("uniqueidentifier") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).equals("datetime") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("timestamp") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("char") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("clob") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("text")) {
//            type = new CustomType("TEXT",0,0);

}
