package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="number", aliases = {"numeric", "java.sql.Types.NUMERIC"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NumericType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MySQLDatabase) {
            return new DatabaseDataType("numeric", getParameters());
        }
        return super.toDatabaseDataType(database);
    }
}
