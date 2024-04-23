package liquibase.ext.bigquery.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.ext.bigquery.database.BigqueryDatabase;


@DataTypeInfo(
        name = "bignumeric",
        minParameters = 0,
        maxParameters = 0,
        priority = 1
)
public class BignumericDataTypeBigQuery extends LiquibaseDataType {

    private final static String BIGNUMERIC = "BIGNUMERIC";

    public BignumericDataTypeBigQuery() {
    }

    public boolean supports(Database database) {
        return database instanceof BigqueryDatabase;
    }

    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof BigqueryDatabase) {

            DatabaseDataType type = new DatabaseDataType(BIGNUMERIC, this.getParameters());
            if (this.getParameters().length > 0) {
                String firstParameter = String.valueOf(this.getParameters()[0]);
                int typePrecision = Integer.parseInt(firstParameter);
                if (typePrecision == 77) {
                    type.setType(BIGNUMERIC);
                }
            }
            return type;
        } else {
            return super.toDatabaseDataType(database);
        }

    }

    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }
}
