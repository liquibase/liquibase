package liquibase.datatype.core;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtils;

@DataTypeInfo(name = "clob", aliases = { "longvarchar", "text", "longtext", "java.sql.Types.LONGVARCHAR", "java.sql.Types.CLOB", "nclob", "longnvarchar", "ntext", "java.sql.Types.LONGNVARCHAR", "java.sql.Types.NCLOB", "tinytext", "mediumtext" }, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
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
            if (database instanceof MSSQLDatabase && !StringUtils.isAscii(val)) {
                return "N'"+database.escapeStringForDatabase(val)+"'";
            }

            return "'"+database.escapeStringForDatabase(val)+"'";
        }
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof MSSQLDatabase) {
            if (!LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration.CONVERT_DATA_TYPES).getValue(Boolean.class) && originalDefinition.toLowerCase().startsWith("text")) {
                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("text"));
                type.addAdditionalInformation(getAdditionalInformation());
                return type;
            }
        }

        if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("BLOB SUB_TYPE TEXT");
        } else if (database instanceof SybaseASADatabase) {
            return new DatabaseDataType("LONG VARCHAR");
        } else if (database instanceof MSSQLDatabase) {
            if (originalDefinition.equalsIgnoreCase("text")
                    || originalDefinition.equals("[text]")
                    || originalDefinition.matches("(?i)text .+")
                    || originalDefinition.matches("\\[text\\] .+")) {

                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("text"));
                type.addAdditionalInformation(getAdditionalInformation());
                return type;
            }
            if (originalDefinition.equalsIgnoreCase("ntext")
                    || originalDefinition.equals("[ntext]")
                    || originalDefinition.matches("(?i)ntext .+")
                    || originalDefinition.matches("\\[ntext\\] .+")) {

                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("ntext"));
                type.addAdditionalInformation(getAdditionalInformation());
                return type;
            }
            if (originalDefinition.equalsIgnoreCase("nclob")) {
                try {
                    if (database.getDatabaseMajorVersion() <= 8) { //2000 or earlier
                        DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("ntext"));
                        type.addAdditionalInformation(getAdditionalInformation());
                        return type;
                    }
                } catch (DatabaseException ignore) { } //assuming it is a newer version

                return new DatabaseDataType(database.escapeDataTypeName("nvarchar"), "MAX");
            }
            try {
                if (database.getDatabaseMajorVersion() <= 8) { //2000 or earlier
                    DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("text"));
                    type.addAdditionalInformation(getAdditionalInformation());
                    return type;
                }
            } catch (DatabaseException ignore) { } //assuming it is a newer version

            return new DatabaseDataType(database.escapeDataTypeName("varchar"), "MAX");
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase().startsWith("text")) {
                return new DatabaseDataType("TEXT");
            } else if (originalDefinition.toLowerCase().startsWith("tinytext")) {
                return new DatabaseDataType("TINYTEXT");
            } else if (originalDefinition.toLowerCase().startsWith("mediumtext")) {
                return new DatabaseDataType("MEDIUMTEXT");
            } else if (originalDefinition.toLowerCase().startsWith("nclob")){
                DatabaseDataType type = new DatabaseDataType("LONGTEXT");
                type.addAdditionalInformation("CHARACTER SET utf8");
                return type;
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
            if (originalDefinition.equalsIgnoreCase("nclob")) {
                return new DatabaseDataType("NCLOB");
            }
            return new DatabaseDataType("CLOB");
        } else if (database instanceof InformixDatabase) {
            if (originalDefinition.toLowerCase().startsWith("text")) {
                return new DatabaseDataType("TEXT");
            }
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
