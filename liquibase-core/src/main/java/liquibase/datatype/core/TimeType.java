package liquibase.datatype.core;

import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.database.Database;
import liquibase.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

@DataTypeInfo(name="time", aliases = {"java.sql.Types.TIME", "java.sql.Time", "timetz"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class TimeType  extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof InformixDatabase) {
            return new DatabaseDataType("INTERVAL HOUR TO FRACTION", 5);
        }
        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            try {
                if (database.getDatabaseMajorVersion() <= 9) {
                    return new DatabaseDataType(database.escapeDataTypeName("datetime"));
                }
            } catch (DatabaseException e) {
                //assume greater than sql 2008 and TIME will work
            }
            if (parameters.length == 0) {
                parameters = new Object[] { 7 };
            } else if (parameters.length > 2) {
                parameters = Arrays.copyOfRange(parameters, 0, 2);
            }
            return new DatabaseDataType(database.escapeDataTypeName("time"), parameters);
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
            if (supportsParameters && getParameters().length > 0 && Integer.parseInt(getParameters()[0].toString()) <= 6) {
                return new DatabaseDataType(getName(), getParameters());
            } else {
                return new DatabaseDataType(getName());
            }
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("DATE");
        }

        if (database instanceof PostgresDatabase) {
            String rawDefinition = originalDefinition.toLowerCase();
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

        if (database instanceof AbstractDb2Database) {
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
        if (database instanceof AbstractDb2Database) {
            return new SimpleDateFormat("HH.mm.ss");
        }
        return new SimpleDateFormat("HH:mm:ss");
    }


}
