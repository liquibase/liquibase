package liquibase.datatype.core;

import liquibase.database.BigQueryDatabase;
import liquibase.datatype.DatabaseDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NumericDataTypeBigQueryTest {

    @Test
    void toDatabaseDataType() {
        NumericDataTypeBigQuery numericDataTypeBigQuery = new NumericDataTypeBigQuery();
        DatabaseDataType databaseDataType = numericDataTypeBigQuery.toDatabaseDataType(new BigQueryDatabase());
        assertNotNull(databaseDataType);
        assertEquals("NUMERIC", databaseDataType.getType());
    }
}