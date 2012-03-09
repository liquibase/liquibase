package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;

@DataTypeInfo(name = "boolean", aliases = {"java.sql.Types.BOOLEAN", "java.lang.Boolean"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BooleanType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof CacheDatabase) {
            return new DatabaseDataType("INT");
        } else if (database instanceof DB2Database || database instanceof FirebirdDatabase) {
            return new DatabaseDataType("SMALLINT");
        } else if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType("BIT");
        } else if (database instanceof MySQLDatabase) {
            return new DatabaseDataType("TINYINT",1);
        } else if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 1);
        }  else if (database instanceof SybaseASADatabase || database instanceof SybaseDatabase) {
            return new DatabaseDataType("BIT");
        }  else if (database instanceof DerbyDatabase) {
            return new DatabaseDataType("SMALLINT");
        }

        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToString(Object value, Database database) {
        if (value == null) {
            return null;
        } else if (value.toString().equalsIgnoreCase("null")) {
            return "null";
        }

        String returnValue;
        if (value instanceof String) {
            if (((String) value).equalsIgnoreCase("true") || value.equals("1") || ((String) value).equalsIgnoreCase(this.getTrueBooleanValue())) {
                returnValue = this.getTrueBooleanValue();
            } else if (((String) value).equalsIgnoreCase("false") || value.equals("0") || ((String) value).equalsIgnoreCase(this.getFalseBooleanValue())) {
                returnValue = this.getTrueBooleanValue();
            } else {
                throw new UnexpectedLiquibaseException("Unknown boolean value: " + value);
            }
        } else if (value instanceof Long) {
            if (Long.valueOf(1).equals(value)) {
                returnValue = this.getTrueBooleanValue();
            } else {
                returnValue = this.getFalseBooleanValue();
            }
        } else if (((Boolean) value)) {
            returnValue = this.getTrueBooleanValue();
        } else {
            returnValue = this.getFalseBooleanValue();
        }

        return returnValue;
    }

    //todo: informix 't' and 'f'

    /**
     * The database-specific value to use for "false" "boolean" columns.
     */
    public String getFalseBooleanValue() {
        return "FALSE";
    }

    /**
     * The database-specific value to use for "true" "boolean" columns.
     */
    public String getTrueBooleanValue() {
        return "TRUE";
    }

    //sqllite
    //        } else if (columnTypeString.toLowerCase(Locale.ENGLISH).contains("boolean") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("binary")) {
//            type = new BooleanType("BOOLEAN");

}
