package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;


@DataTypeInfo(
        name = "bignumeric",
        minParameters = 0,
        maxParameters = 0,
        priority = LiquibaseDataType.PRIORITY_DATABASE
)
public class BignumericDataTypeBigQuery extends LiquibaseDataType {

    private static final String BIGNUMERIC = "BIGNUMERIC";

    public BignumericDataTypeBigQuery() {
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof BigQueryDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof BigQueryDatabase) {

            DatabaseDataType type = new DatabaseDataType(BIGNUMERIC, this.getParameters());
            if (this.getParameters().length > 0) {
                String firstParameter = String.valueOf(this.getParameters()[0]);
                try {
                    int typePrecision = Integer.parseInt(firstParameter);
                    if (typePrecision == 77) {
                        type.setType(BIGNUMERIC);
                    }
                } catch (NumberFormatException e) {
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
