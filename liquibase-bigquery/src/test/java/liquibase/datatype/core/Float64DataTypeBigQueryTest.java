package liquibase.datatype.core;

import liquibase.database.BigQueryDatabase;
import liquibase.datatype.DatabaseDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Float64DataTypeBigQueryTest {

    @Test
    void toDatabaseDataType() {
        Float64DataTypeBigQuery float64DataTypeBigQuery = new Float64DataTypeBigQuery();
        DatabaseDataType databaseDataType = float64DataTypeBigQuery.toDatabaseDataType(new BigQueryDatabase());
        assertNotNull(databaseDataType);
        assertEquals("FLOAT64", databaseDataType.getType());
    }
}