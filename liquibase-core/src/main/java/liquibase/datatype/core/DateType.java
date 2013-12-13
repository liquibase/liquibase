package liquibase.datatype.core;

import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.database.Database;

@DataTypeInfo(name="date", aliases = {"java.sql.Types.DATE", "java.sql.Date"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DateType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MSSQLDatabase) {
            try {
                if (database.getDatabaseMajorVersion() <= 9) { //2005 or earlier
                    return new DatabaseDataType("SMALLDATETIME");
                }
            } catch (DatabaseException ignore) { } //assuming it is a newer version

        }
        return new DatabaseDataType(getName());
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        } else if (value instanceof DatabaseFunction) {
            return ((DatabaseFunction) value).getValue();
        } else if (value.toString().equals("CURRENT_TIMESTAMP()")) {
              return database.getCurrentDateTimeFunction();
        } else if (value instanceof java.sql.Timestamp) {
            return database.getDateLiteral(((java.sql.Timestamp) value));
        } else if (value instanceof java.sql.Date) {
            return database.getDateLiteral(((java.sql.Date) value));
        } else if (value instanceof java.sql.Time) {
            return database.getDateLiteral(((java.sql.Time) value));
        } else if (value instanceof java.util.Date) {
            return database.getDateLiteral(((java.util.Date) value));
        } else {
            return "'"+((String) value).replaceAll("'","''")+"'";
        }
    }


    @Override
    public Object sqlToObject(String value, Database database) {
        if (database instanceof DB2Database) {
            return value.replaceFirst("^\"SYSIBM\".\"DATE\"\\('", "").replaceFirst("'\\)", "");
        }
        if (database instanceof DerbyDatabase) {
            return value.replaceFirst("^DATE\\('", "").replaceFirst("'\\)", "");
        }

        return super.sqlToObject(value, database);
    }
}
