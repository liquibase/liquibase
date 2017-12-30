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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DataTypeInfo(name = "datetime", minParameters = 0, maxParameters = 1,
    aliases = {"java.sql.Types.DATETIME", "java.util.Date", "smalldatetime", "datetime2"},
    priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DateTimeType extends LiquibaseDataType {

    protected static final String SQL_DATETYPE_TIMESTAMP = "TIMESTAMP";

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if ((database instanceof DerbyDatabase) || (database instanceof FirebirdDatabase) || (database instanceof
            H2Database) || (database instanceof HsqlDatabase)) {
            return new DatabaseDataType(SQL_DATETYPE_TIMESTAMP);
        }

        if (database instanceof DB2Database) {
            return new DatabaseDataType(SQL_DATETYPE_TIMESTAMP, getParameters());
		}

        if (database instanceof OracleDatabase) {
            if (getRawDefinition().toUpperCase().contains("TIME ZONE")) {
                return new DatabaseDataType(getRawDefinition().replaceFirst("\\(11\\)$", ""));
            }
            return new DatabaseDataType(SQL_DATETYPE_TIMESTAMP, getParameters());
        }

        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (originalDefinition.matches("(?i)^\\[?smalldatetime.*")) {
                return new DatabaseDataType(database.escapeDataTypeName("smalldatetime"));
            } else if ("datetime2".equalsIgnoreCase(originalDefinition)
                    || "[datetime2]".equals(originalDefinition)
                    || originalDefinition.matches("(?i)\\[?datetime2\\]?\\s*\\(.+")
                    ) {

                // If the scale for datetime2 is the database default anyway, omit it.
                if ( (parameters.length >= 1) &&
                    (Integer.parseInt(parameters[0].toString())
                        == (database.getDefaultScaleForNativeDataType("datetime2"))) ) {
                    parameters = new Object[0];
                }
                return new DatabaseDataType(database.escapeDataTypeName("datetime2"), parameters);
            }
            return new DatabaseDataType(database.escapeDataTypeName("datetime"));
        }
        if (database instanceof InformixDatabase) {

            // From database to changelog
            if (((getAdditionalInformation() == null) || getAdditionalInformation().isEmpty())
                && ((getParameters() != null) && (getParameters().length > 0))) {

                String parameter = String.valueOf(getParameters()[0]);

                if ("4365".equals(parameter)) {
                    return new DatabaseDataType("DATETIME YEAR TO FRACTION(3)");
                }

                if ("3594".equals(parameter)) {
                    return new DatabaseDataType("DATETIME YEAR TO SECOND");
                }

                if ("3080".equals(parameter)) {
                    return new DatabaseDataType("DATETIME YEAR TO MINUTE");
                }

                if ("2052".equals(parameter)) {
                    return new DatabaseDataType("DATETIME YEAR TO DAY");
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

        int maxFractionalDigits = database.getMaxFractionalDigitsForTimestamp();
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
            String[] genericFormats = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss[.nnnnnnnnn]",
                "yyyy-MM-dd' 'HH:mm:ss[.nnnnnnnnn]",
                "yyyy-MM-dd"
            };

            for (String format : genericFormats) {
                    /**
                     * Java 7's SimpleDateFormat cannot deal with microseconds. java.sql.Timestamp.valueOf cannot
                     * work with the 'T' form (ISO 8601). So it's either Java 8 or some custom library like Yoda.
                      */
                try {
                    /**
                     * If the value contains fractions of a second, normalise the fractions to 9 digits.
                      */
                    Matcher fractionalPartMatcher = Pattern.compile("(.*\\.)(\\d{1,9})(?:$| )(.*)").matcher(value);
                    if (fractionalPartMatcher.find()) {
                        String fractionalPart = fractionalPartMatcher.group(2);
                        // Add required number of '0's
                        fractionalPart = String.format("%-9s", fractionalPart).replace(' ', '0');
                        // And insert into the string
                        value = fractionalPartMatcher.replaceFirst("$1" + fractionalPart + "$3");
                    }

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
                    LocalDateTime parsedTimestamp = LocalDateTime.parse(value, dtf);
                    return Timestamp.valueOf(parsedTimestamp);
                } catch (DateTimeParseException e1) {
                    // It's ok, try the next format.
                }
            }

            if (value.contains("/") || value.contains("-")) {
                // maybe a custom format the database expects. Just return it as it is (String).
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
            // TODO: Potential error, MySQL 5.6.4+ supports up to 9 fractional digits
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        if (database instanceof MSSQLDatabase) {
            // TODO: Potential error, MSSQL supports up to 9 fractional digits
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        }

        if (database instanceof DB2Database) {
            return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }
}
