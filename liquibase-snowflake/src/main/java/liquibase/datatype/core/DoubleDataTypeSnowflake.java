package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.servicelocator.PrioritizedService;

@DataTypeInfo(
    name = "double",
    aliases = {"java.sql.Types.DOUBLE", "java.lang.Double"},
    minParameters = 0,
    maxParameters = 2,
    priority = PrioritizedService.PRIORITY_DATABASE
)
public class DoubleDataTypeSnowflake extends DoubleType {

    public DoubleDataTypeSnowflake() {

    }

    public int getPriority() {
        return PrioritizedService.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        // Double is an alias for the FLOAT data type in Snowflake.
        return new DatabaseDataType("FLOAT");
    }
}
