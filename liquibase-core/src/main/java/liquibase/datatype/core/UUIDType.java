package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;

@DataTypeInfo(name="uuid", aliases = {"uniqueidentifier"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class UUIDType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        try {
            if (database instanceof H2Database
                    || (database instanceof PostgresDatabase && database.getDatabaseMajorVersion() * 10 + database.getDatabaseMinorVersion() >= 83)) {
                return new DatabaseDataType("UUID");
            }
        } catch (DatabaseException e) {
            // fall back
        }

        if (database instanceof MSSQLDatabase || database instanceof SybaseASADatabase || database instanceof SybaseDatabase) {
            return new DatabaseDataType("UNIQUEIDENTIFIER");
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("RAW",16);
        }
        if (database instanceof SQLiteDatabase) {
            return new DatabaseDataType("TEXT");
        }
        return new DatabaseDataType("char", 36);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (database instanceof MSSQLDatabase) {
            return "'"+value+"'";
        }
        return super.objectToSql(value, database);
    }
}
