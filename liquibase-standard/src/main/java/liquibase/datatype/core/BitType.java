package liquibase.datatype.core;

import liquibase.Scope;
import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DataTypeInfo(name = "bit", minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BitType extends LiquibaseDataType {

    private static final Pattern PG_BIT_LITERAL_PATTERN = Pattern.compile("(?i)b'([01]+)'(::bit.*)?");
    private static final Pattern BINARY_STRING_PATTERN = Pattern.compile("[01]+");

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if ((database instanceof FirebirdDatabase)) {
            try {
                if (database.getDatabaseMajorVersion() <= 2) {
                    return new DatabaseDataType("SMALLINT");
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Error checking database major version, assuming version 3+: "+e.getMessage(), e);
            }
            return new DatabaseDataType("BOOLEAN");
        }

        if ((database instanceof Db2zDatabase)) {
            return new DatabaseDataType("SMALLINT");
        } else if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("bit"));
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("bit") && getParameters().length > 0) {
                return new DatabaseDataType("BIT", getParameters());
            }
            return database instanceof MariaDBDatabase ? new DatabaseDataType("TINYINT(1)") : new DatabaseDataType("TINYINT");
        } else if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 1);
        } else if ((database instanceof SybaseASADatabase) || (database instanceof SybaseDatabase)) {
            return new DatabaseDataType("BIT");
        } else if (database instanceof DerbyDatabase) {
            return new DatabaseDataType("SMALLINT");
        } else if (database instanceof DB2Database) {
            return new DatabaseDataType("SMALLINT");
        } else if (database instanceof PostgresDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("bit")) {
                return new DatabaseDataType("BIT", getParameters());
            }
        } else if (database instanceof H2Database) {
            if (getParameters().length > 0) {
                return new DatabaseDataType("BIT", getParameters());
            }
            return new DatabaseDataType("BOOLEAN");
        }

        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
            return null;
        }

        if (database instanceof PostgresDatabase) {
            String strValue = value.toString().trim();

            // Already formatted as PostgreSQL bit literal - normalize to uppercase
            Matcher bitLiteralMatcher = PG_BIT_LITERAL_PATTERN.matcher(strValue);
            if (bitLiteralMatcher.matches()) {
                String bitString = bitLiteralMatcher.group(1);
                String cast = bitLiteralMatcher.group(2);
                if (cast != null) {
                    return "B'" + bitString + "'" + cast;
                } else {
                    return "B'" + bitString + "'";
                }
            }
            // Binary string (e.g., "101010")
            if (BINARY_STRING_PATTERN.matcher(strValue).matches()) {
                return "B'" + strValue + "'";
            }

            // Decimal integer - convert to binary
            try {
                long decimalValue = Long.parseLong(strValue);
                if (decimalValue >= 0) {
                    return "B'" + Long.toBinaryString(decimalValue) + "'";
                }
            } catch (NumberFormatException e) {
                // Not a decimal, fall through
            }
        }


        return super.objectToSql(value, database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        // BIT without parameters or BIT(1) is commonly used as a boolean substitute
        // BIT(n) where n > 1 is a bit string and should be handled as BIT
        if (getParameters().length == 0) {
            return LoadDataChange.LOAD_DATA_TYPE.BOOLEAN;
        }
        if (getParameters().length >= 1) {
            try {
                int size = Integer.parseInt(getParameters()[0].toString());
                if (size == 1) {
                    return LoadDataChange.LOAD_DATA_TYPE.BOOLEAN;
                }
            } catch (NumberFormatException e) {
                // If we can't parse the parameter, default to BIT
            }
        }
        return LoadDataChange.LOAD_DATA_TYPE.BIT;
    }
}
