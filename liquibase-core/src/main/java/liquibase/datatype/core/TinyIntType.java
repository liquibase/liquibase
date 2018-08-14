package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

import java.util.Locale;

@DataTypeInfo(name="tinyint", aliases = "java.sql.Types.TINYINT", minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class TinyIntType  extends LiquibaseDataType {


    private boolean autoIncrement;

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("tinyint"));
        }
        if ((database instanceof DerbyDatabase) || (database instanceof PostgresDatabase) || (database instanceof
            FirebirdDatabase) || (database instanceof AbstractDb2Database)) {
            return new DatabaseDataType("SMALLINT");
        }
        if (database instanceof MySQLDatabase) {
            DatabaseDataType type = new DatabaseDataType("TINYINT", getParameters());
            type.addAdditionalInformation(getAdditionalInformation());
            return type;
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER",3);
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
            return null;
        }
        if (value instanceof DatabaseFunction) {
            return value.toString();
        }

        return formatNumber(value.toString());
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }
}
