package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtils;

@DataTypeInfo(name = "boolean", aliases = {"java.sql.Types.BOOLEAN", "java.lang.Boolean", "bit"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BooleanType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof AbstractDb2Database || database instanceof FirebirdDatabase) {
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
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }

        String returnValue;
        if (value instanceof String) {
            if (((String) value).equalsIgnoreCase("true") || value.equals("1") || value.equals("t") || ((String) value).equalsIgnoreCase(this.getTrueBooleanValue(database))) {
                returnValue = this.getTrueBooleanValue(database);
            } else if (((String) value).equalsIgnoreCase("false") || value.equals("0") || value.equals("f") || ((String) value).equalsIgnoreCase(this.getFalseBooleanValue(database))) {
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
            if (value.equals(1) || value.toString().equals("1") || value.toString().equals("1.0")) {
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
        return database instanceof AbstractDb2Database
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

    //sqllite
    //        } else if (columnTypeString.toLowerCase(Locale.ENGLISH).contains("boolean") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("binary")) {
//            type = new BooleanType("BOOLEAN");

}
