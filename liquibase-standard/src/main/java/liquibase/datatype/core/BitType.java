package liquibase.datatype.core;

import liquibase.Scope;
import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.util.StringUtil;

import java.util.Locale;

@DataTypeInfo(name = "bit", minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BitType extends LiquibaseDataType {

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
        }

        if ((database instanceof Db2zDatabase)) {
            return new DatabaseDataType("SMALLINT");
        } else if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("bit"));
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("bit")) {
                return new DatabaseDataType("BIT", getParameters());
            }
            return database instanceof MariaDBDatabase ? new DatabaseDataType("TINYINT(1)") : new DatabaseDataType("TINYINT");
        } else if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 1);
        } else if ((database instanceof SybaseASADatabase) || (database instanceof SybaseDatabase)) {
            return new DatabaseDataType("BIT");
        } else if (database instanceof DerbyDatabase) {
            if (((DerbyDatabase) database).supportsBooleanDataType()) {
                return new DatabaseDataType("SMALLINT");
            }
        } else if (database instanceof DB2Database) {
                return new DatabaseDataType("SMALLINT");
        } else if (database instanceof PostgresDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("bit")) {
                return new DatabaseDataType("BIT", getParameters());
            }
        } else if (database instanceof H2Database && getParameters().length > 0) {
            return new DatabaseDataType("BIT", getParameters());
        }

        return super.toDatabaseDataType(database);
    }

//    @Override
//    public String objectToSql(Object value, Database database) {
//        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
//            return null;
//        }
//
//        String returnValue="";
//        if (value instanceof String) {
//            value = ((String) value).replaceAll("'", "");
//            if ("1".equals(value) || "b'1'".equals(((String) value).toLowerCase(Locale.US))) {
//                returnValue = this.getTrueBitValue();
//            } else if ("0".equals(value) || "b'0'".equals(
//                    ((String) value).toLowerCase(Locale.US))) {
//                returnValue = this.getFalseBitValue();
//            } else if (database instanceof PostgresDatabase && Pattern.matches("b?([01])\\1*(::bit|::\"bit\")?", (String) value)) {
//                returnValue = "b'"
//                        + value.toString()
//                        .replace("b", "")
//                        .replace("\"", "")
//                        .replace("::it", "")
//                        + "'::\"bit\"";
//            }
//        } else if (value instanceof Number) {
//            if (value.equals(1) || "1".equals(value.toString()) || "1.0".equals(value.toString())) {
//                returnValue = this.getTrueBitValue();
//            } else {
//                returnValue = this.getFalseBitValue();
//            }
//        } else {
//            throw new UnexpectedLiquibaseException("Cannot convert type "+value.getClass()+" to a bit value");
//        }
//
//        return returnValue;
//    }
//
//    public String getFalseBitValue() {
//        return "0";
//    }
//
//    public String getTrueBitValue() {
//        return "1";
//    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.BIT;
    }
}
