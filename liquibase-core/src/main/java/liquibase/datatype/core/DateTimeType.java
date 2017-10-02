package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@DataTypeInfo(name = "datetime", aliases = {"java.sql.Types.DATETIME", "java.util.Date", "smalldatetime", "datetime2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DateTimeType extends LiquibaseDataType {

    public static final int PRACITCALLY_INFINITE_FRACTIONAL_DIGITS = 99;

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        int maxFractionalDigits = database.getMaxFractionalDigitsForTimestamp();

        if ((database instanceof DerbyDatabase) || (database instanceof FirebirdDatabase) || (database instanceof
            H2Database) || (database instanceof HsqlDatabase)) {
            return new DatabaseDataType("TIMESTAMP");
        }

        if (database instanceof DB2Database) {
            return new DatabaseDataType("TIMESTAMP", getParameters());
		}

        if (database instanceof OracleDatabase) {
            if (getRawDefinition().toUpperCase().contains("TIME ZONE")) {
                return new DatabaseDataType(getRawDefinition().replaceFirst("\\(11\\)$", ""));
            }
            return new DatabaseDataType("TIMESTAMP", getParameters());
        }

        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (originalDefinition.toLowerCase().startsWith("smalldatetime")
                    || originalDefinition.toLowerCase().startsWith("[smalldatetime")) {

                return new DatabaseDataType(database.escapeDataTypeName("smalldatetime"));
            } else if ("datetime2".equalsIgnoreCase(originalDefinition)
                    || "[datetime2]".equals(originalDefinition)
                    || originalDefinition.matches("(?i)datetime2\\s*\\(.+")
                    || originalDefinition.matches("\\[datetime2\\]\\s*\\(.+")) {

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
          if ((getAdditionalInformation() == null) || getAdditionalInformation().isEmpty()) {
            if ((getParameters() != null) && (getParameters().length > 0)) {

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
          if ((getAdditionalInformation() != null) && !getAdditionalInformation().isEmpty()) {
            return new DatabaseDataType(originalDefinition);
          }

          return new DatabaseDataType("DATETIME YEAR TO FRACTION", 5);
        }
        if (database instanceof PostgresDatabase) {
            String rawDefinition = originalDefinition.toLowerCase();
            Object[] params = getParameters();
            if (rawDefinition.contains("tz") || rawDefinition.contains("with time zone")) {
                if (params.length == 0 ) {
                    return new DatabaseDataType("TIMESTAMP WITH TIME ZONE");
                } else {
                    Object param = params[0];
                    if (params.length == 2) {
                        param = params[1];
                    }
                    return new DatabaseDataType("TIMESTAMP(" + param + ") WITH TIME ZONE");
                }
            } else {
                if (params.length == 0 ) {
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
            if ((getParameters().length == 0) || (maxFractionalDigits == 0)) {
                // fast out...
                return new DatabaseDataType(getName());
            }

            Object[] params = getParameters();
            Integer precision = Integer.valueOf(params[0].toString());
            if (precision > 6) {
                LogService.getLog(getClass()).warning(
                        LogType.LOG, "MySQL does not support a timestamp precision"
                                + " of '" + precision + "' - resetting to"
                                + " the maximum of '6'");
                params = new Object[] {6};
            }
            return new DatabaseDataType(getName(), params);
        }

        return new DatabaseDataType(getName());
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equalsIgnoreCase(value.toString())) {
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

            if ((database instanceof OracleDatabase) && value.matches("to_date\\('\\d+\\-\\d+\\-\\d+ \\d+:\\d+:\\d+'," +
                " 'YYYY\\-MM\\-DD HH24:MI:SS'\\)")) {
                dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
                value = value.replaceFirst(".*?'", "").replaceFirst("',.*","");
            }

            if ((database instanceof HsqlDatabase) && value.matches("TIMESTAMP'\\d+\\-\\d+\\-\\d+ \\d+:\\d+:\\d+(?:\\" +
                ".\\d+)?'")) {
                dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:s.S");
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

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.DATE;
    }

    private boolean zeroTime(String stringVal) {
        return "".equals(stringVal.replace("-", "").replace(":", "").replace(" ", "").replace("0", ""));
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
}
