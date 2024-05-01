package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(
        name = "float64",
        minParameters = 0,
        maxParameters = 0,
        priority = BigQueryDatabase.BIGQUERY_PRIORITY_DATABASE
)
public class Float64DataTypeBigQuery extends LiquibaseDataType {
    public Float64DataTypeBigQuery() {
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof BigQueryDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof BigQueryDatabase) {

            DatabaseDataType type = new DatabaseDataType("FLOAT64", this.getParameters());
            type.setType("FLOAT64");
            return type;
        } else {
            return super.toDatabaseDataType(database);
        }

    }

    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }
}
