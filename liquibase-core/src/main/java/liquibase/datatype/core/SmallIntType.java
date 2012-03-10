package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="smallint", aliases = "java.sql.Types.SMALLINT", minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class SmallIntType extends LiquibaseDataType {

    @Override
    public String objectToString(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }
        if (value instanceof Boolean)
            return Boolean.TRUE.equals(value) ? "1" : "0";
        else
            return value.toString();
    }

}
