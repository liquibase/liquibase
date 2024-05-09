package liquibase.datatype.core;

import liquibase.database.BigQueryDatabase;
import liquibase.datatype.DatabaseDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeographyDataTypeBigQueryTest {

    @Test
    void toDatabaseDataType() {
        GeographyDataTypeBigQuery datatype = new GeographyDataTypeBigQuery();
        DatabaseDataType databaseDataType = datatype.toDatabaseDataType(new BigQueryDatabase());
        assertEquals("GEOGRAPHY", databaseDataType.getType());
    }
}