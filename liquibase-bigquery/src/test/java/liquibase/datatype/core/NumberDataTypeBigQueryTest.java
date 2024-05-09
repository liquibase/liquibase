package liquibase.datatype.core;

import liquibase.database.BigQueryDatabase;
import liquibase.datatype.DatabaseDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NumberDataTypeBigQueryTest {

    @Test
    void toDatabaseDataType() {
        NumberDataTypeBigQuery numberDataTypeBigQuery = new NumberDataTypeBigQuery();
        DatabaseDataType databaseDataType = numberDataTypeBigQuery.toDatabaseDataType(new BigQueryDatabase());
        assertNotNull(databaseDataType);
        assertEquals("NUMERIC", databaseDataType.getType());
    }
}