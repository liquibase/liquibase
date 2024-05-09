package liquibase.datatype.core;

import liquibase.database.BigQueryDatabase;
import liquibase.datatype.DatabaseDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Int64DataTypeBigQueryTest {

    @Test
    void toDatabaseDataType() {
        Int64DataTypeBigQuery int64DataTypeBigQuery = new Int64DataTypeBigQuery();
        DatabaseDataType databaseDataType = int64DataTypeBigQuery.toDatabaseDataType(new BigQueryDatabase());
        assertNotNull(databaseDataType);
        assertEquals("INT64", databaseDataType.getType());
    }
}