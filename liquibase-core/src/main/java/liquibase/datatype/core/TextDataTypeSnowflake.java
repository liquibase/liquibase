package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.servicelocator.PrioritizedService;

@DataTypeInfo(name = "text", minParameters = 0, maxParameters = 0, priority = PrioritizedService.PRIORITY_DATABASE)
public class TextDataTypeSnowflake extends LiquibaseDataType {

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof SnowflakeDatabase) {
            return new DatabaseDataType("TEXT", getParameters());
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.STRING;
    }
}
