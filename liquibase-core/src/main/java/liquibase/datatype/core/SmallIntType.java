package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

@DataTypeInfo(name="smallint", aliases = {"java.sql.Types.SMALLINT", "int2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class SmallIntType extends LiquibaseDataType {

    private boolean autoIncrement;

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof DB2Database || database instanceof DerbyDatabase || database instanceof FirebirdDatabase || database instanceof MSSQLDatabase || database instanceof PostgresDatabase || database instanceof InformixDatabase || database instanceof MySQLDatabase) {
            return new DatabaseDataType("SMALLINT"); //always smallint regardless of parameters passed
        }

        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 5);
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

        if (value instanceof Boolean)
            return Boolean.TRUE.equals(value) ? "1" : "0";
        else {
            return formatNumber(value.toString());
        }
    }

}
