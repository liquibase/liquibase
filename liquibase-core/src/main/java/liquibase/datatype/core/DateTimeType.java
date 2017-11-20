package liquibase.datatype.core;

import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.util.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@DataTypeInfo(name = "datetime", aliases = {"java.sql.Types.DATETIME", "java.util.Date", "smalldatetime", "datetime2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DateTimeType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        boolean allowFractional = supportsFractionalDigits(database);
        if (database instanceof AbstractDb2Database
                || database instanceof DerbyDatabase
                || database instanceof FirebirdDatabase
                || database instanceof H2Database
                || database instanceof HsqlDatabase) {
            return new DatabaseDataType("TIMESTAMP");
        }

        if (database instanceof OracleDatabase) {
            if (getRawDefinition().toUpperCase().contains("TIME ZONE")) {
                // remove the last data type size that comes from column size
                return new DatabaseDataType(getRawDefinition().replaceFirst("\\(\\d+\\)$", ""));
            }
            return new DatabaseDataType("TIMESTAMP", getParameters());
        }

        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (originalDefinition.toLowerCase().startsWith("smalldatetime")
                    || originalDefinition.toLowerCase().startsWith("[smalldatetime")) {

                return new DatabaseDataType(database.escapeDataTypeName("smalldatetime"));
            } else if (originalDefinition.equalsIgnoreCase("datetime2")
                    || originalDefinition.equals("[datetime2]")
                    || originalDefinition.matches("(?i)datetime2\\s*\\(.+")
                    || originalDefinition.matches("\\[datetime2\\]\\s*\\(.+")) {

                try {
                    if (database.getDatabaseMajorVersion() <= 9) { //2005 or earlier
                        return new DatabaseDataType(database.escapeDataTypeName("datetime"));
                    }
                } catch (DatabaseException ignore) { } //assuming it is a newer version

                if (parameters.length == 0) {
                    parameters = new Object[] { 7 };
                } else if (parameters.length > 1) {
                    parameters = new Object[] {parameters[1]};
                }
                return new DatabaseDataType(database.escapeDataTypeName("datetime2"), parameters);
            }
            return new DatabaseDataType(database.escapeDataTypeName("datetime"));
        }
        if (database instanceof InformixDatabase) {

          // From database to changelog
          if (getAdditionalInformation() == null || getAdditionalInformation().length() == 0) {
            if (getParameters() != null && getParameters().length > 0) {

              String parameter = String.valueOf(getParameters()[0]);
              
              if("4365".equals(parameter)) {
                return new DatabaseDataType("DATETIME YEAR TO FRACTION(3)");
              }

              if("3594".equals(parameter)) {
                return new DatabaseDataType("DATETIME YEAR TO SECOND");
              }

              if("3080".equals(parameter)) {
                return new DatabaseDataType("DATETIME YEAR TO MINUTE");
              }

              if("2052".equals(parameter)) {
                return new DatabaseDataType("DATETIME YEAR TO DAY");
              }
            }
          }

          // From changelog to the database
          if (getAdditionalInformation() != null && getAdditionalInformation().length() > 0) {
            return new DatabaseDataType(originalDefinition);
          }

          return new DatabaseDataType("DATETIME YEAR TO FRACTION", 5);
        }
        if (database instanceof PostgresDatabase) {
            String rawDefinition = originalDefinition.toLowerCase();
            Object[] params = getParameters();
            if (rawDefinition.contains("tz") || rawDefinition.contains("with time zone")) {
                if (params.length == 0 || !allowFractional) {
                    return new DatabaseDataType("TIMESTAMP WITH TIME ZONE");
                } else {
                    Object param = params[0];
                    if (params.length == 2) {
                        param = params[1];
                    }
                    return new DatabaseDataType("TIMESTAMP(" + param + ") WITH TIME ZONE");
                }
            } else {
                if (params.length == 0 || !allowFractional) {
                    return new DatabaseDataType("TIMESTAMP WITHOUT TIME ZONE");
                } else {
                    Object param = params[0];
                    if (params.length == 2) {
                        param = params[1];
                    }
                    return new DatabaseDataType("TIMESTAMP(" + param + ") WITHOUT TIME ZONE");
                }
            }
        }
        if (database instanceof SQLiteDatabase) {
            return new DatabaseDataType("TEXT");
        }

        if (database instanceof MySQLDatabase) {
            if (getParameters().length == 0 || !allowFractional) {
                // fast out...
                return new DatabaseDataType(getName());
            }

            Object[] params = getParameters();
            Integer precision = Integer.valueOf(params[0].toString());
            if (precision > 6) {
                LogFactory.getInstance().getLog().warning(
                        "MySQL does not support a timestamp precision"
                                + " of '" + precision + "' - resetting to"
                                + " the maximum of '6'");
                params = new Object[] {6};
            }
            return new DatabaseDataType(getName(), params);
        }

        return new DatabaseDataType(getName());
    }

    protected boolean supportsFractionalDigits(Database database) {
        if (database.getConnection() == null) {
            // if no connection is there we cannot do anything...
            LogFactory.getInstance().getLog().warning(
                    "No database connection available - specified"
                            + " DATETIME/TIMESTAMP precision will be tried");
            return true;
        }

        try {
            String minimumVersion = "0";
            int major = database.getDatabaseMajorVersion();
            int minor = database.getDatabaseMinorVersion();
            int patch = 0;

            if (MySQLDatabase.class.isInstance(database)) {
                patch = ((MySQLDatabase) database).getDatabasePatchVersion();

                // MySQL 5.6.4 introduced fractional support...
                minimumVersion = "5.6.4";
            } else if (PostgresDatabase.class.isInstance(database)) {
                // PostgreSQL 7.2 introduced fractional support...
                minimumVersion = "7.2";
            }

            return isMinimumVersion(minimumVersion, major, minor, patch);
        } catch (DatabaseException x) {
            LogFactory.getInstance().getLog().warning(
                    "Unable to determine exact database server version"
                            + " - specified TIMESTAMP precision"
                            + " will not be set: ", x);
            return false;
        }
    }

    protected boolean isMinimumVersion(String minimumVersion, int major, int minor, int patch) {
        String[] parts = minimumVersion.split("\\.", 3);
        int minMajor = Integer.parseInt(parts[0]);
        int minMinor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int minPatch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        
        if (minMajor > major) {
            return false;
        }

        if (minMajor == major && minMinor > minor) {
            return false;
        }

        return !(minMajor == major && minMinor == minor && minPatch > patch);
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

        if (database instanceof AbstractDb2Database) {
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
            String[] genericFormats = new String[] {"yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss" };

            for (String format : genericFormats) {
                try {
                    return new Timestamp(new SimpleDateFormat(format).parse(value).getTime());
                } catch (ParseException ignore) {
                    //doesn't match
                }
            }

            if (value.contains("/") || value.contains("-")) { //maybe a custom format the database expects. Just return it.
                return value;
            }

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

        if (database instanceof AbstractDb2Database) {
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
