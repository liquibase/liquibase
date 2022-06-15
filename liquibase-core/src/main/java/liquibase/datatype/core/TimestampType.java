package liquibase.datatype.core;

import liquibase.Scope;
import liquibase.change.core.LoadDataChange;
import java.util.Locale;

import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseIncapableOfOperation;
import liquibase.util.StringUtil;
import liquibase.util.grammar.ParseException;

/**
 * Data type support for TIMESTAMP data types in various DBMS. All DBMS are at least expected to support the
 * year, month, day, hour, minute and second parts. Optionally, fractional seconds and time zone information can be
 * specified as well.
 */
@DataTypeInfo(name = "timestamp", aliases = {"java.sql.Types.TIMESTAMP", "java.sql.Types.TIMESTAMP_WITH_TIMEZONE", "java.sql.Timestamp", "timestamptz"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class TimestampType extends DateTimeType {

    /**
     * Returns a DBMS-specific String representation of this TimestampType for use in SQL statements.
     * @param database the database for which the String must be generated
     * @return a String with the DBMS-specific type specification
     */
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
        // If a fractional precision is given, check is the DBMS supports the length
        if (getParameters().length > 0) {
            Integer desiredLength = null;
            try {
                desiredLength = Integer.parseInt(String.valueOf(getParameters()[0]));
            } catch (NumberFormatException e) {
                // That is ok, we won't touch the parameter in this case.
            }

            if (desiredLength != null) {
                int maxFractionalDigits = database.getMaxFractionalDigitsForTimestamp();
                if (maxFractionalDigits < desiredLength) {
                    throw new DatabaseIncapableOfOperation(
                            String.format(
                                    "Using a TIMESTAMP data type with a fractional precision of %d", desiredLength
                            ),
                            String.format(
                                    "A timestamp datatype with %d fractional digits was requested, but %s " +
                                            "only supports %d digits.",
                                    desiredLength,
                                    database.getDatabaseProductName(),
                                    maxFractionalDigits
                            ),
                            database
                    );
                }
            }
        }

        if (database instanceof MySQLDatabase) {
            if (originalDefinition.contains(" ") || originalDefinition.contains("(")) {
                return new DatabaseDataType(getRawDefinition());
            }
            return super.toDatabaseDataType(database);
        }
        if (database instanceof MSSQLDatabase) {
            if (!GlobalConfiguration.CONVERT_DATA_TYPES.getCurrentValue()
                    && originalDefinition.toLowerCase(Locale.US).startsWith("timestamp")) {
                return new DatabaseDataType(database.escapeDataTypeName("timestamp"));
            }
            Object[] parameters = getParameters();
            if (parameters.length >= 1) {
                final int paramValue = Integer.parseInt(parameters[0].toString());
                // If the scale for datetime2 is the database default anyway, omit it.
                // If the scale is 8, omit it since it's not a valid value for datetime2
                if (paramValue > 7 || paramValue == (database.getDefaultScaleForNativeDataType("datetime2"))) {
                    parameters = new Object[0];

                }

            }
            return new DatabaseDataType(database.escapeDataTypeName("datetime2"), parameters);
        }
        if (database instanceof SybaseDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("datetime"));
        }
        if (database instanceof AbstractDb2Database) {
            Object[] parameters = getParameters();
            if ((parameters != null) && (parameters.length > 1)) {
                parameters = new Object[] {parameters[1]};
            }
            return new DatabaseDataType(database.escapeDataTypeName("timestamp"), parameters);
        }

        /*
         * From here on, we assume that we have a SQL standard compliant database that supports the
         * TIMESTAMP[(p)] [WITH TIME ZONE|WITHOUT TIME ZONE] syntax. p is the number of fractional digits,
         * i.e. if "2017-06-02 23:59:45.123456" is supported by the DBMS, p would be 6.
         */
        DatabaseDataType type;

        if (getParameters().length > 0) {
            int fractionalDigits = 0;
            String fractionalDigitsInput = getParameters()[0].toString();
            try {
                fractionalDigits = Integer.parseInt(fractionalDigitsInput);
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                    new ParseException(String.format("A timestamp with '%s' fractional digits was requested, but '%s' does not " +
                        "seem to be an integer.", fractionalDigitsInput, fractionalDigitsInput))
                );
            }
            int maxFractionalDigits = database.getMaxFractionalDigitsForTimestamp();
            if (maxFractionalDigits < fractionalDigits) {
                Scope.getCurrentScope().getLog(getClass()).warning(String.format(
                        "A timestamp datatype with %d fractional digits was requested, but the DBMS %s only supports " +
                                "%d digits. Because of this, the number of digits was reduced to %d.",
                        fractionalDigits, database.getDatabaseProductName(), maxFractionalDigits, maxFractionalDigits)
                );
                fractionalDigits = maxFractionalDigits;
            }
            type =  new DatabaseDataType("TIMESTAMP", fractionalDigits);
        } else {
            type = new DatabaseDataType("TIMESTAMP");
        }

        if (originalDefinition.startsWith("java.sql.Types.TIMESTAMP_WITH_TIMEZONE")
            && (database instanceof PostgresDatabase
            || database instanceof OracleDatabase
            || database instanceof H2Database
            || database instanceof HsqlDatabase)) {

            if (database instanceof PostgresDatabase || database instanceof H2Database) {
                type.addAdditionalInformation("WITH TIME ZONE");
            } else {
                type.addAdditionalInformation("WITH TIMEZONE");
            }

            return type;
        }

        if (getAdditionalInformation() != null
                && (database instanceof PostgresDatabase
                || database instanceof OracleDatabase)
                || database instanceof H2Database
                || database instanceof HsqlDatabase){
            String additionalInformation = this.getAdditionalInformation();

            if (additionalInformation != null) {
                String additionInformation = additionalInformation.toUpperCase(Locale.US);
                if ((database instanceof PostgresDatabase || database instanceof H2Database) && additionInformation.toUpperCase(Locale.US).contains("TIMEZONE")) {
                    additionalInformation = additionInformation.toUpperCase(Locale.US).replace("TIMEZONE", "TIME ZONE");
                }
                // CORE-3229 Oracle 11g doesn't support WITHOUT clause in TIMESTAMP data type
                if ((database instanceof OracleDatabase) && additionInformation.startsWith("WITHOUT")) {
                    // https://docs.oracle.com/cd/B19306_01/server.102/b14225/ch4datetime.htm#sthref389
                    additionalInformation = null;
                }

                if ((database instanceof H2Database) && additionInformation.startsWith("WITHOUT")) {
                    // http://www.h2database.com/html/datatypes.html
                    additionalInformation = null;
                }
            }

            type.addAdditionalInformation(additionalInformation);
            return type;
        }

        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.DATE;
    }


}
