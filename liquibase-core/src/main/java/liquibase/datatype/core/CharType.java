package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtils;

@DataTypeInfo(name="char", aliases = "java.sql.Types.CHAR", minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class CharType extends LiquibaseDataType {

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }

        if (value instanceof DatabaseFunction) {
            return value.toString();
        }

        String val = String.valueOf(value);
        if (database instanceof MSSQLDatabase && !StringUtils.isAscii(val)) {
            return "N'"+database.escapeStringForDatabase(val)+"'";
        }

        return "'"+database.escapeStringForDatabase(val)+"'";
    }

    /**
     * Return the size of this data type definition. If unknown or unspecified, return -1
     */
    protected int getSize() {
        if (getParameters().length == 0) {
            return -1;
        }

        if (getParameters()[0] instanceof String) {
            return Integer.valueOf((String) getParameters()[0]);
        }

        if (getParameters()[0] instanceof Number) {
            return ((Number) getParameters()[0]).intValue();
        }

        return -1;
    }


}
