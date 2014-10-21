package liquibase.datatype.core;

import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.database.Database;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@DataTypeInfo(name="time", aliases = {"java.sql.Types.TIME", "java.sql.Time", "timetz"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class TimeType  extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof InformixDatabase) {
            return new DatabaseDataType("INTERVAL HOUR TO FRACTION", 5);
        }
        if (database instanceof MSSQLDatabase) {
            try {
                if (database.getDatabaseMajorVersion() <= 9) {
                    return new DatabaseDataType("DATETIME");
                }
            } catch (DatabaseException e) {
                //assume greater than sql 2008 and TIME will work
            }
        }

        if (database instanceof MySQLDatabase) {
            boolean supportsParameters = true;
            try {
                supportsParameters = database.getDatabaseMajorVersion() >= 5
                        && database.getDatabaseMinorVersion() >= 6
                        && ((MySQLDatabase) database).getDatabasePatchVersion() >= 4;
            } catch (Exception ignore) {
                //assume supports parameters
            }
            if (supportsParameters && getParameters().length > 0 && Integer.valueOf(getParameters()[0].toString()) <= 6) {
                return new DatabaseDataType(getName(), getParameters());
            } else {
                return new DatabaseDataType(getName());
            }
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("DATE");
        }

        if (database instanceof PostgresDatabase) {
            String rawDefinition = getRawDefinition().toLowerCase();
            if (rawDefinition.contains("tz") || rawDefinition.contains("with time zone")) {
                return new DatabaseDataType("TIME WITH TIME ZONE");
            } else {
                return new DatabaseDataType("TIME WITHOUT TIME ZONE");
            }
        }

        return new DatabaseDataType(getName());
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }  else if (value instanceof DatabaseFunction) {
            return database.generateDatabaseFunctionValue((DatabaseFunction) value);
        } else if (value instanceof java.sql.Time) {
            return database.getTimeLiteral(((java.sql.Time) value));
        } else {
            return "'"+((String) value).replaceAll("'","''")+"'";
        }
    }

    @Override
    public Object sqlToObject(String value, Database database) {
        if (zeroTime(value)) {
            return value;
        }

        if (database instanceof DB2Database) {
            return value.replaceFirst("^\"SYSIBM\".\"TIME\"\\('", "").replaceFirst("'\\)", "");
        }
        if (database instanceof DerbyDatabase) {
            return value.replaceFirst("^TIME\\('", "").replaceFirst("'\\)", "");
        }

        try {
            DateFormat timeFormat = getTimeFormat(database);

            if (database instanceof OracleDatabase && value.matches("to_date\\('\\d+:\\d+:\\d+', 'HH24:MI:SS'\\)")) {
                timeFormat = new SimpleDateFormat("HH:mm:s");
                value = value.replaceFirst(".*?'", "").replaceFirst("',.*","");
            }

            return new java.sql.Time(timeFormat.parse(value).getTime());
        } catch (ParseException e) {
            return new DatabaseFunction(value);
        }
    }

    private boolean zeroTime(String stringVal) {
        return stringVal.replace("-","").replace(":", "").replace(" ","").replace("0","").equals("");
    }

    protected DateFormat getTimeFormat(Database database) {
        if (database instanceof DB2Database) {
            return new SimpleDateFormat("HH.mm.ss");
        }
        return new SimpleDateFormat("HH:mm:ss");
    }


}
