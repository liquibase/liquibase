package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="nvarchar", aliases = {"java.sql.Types.NVARCHAR", "nvarchar2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NVarcharType extends CharType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof HsqlDatabase || database instanceof PostgresDatabase|| database instanceof DerbyDatabase) {
            return new DatabaseDataType("VARCHAR", getParameters());
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NVARCHAR2", getParameters());
        }
        if (database instanceof MSSQLDatabase) {
            if (getParameters() != null && getParameters().length > 0) {
                Object param1 = getParameters()[0];
                if (param1.toString().matches("\\d+")) {
                    if (Long.valueOf(param1.toString()) > 8000) {
                        return new DatabaseDataType("NVARCHAR", "MAX");
                    }
                }
            }
            return new DatabaseDataType("NVARCHAR", getParameters());
        }
        return super.toDatabaseDataType(database);
    }

}