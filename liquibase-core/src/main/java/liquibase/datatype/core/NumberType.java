package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="number", aliases = {"numeric", "java.sql.Types.NUMERIC"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NumberType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MySQLDatabase || database instanceof DB2Database|| database instanceof MSSQLDatabase
                || database instanceof HsqlDatabase || database instanceof DerbyDatabase || database instanceof PostgresDatabase || database instanceof FirebirdDatabase) {
            return new DatabaseDataType("numeric", getParameters());
        }
        return super.toDatabaseDataType(database);
    }
}
