package liquibase.datatype.core;

import liquibase.database.BigQueryDatabase;
import liquibase.datatype.DatabaseDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BoolDataTypeBigQueryTest {

    @Test
    void toDatabaseDataType() {
        BoolDataTypeBigQuery boolDataTypeBigQuery = new BoolDataTypeBigQuery();
        DatabaseDataType databaseDataType = boolDataTypeBigQuery.toDatabaseDataType(new BigQueryDatabase());
        assertNotNull(databaseDataType);
        assertEquals("BOOL", databaseDataType.getType());
    }
}