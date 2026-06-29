package liquibase.datatype.core;

import liquibase.Scope;
import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtil;

import java.util.Locale;
import java.util.regex.Pattern;

@DataTypeInfo(name = "boolean", aliases = {"java.sql.Types.BOOLEAN", "java.lang.Boolean", "bit", "bool"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BooleanType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
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
            if (database instanceof MariaDBDatabase) {
                if (originalDefinition.toLowerCase(Locale.US).startsWith("bit")) {
                    return new DatabaseDataType("BIT", getParameters());
                }
                return new DatabaseDataType("TINYINT(1)");
            }
            // For MySQL (not MariaDB): collapse bit / bit(1) / boolean → TINYINT(1).
            //
            // BooleanType handles the "bit" alias (see @DataTypeInfo).  Two separate
            // scenarios arrive here:
            //   a) A snapshot produced by the JDBC driver with tinyInt1isBit=true
            //      (the default): TINYINT(1) columns are reported as TYPE_NAME="BIT",
            //      so Liquibase builds originalDefinition="BIT(1)".  Without the guard
            //      below, generating a changelog from that snapshot would write BIT(1),
            //      re-applying it would create BIT(1) ≠ TINYINT(1), and every subsequent
            //      generateChangeLog would see a spurious diff.
            //   b) A changeset authored with type="boolean" or type="bit" (implying
            //      size 1 or unspecified): TINYINT(1) is the MySQL de-facto boolean type.
            //
            // Preserve genuine BIT(n) columns (n > 1) — users who declare
            // type="bit(8)" explicitly want a bit-field, not a boolean column.
            String lowerDef = originalDefinition.toLowerCase(Locale.US);
            if (lowerDef.startsWith("bit") && getParameters().length > 0
                    && !"1".equals(String.valueOf(getParameters()[0]))) {
                return new DatabaseDataType("BIT", getParameters());
            }
            return new DatabaseDataType("TINYINT(1)");
        } else if (database instanceof OracleDatabase) {
            try {
                if (database.getDatabaseMajorVersion() >= OracleDatabase.ORACLE_23C_MAJOR_VERSION) {
                    return new DatabaseDataType("BOOLEAN");
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Error checking database major version, assuming version <23: "+e.getMessage(), e);
            }
            return new DatabaseDataType("NUMBER", 1);
        } else if ((database instanceof SybaseASADatabase) || (database instanceof SybaseDatabase)) {
            return new DatabaseDataType("BIT");
        } else if (database instanceof DerbyDatabase) {
            if (((DerbyDatabase) database).supportsBooleanDataType()) {
                return new DatabaseDataType("BOOLEAN");
            } else {
                return new DatabaseDataType("SMALLINT");
            }
        } else if (database instanceof DB2Database) {
			if (((DB2Database) database).supportsBooleanDataType())
				return new DatabaseDataType("BOOLEAN");
			else
				return new DatabaseDataType("SMALLINT");
        } else if (database instanceof HsqlDatabase) {
            return new DatabaseDataType("BOOLEAN");
        } else if (database instanceof PostgresDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("bit")) {
                return new DatabaseDataType("BIT", getParameters());
            }
        } else if (database instanceof H2Database && getParameters().length > 0) {
          return new DatabaseDataType("BOOLEAN");
      }


        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
            return null;
        }

        String returnValue;
        if (value instanceof String) {
            value = ((String) value).replaceAll("'", "");
            if ("true".equals(((String) value).toLowerCase(Locale.US)) || "1".equals(value) || "b'1'".equals(((String) value).toLowerCase(Locale.US)) || "t".equals(((String) value).toLowerCase(Locale.US)) || ((String) value).toLowerCase(Locale.US).equals(this.getTrueBooleanValue(database).toLowerCase(Locale.US))) {
                returnValue = this.getTrueBooleanValue(database);
            } else if ("false".equals(((String) value).toLowerCase(Locale.US)) || "0".equals(value) || "b'0'".equals(
                    ((String) value).toLowerCase(Locale.US)) || "f".equals(((String) value).toLowerCase(Locale.US)) || ((String) value).toLowerCase(Locale.US).equals(this.getFalseBooleanValue(database).toLowerCase(Locale.US))) {
                returnValue = this.getFalseBooleanValue(database);
            } else if (database instanceof PostgresDatabase && Pattern.matches("b?([01])\\1*(::bit|::\"bit\")?", (String) value)) {
                returnValue = "b'" 
                        + value.toString()
                                .replace("b", "")
                                .replace("\"", "")
                                .replace("::it", "")
                        + "'::\"bit\"";
            } else if (database instanceof SybaseASADatabase && ((String) value).startsWith("COMPUTE")) {
                returnValue = (String) value;
            } else {
                throw new UnexpectedLiquibaseException("Unknown boolean value: " + value);
            }
        } else if (value instanceof Long) {
            if (Long.valueOf(1).equals(value)) {
                returnValue = this.getTrueBooleanValue(database);
            } else {
                returnValue = this.getFalseBooleanValue(database);
            }
        } else if (value instanceof Number) {
            if (value.equals(1) || "1".equals(value.toString()) || "1.0".equals(value.toString())) {
                returnValue = this.getTrueBooleanValue(database);
            } else {
                returnValue = this.getFalseBooleanValue(database);
            }
        } else if (value instanceof DatabaseFunction) {
            return value.toString();
        } else if (value instanceof Boolean) {
            if (((Boolean) value)) {
                returnValue = this.getTrueBooleanValue(database);
            } else {
                returnValue = this.getFalseBooleanValue(database);
            }
        } else {
            throw new UnexpectedLiquibaseException("Cannot convert type "+value.getClass()+" to a boolean value");
        }

        return returnValue;
    }

    protected boolean isNumericBoolean(Database database) {
        if ((database instanceof FirebirdDatabase)) {
            try {
                if (database.getDatabaseMajorVersion() <= 2) {
                    return true;
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Error checking database major version, assuming version 3+: "+e.getMessage(), e);
            }
            return false;
        }
        if (database instanceof DerbyDatabase) {
            return !((DerbyDatabase) database).supportsBooleanDataType();
        } else if (database instanceof DB2Database) {
			return !((DB2Database) database).supportsBooleanDataType();
    	}
        return (database instanceof Db2zDatabase) || (database instanceof FirebirdDatabase) || (database instanceof
            MSSQLDatabase) || (database instanceof MySQLDatabase) || (database instanceof OracleDatabase) ||
            (database instanceof SQLiteDatabase) || (database instanceof SybaseASADatabase) || (database instanceof
            SybaseDatabase);
    }

    /**
     * The database-specific value to use for "false" "boolean" columns.
     */
    public String getFalseBooleanValue(Database database) {
        if (isNumericBoolean(database)) {
            return "0";
        }
        if (database instanceof InformixDatabase) {
            return "'f'";
        }
        return "FALSE";
    }

    /**
     * The database-specific value to use for "true" "boolean" columns.
     */
    public String getTrueBooleanValue(Database database) {
        if (isNumericBoolean(database)) {
            return "1";
        }
        if (database instanceof InformixDatabase) {
            return "'t'";
        }
        return "TRUE";
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.BOOLEAN;
    }

}
