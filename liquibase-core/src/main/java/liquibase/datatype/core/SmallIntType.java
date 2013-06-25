package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="smallint", aliases = "java.sql.Types.SMALLINT", minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class SmallIntType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof DB2Database || database instanceof DerbyDatabase || database instanceof FirebirdDatabase || database instanceof MSSQLDatabase) {
            return new DatabaseDataType("SMALLINT"); //always smallint regardless of parameters passed
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }
        if (value instanceof Boolean)
            return Boolean.TRUE.equals(value) ? "1" : "0";
        else
            return value.toString();
    }

}
