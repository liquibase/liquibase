package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;


@DataTypeInfo(
        name = "numeric",
        minParameters = 0,
        maxParameters = 0,
        priority = 1
)
public class NumberDataTypeBigQuery extends LiquibaseDataType {
    public NumberDataTypeBigQuery() {
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof BigQueryDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof BigQueryDatabase) {

            return new DatabaseDataType("NUMERIC", this.getParameters());
        } else {
            return super.toDatabaseDataType(database);
        }

    }

    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }
}
