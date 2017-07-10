package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtils;

@DataTypeInfo(name = "boolean", aliases = {"java.sql.Types.BOOLEAN", "java.lang.Boolean", "bit", "bool"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BooleanType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof DB2Database || database instanceof FirebirdDatabase) {
            return new DatabaseDataType("SMALLINT");
        } else if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("bit"));
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase().startsWith("bit")) {
                return new DatabaseDataType("BIT", getParameters());
            }
            return new DatabaseDataType("BIT", 1);
        } else if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 1);
        } else if (database instanceof SybaseASADatabase || database instanceof SybaseDatabase) {
            return new DatabaseDataType("BIT");
        } else if (database instanceof DerbyDatabase) {
            if (((DerbyDatabase) database).supportsBooleanDataType()) {
                return new DatabaseDataType("BOOLEAN");
            } else {
                return new DatabaseDataType("SMALLINT");
            }
        } else if (database instanceof HsqlDatabase) {
            return new DatabaseDataType("BOOLEAN");
        } else if (database instanceof PostgresDatabase) {
            if (originalDefinition.toLowerCase().startsWith("bit")) {
                return new DatabaseDataType("BIT", getParameters());
            }
	}

        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || "null".equalsIgnoreCase(value.toString())) {
            return null;
        }

        String returnValue;
        if (value instanceof String) {
            if ("true".equalsIgnoreCase((String) value) || "1".equals(value) || "b'1'".equalsIgnoreCase((String)
                value) || "t".equals(value) || ((String) value).equalsIgnoreCase(this.getTrueBooleanValue(database))) {
                returnValue = this.getTrueBooleanValue(database);
            } else if ("false".equalsIgnoreCase((String) value) || "0".equals(value) || "b'0'".equalsIgnoreCase(
                (String) value) || "f".equals(value) || ((String) value).equalsIgnoreCase(this.getFalseBooleanValue(database))) {
                returnValue = this.getFalseBooleanValue(database);
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
        if (database instanceof DerbyDatabase) {
            return !((DerbyDatabase) database).supportsBooleanDataType();
        }
        return database instanceof DB2Database
                || database instanceof FirebirdDatabase
                || database instanceof MSSQLDatabase
                || database instanceof MySQLDatabase
                || database instanceof OracleDatabase
                || database instanceof SQLiteDatabase
                || database instanceof SybaseASADatabase
                || database instanceof SybaseDatabase;
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
