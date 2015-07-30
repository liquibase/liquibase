package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

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
        if (database instanceof DerbyDatabase || database instanceof PostgresDatabase || database instanceof FirebirdDatabase) {
            return new DatabaseDataType("SMALLINT");
        }
        if (database instanceof MySQLDatabase) {
            DatabaseDataType type = new DatabaseDataType("TINYINT");
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
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }
        if (value instanceof DatabaseFunction) {
            return value.toString();
        }

        return formatNumber(value.toString());
    }
}
