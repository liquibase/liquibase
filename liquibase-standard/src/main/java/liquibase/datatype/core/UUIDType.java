package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;

import java.util.Locale;

@DataTypeInfo(name = "uuid", aliases = { "uniqueidentifier", "java.util.UUID" }, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class UUIDType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        try {
            if (database instanceof H2Database
                    || (database instanceof PostgresDatabase && database.getDatabaseMajorVersion() * 10 + database.getDatabaseMinorVersion() >= 83)
                    || (database instanceof HsqlDatabase && database.getDatabaseMajorVersion() * 10 + database.getDatabaseMinorVersion() >= 24)) {
                return new DatabaseDataType("UUID");
            }
        } catch (DatabaseException e) {
            // fall back
        }

        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("uniqueidentifier"));
        }
        if ((database instanceof SybaseASADatabase) || (database instanceof SybaseDatabase)) {
            return new DatabaseDataType("UNIQUEIDENTIFIER");
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("RAW",16);
        }
        if (database instanceof SQLiteDatabase) {
            return new DatabaseDataType("TEXT");
        }
        try {
            if(database instanceof MariaDBDatabase && ((database.getDatabaseMajorVersion() == 10 && database.getDatabaseMinorVersion() >= 7)
                    || database.getDatabaseMajorVersion() >= 11)) {
                return new DatabaseDataType("UUID");
            }
        } catch (DatabaseException e) {
            try {
                throw new DatabaseException("UUID data type is not supported in versions lower than 10.7");
            } catch (DatabaseException ex) {
                throw new RuntimeException(ex);
            }
        }
//        try {
//            if (database instanceof MySQLDatabase && (database.getDatabaseMajorVersion() == 8)) {
//                return new DatabaseDataType("BINARY", 16);
//            }
//        } catch (DatabaseException e) {
//            throw new RuntimeException(e);
//        }
        if (database instanceof MySQLDatabase) {
            return new DatabaseDataType("binary", 16);
        }
        return new DatabaseDataType("char", 36);
    }

    @Override
    protected String otherToSql(Object value, Database database) {
        if (value == null) {
            return null;
        }
        if (database instanceof MSSQLDatabase) {
            return "'" + value.toString().toUpperCase(Locale.ENGLISH) + "'";
        }
        // MYSQL displays binary(16) uuids as lowercase: https://dev.mysql.com/blog-archive/mysql-8-0-uuid-support/
        if (database instanceof MySQLDatabase && !(database instanceof MariaDBDatabase)) {
            return value.toString().toLowerCase(Locale.ENGLISH);
        }
        return super.otherToSql(value, database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.UUID;
    }

}
