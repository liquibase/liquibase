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

@DataTypeInfo(name = "datetime", aliases = {"java.sql.Types.DATETIME", "java.util.Date", "smalldatetime", "datetime2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DateTimeType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof DB2Database
                || database instanceof DerbyDatabase
                || database instanceof FirebirdDatabase
                || database instanceof H2Database
                || database instanceof HsqlDatabase
                || database instanceof OracleDatabase) {
            return new DatabaseDataType("TIMESTAMP");
        }

        if (database instanceof MSSQLDatabase && getParameters().length > 0 && "16".equals(getParameters()[0])) {
            return new DatabaseDataType("SMALLDATETIME");
        }
        if (database instanceof InformixDatabase) {
            return new DatabaseDataType("DATETIME YEAR TO FRACTION", 5);
        }
        if (database instanceof PostgresDatabase) {
            return new DatabaseDataType("TIMESTAMP WITH TIME ZONE");
        }
        if (database instanceof SQLiteDatabase) {
            return new DatabaseDataType("TEXT");
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
            if (supportsParameters) {
                return new DatabaseDataType(getName(), getParameters());
            } else {
                return new DatabaseDataType(getName());
            }
        }

        return new DatabaseDataType(getName());
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        } else if (value instanceof DatabaseFunction) {
            return database.generateDatabaseFunctionValue((DatabaseFunction) value);
        } else if (database.isFunction(value.toString())) {
            return value.toString();
        } else if (value instanceof String) {
            return "'" + ((String) value).replaceAll("'", "''") + "'";
        }
        return database.getDateTimeLiteral(((java.sql.Timestamp) value));
    }

    @Override
    public Object sqlToObject(String value, Database database) {
        if (zeroTime(value)) {
            return value;
        }

        if (database instanceof DB2Database) {
            return value.replaceFirst("^\"SYSIBM\".\"TIMESTAMP\"\\('", "").replaceFirst("'\\)", "");
        }
        if (database instanceof DerbyDatabase) {
            return value.replaceFirst("^TIMESTAMP\\('", "").replaceFirst("'\\)", "");
        }

        try {
            DateFormat dateTimeFormat = getDateTimeFormat(database);

            if (database instanceof OracleDatabase && value.matches("to_date\\('\\d+\\-\\d+\\-\\d+ \\d+:\\d+:\\d+', 'YYYY\\-MM\\-DD HH24:MI:SS'\\)")) {
                dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
                value = value.replaceFirst(".*?'", "").replaceFirst("',.*","");
            }

            return new Timestamp(dateTimeFormat.parse(value).getTime());
        } catch (ParseException e) {
            return new DatabaseFunction(value);
        }
    }

    private boolean zeroTime(String stringVal) {
        return stringVal.replace("-","").replace(":", "").replace(" ","").replace("0","").equals("");
    }

    protected DateFormat getDateTimeFormat(Database database) {
        if (database instanceof MySQLDatabase) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //no ms in mysql
        }
        if (database instanceof MSSQLDatabase) {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); //no ms in mysql
        }

        if (database instanceof DB2Database) {
            return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    //oracle
//    @Override
//    public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
//        if (defaultValue != null) {
//            if (defaultValue instanceof String) {
//                if (dataType == Types.DATE || dataType == Types.TIME || dataType == Types.TIMESTAMP) {
//                    if (((String) defaultValue).indexOf("YYYY-MM-DD HH") > 0) {
//                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('", "").replaceFirst("', 'YYYY-MM-DD HH24:MI:SS'\\)$", "");
//                    } else if (((String) defaultValue).indexOf("YYYY-MM-DD") > 0) {
//                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('", "").replaceFirst("', 'YYYY-MM-DD'\\)$", "");
//                    } else {
//                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('", "").replaceFirst("', 'HH24:MI:SS'\\)$", "");
//                    }
//                } else if (
//                        dataType == Types.BIGINT ||
//                                dataType == Types.NUMERIC ||
//                                dataType == Types.BIT ||
//                                dataType == Types.SMALLINT ||
//                                dataType == Types.DECIMAL ||
//                                dataType == Types.INTEGER ||
//                                dataType == Types.TINYINT ||
//                                dataType == Types.FLOAT ||
//                                dataType == Types.REAL
//                        ) {
//                    /*
//                         * if dataType is numeric-type then cut "(" , ")" symbols
//                         * Cause: Column's default value option may be set by both ways:
//                         * DEFAULT 0
//                         * DEFAULT (0)
//                         * */
//                    defaultValue = ((String) defaultValue).replaceFirst("\\(", "").replaceFirst("\\)", "");
//                }
//                defaultValue = ((String) defaultValue).replaceFirst("'\\s*$", "'"); //sometimes oracle adds an extra space after the trailing ' (see http://sourceforge.net/tracker/index.php?func=detail&aid=1824663&group_id=187970&atid=923443).
//            }
//        }
//        return super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);
//    }







    //postgres
//    @Override
//    public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
//        if (defaultValue != null) {
//            if (defaultValue instanceof String) {
//                defaultValue = ((String) defaultValue).replaceAll("'::[\\w\\s]+$", "'");
//
//                if (dataType == Types.DATE || dataType == Types.TIME || dataType == Types.TIMESTAMP) {
//                    //remove trailing time zone info
//                    defaultValue = ((String) defaultValue).replaceFirst("-\\d+$", "");
//                }
//            }
//        }
//        return super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);
//
//    }

}
