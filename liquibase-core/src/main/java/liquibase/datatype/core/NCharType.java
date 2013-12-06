package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="nchar", aliases = { "java.sql.Types.NCHAR", "nchar2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NCharType extends CharType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof HsqlDatabase) {
            return new DatabaseDataType("CHAR", getParameters());
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NCHAR", getParameters());
        }
        return super.toDatabaseDataType(database);
    }

}