package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

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
        if (database instanceof DerbyDatabase || database instanceof PostgresDatabase) {
            return new DatabaseDataType("SMALLINT");
        }
        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType("TINYINT");
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER",3);
        }
        return super.toDatabaseDataType(database);
    }
}
