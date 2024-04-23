package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.BigqueryDatabase;
import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(
        name = "float64",
        minParameters = 0,
        maxParameters = 0,
        priority = BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE
)
public class Float64DataTypeBigQuery extends LiquibaseDataType {
    public Float64DataTypeBigQuery() {
    }

    public boolean supports(Database database) {
        return database instanceof BigqueryDatabase;
    }

    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof BigqueryDatabase) {

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
