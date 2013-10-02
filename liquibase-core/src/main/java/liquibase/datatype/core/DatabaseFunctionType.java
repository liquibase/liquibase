package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

@DataTypeInfo(name="function", aliases = "liquibase.statement.DatabaseFunction", minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DatabaseFunctionType extends LiquibaseDataType {

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null  || value.toString().equalsIgnoreCase("null"))  {
            return null;
        }
        if (value instanceof DatabaseFunction) {
            return database.generateDatabaseFunctionValue((DatabaseFunction) value);
        }


        return value.toString();
    }
}
