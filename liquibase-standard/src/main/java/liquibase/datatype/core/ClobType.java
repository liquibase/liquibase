package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtil;

import java.util.Locale;

@DataTypeInfo(name = "clob", aliases = {"java.sql.Types.CLOB", "nclob", "java.sql.Types.NCLOB"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
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
            if ((database instanceof MSSQLDatabase) && !StringUtil.isAscii(val)) {
                return "N'" + database.escapeStringForDatabase(val) + "'";
            }

            return "'" + database.escapeStringForDatabase(val) + "'";
        }
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
        if (database instanceof MSSQLDatabase) {
            if (Boolean.TRUE.equals(GlobalConfiguration.CONVERT_DATA_TYPES.getCurrentValue())) {
                if ("nclob".equals(originalDefinition.toLowerCase(Locale.US))) {
                    return new DatabaseDataType(database.escapeDataTypeName("nvarchar"), "MAX");
                }
                return new DatabaseDataType(database.escapeDataTypeName("varchar"), "MAX");
            }
        } else if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("BLOB SUB_TYPE TEXT");
        } else if (database instanceof SybaseASADatabase) {
            return new DatabaseDataType("LONG VARCHAR");
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("nclob")) {
                DatabaseDataType type = new DatabaseDataType("LONGTEXT");
                type.addAdditionalInformation("CHARACTER SET utf8");
                return type;
            } else {
                return new DatabaseDataType("LONGTEXT");
            }
        } else if ((database instanceof H2Database) || (database instanceof HsqlDatabase)) {

            return new DatabaseDataType("CLOB");

        } else if ((database instanceof PostgresDatabase) || (database instanceof SQLiteDatabase) || (database
                instanceof SybaseDatabase)) {
            return new DatabaseDataType("TEXT");
        } else if (database instanceof OracleDatabase) {
            if ("nclob".equals(originalDefinition.toLowerCase(Locale.US))) {
                return new DatabaseDataType("NCLOB");
            }
            return new DatabaseDataType("CLOB");
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.CLOB;
    }


}
