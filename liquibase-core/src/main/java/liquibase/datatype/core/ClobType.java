package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtils;

import java.util.Locale;

@DataTypeInfo(name = "clob", aliases = {"longvarchar", "text", "longtext", "java.sql.Types.LONGVARCHAR", "java.sql.Types.CLOB", "nclob", "longnvarchar", "ntext", "java.sql.Types.LONGNVARCHAR", "java.sql.Types.NCLOB", "tinytext", "mediumtext"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class ClobType extends LiquibaseDataType {

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
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
            if ((database instanceof MSSQLDatabase) && !StringUtils.isAscii(val)) {
                return "N'" + database.escapeStringForDatabase(val) + "'";
            }

            return "'" + database.escapeStringForDatabase(val) + "'";
        }
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof MSSQLDatabase) {
            if ((!LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration
                .CONVERT_DATA_TYPES).getValue(Boolean.class) && originalDefinition.toUpperCase(Locale.US).startsWith("text"))
                || originalDefinition.toUpperCase(Locale.US).startsWith("[text]")) {
                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("varchar"));
                // If there is additional specification after ntext (e.g.  COLLATE), import that.
                String originalExtraInfo = originalDefinition.replaceFirst("^\\[?text\\]?\\s*", "");
                type.addAdditionalInformation("(max)"
                        + (StringUtils.isEmpty(originalExtraInfo) ? "" : " " + originalExtraInfo));
                return type;
            }
        }

        if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("BLOB SUB_TYPE TEXT");
        } else if (database instanceof SybaseASADatabase) {
            return new DatabaseDataType("LONG VARCHAR");
        } else if (database instanceof MSSQLDatabase) {
            if (originalDefinition.matches("^(?i)\\[?text\\]?.*")) {
                // The SQL Server datatype "text" is deprecated and should be replaced with VARCHAR(MAX).
                // See: https://docs.microsoft.com/en-us/sql/t-sql/data-types/ntext-text-and-image-transact-sql
                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("varchar"));
                // If there is additional specification after ntext (e.g.  COLLATE), import that.
                String originalExtraInfo = originalDefinition.replaceFirst("^\\[?text\\]?\\s*", "");
                type.addAdditionalInformation("(max)"
                        + (StringUtils.isEmpty(originalExtraInfo) ? "" : " " + originalExtraInfo));
                return type;
            }
            if (originalDefinition.toLowerCase(Locale.US).startsWith("ntext")
                    || originalDefinition.toLowerCase(Locale.US).startsWith("[ntext]")) {
                // The SQL Server datatype "ntext" is deprecated and should be replaced with NVARCHAR(MAX).
                // See: https://docs.microsoft.com/en-us/sql/t-sql/data-types/ntext-text-and-image-transact-sql
                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("nvarchar"));
                // If there is additional specification after ntext (e.g.  COLLATE), import that.
                String originalExtraInfo = originalDefinition.replaceFirst("^\\[?ntext\\]?\\s*", "");
                type.addAdditionalInformation("(max)"
                    + (StringUtils.isEmpty(originalExtraInfo) ? "" : " " + originalExtraInfo));
                return type;
            }
            if ("nclob".equals(originalDefinition.toLowerCase(Locale.US))) {
                return new DatabaseDataType(database.escapeDataTypeName("nvarchar"), "MAX");
            }

            return new DatabaseDataType(database.escapeDataTypeName("varchar"), "MAX");
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("text")) {
                return new DatabaseDataType("TEXT");
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("tinytext")) {
                return new DatabaseDataType("TINYTEXT");
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("mediumtext")) {
                return new DatabaseDataType("MEDIUMTEXT");
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("nclob")) {
                DatabaseDataType type = new DatabaseDataType("LONGTEXT");
                type.addAdditionalInformation("CHARACTER SET utf8");
                return type;
            } else {
                return new DatabaseDataType("LONGTEXT");
            }
        } else if ((database instanceof H2Database) || (database instanceof HsqlDatabase)) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("longvarchar") || originalDefinition.startsWith("java.sql.Types.LONGVARCHAR")) {
                return new DatabaseDataType("LONGVARCHAR");
            } else {
                return new DatabaseDataType("CLOB");
            }
        } else if ((database instanceof PostgresDatabase) || (database instanceof SQLiteDatabase) || (database
            instanceof SybaseDatabase)) {
            return new DatabaseDataType("TEXT");
        } else if (database instanceof OracleDatabase) {
            if ("nclob".equals(originalDefinition.toLowerCase(Locale.US))) {
                return new DatabaseDataType("NCLOB");
            }
            return new DatabaseDataType("CLOB");
        } else if (database instanceof InformixDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("text")) {
                return new DatabaseDataType("TEXT");
            }
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.CLOB;
    }


}
