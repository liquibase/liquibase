package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.BigqueryDatabase;
import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;


@DataTypeInfo(
        name = "geography",
        minParameters = 0,
        maxParameters = 0,
        priority = BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE
)
public class GeographyDataTypeBigQuery extends LiquibaseDataType {
    public GeographyDataTypeBigQuery() {
    }

    public boolean supports(Database database) {
        return database instanceof BigqueryDatabase;
    }

    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof BigqueryDatabase) {

            DatabaseDataType type = new DatabaseDataType("GEOGRAPHY", this.getParameters());
            type.setType("GEOGRAPHY");
            return type;
        } else {
            return super.toDatabaseDataType(database);
        }

    }

    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }
}
