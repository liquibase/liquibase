package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="function", aliases = "liquibase.statement.DatabaseFunction", minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DatabaseFunctionType extends LiquibaseDataType {

    @Override
    public String objectToString(Object value, Database database) {
        if (value == null  || value.toString().equalsIgnoreCase("null"))  {
            return null;
        }
        if (value.toString().equalsIgnoreCase("CURRENT_TIMESTAMP()") || value.toString().equalsIgnoreCase("NOW()")) {
            return database.getCurrentDateTimeFunction();
        }


        return value.toString();
    }
}
