package liquibase.datatype.core;

import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;
import liquibase.database.Database;

@DataTypeInfo(name="time", aliases = {"java.sql.Types.TIME", "java.sql.Time"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class TimeType  extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof InformixDatabase) {
            return new DatabaseDataType("INTERVAL HOUR TO FRACTION", 5);
        }
        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType("DATETIME");
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("DATE");
        }
        return new DatabaseDataType(getName());
    }

    @Override
    public String objectToString(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }  else if (value instanceof DatabaseFunction) {
            return ((DatabaseFunction) value).getValue();
        } else if (value.toString().equals("CURRENT_TIMESTAMP()")) {
              return database.getCurrentDateTimeFunction();
        } else if (value instanceof java.sql.Time) {
            return database.getTimeLiteral(((java.sql.Time) value));
        } else {
            return "'"+((String) value).replaceAll("'","''")+"'";
        }
    }

    @Override
    public Object stringToObject(String value, Database database) {
        if (database instanceof DB2Database) {
            return value.replaceFirst("^\"SYSIBM\".\"TIME\"\\('", "").replaceFirst("'\\)", "");
        }
        if (database instanceof DerbyDatabase) {
            return value.replaceFirst("^TIME\\('", "").replaceFirst("'\\)", "");
        }

        return super.stringToObject(value, database);
    }

}
