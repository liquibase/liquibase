package liquibase.datatype.core;

import java.math.BigInteger;
import java.util.Arrays;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtils;

@DataTypeInfo(name="clob", aliases = {"longvarchar", "text", "longtext", "java.sql.Types.LONGVARCHAR", "java.sql.Types.CLOB", "ntext", "java.sql.Types.NCLOB"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class ClobType extends LiquibaseDataType {

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }

        if (value instanceof DatabaseFunction) {
            return value.toString();
        }

        String val = String.valueOf(value);
        // postgres type character varying gets identified as a char type
        // simple sanity check to avoid double quoting a value
        if (val.startsWith("'")) {
            return val;
        } else {
            return "'"+database.escapeStringForDatabase(val)+"'";
        }
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());

        if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("BLOB SUB_TYPE TEXT");
        } else if (database instanceof SybaseASADatabase) {
            return new DatabaseDataType("LONG VARCHAR");
        } else if (database instanceof MSSQLDatabase) {
            if (originalDefinition.equalsIgnoreCase("text")
                    || originalDefinition.equals("[text]")) {

                return new DatabaseDataType("[text]");
            }
            if (originalDefinition.equalsIgnoreCase("ntext")
                    || originalDefinition.equals("[ntext]")) {

                return new DatabaseDataType("[ntext]");
            }
            Object[] parameters = getParameters();
            boolean max = true;
            if (parameters.length > 0) {
                String param1 = parameters[0].toString();
                max = !param1.matches("\\d+")
                        || new BigInteger(param1).compareTo(BigInteger.valueOf(4000L)) > 0;
            }
            if (max) {
                try {
                    if (database.getDatabaseMajorVersion() <= 8) { //2000 or earlier
                        return new DatabaseDataType("[ntext]");
                    }
                } catch (DatabaseException ignore) { } //assuming it is a newer version

                return new DatabaseDataType("[nvarchar]", "MAX");
            }
            if (parameters.length > 1) {
                parameters = Arrays.copyOfRange(parameters, 0, 1);
            }
            return new DatabaseDataType("[nvarchar]", parameters);
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase().startsWith("text")) {
                return new DatabaseDataType("TEXT");
            } else {
                return new DatabaseDataType("LONGTEXT");
            }
        } else if (database instanceof H2Database || database instanceof HsqlDatabase) {
            if (originalDefinition.toLowerCase().startsWith("longvarchar") || originalDefinition.startsWith("java.sql.Types.LONGVARCHAR")) {
                return new DatabaseDataType("LONGVARCHAR");
            } else {
                return new DatabaseDataType("CLOB");
            }
        } else if (database instanceof PostgresDatabase || database instanceof SQLiteDatabase || database instanceof SybaseDatabase) {
            return new DatabaseDataType("TEXT");
        } else if (database instanceof OracleDatabase) {
            return new DatabaseDataType("CLOB");
        }
        return super.toDatabaseDataType(database);
    }

    //sqlite
    //        } else if (columnTypeString.equals("TEXT") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("uuid") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("uniqueidentifier") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).equals("uniqueidentifier") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).equals("datetime") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("timestamp") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("char") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("clob") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("text")) {
//            type = new CustomType("TEXT",0,0);

}
