package liquibase.datatype.core;

import liquibase.database.BigQueryDatabase;
import liquibase.datatype.DatabaseDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StringDataTypeBigQueryTest {

    @Test
    void toDatabaseDataType() {
        StringDataTypeBigQuery stringDataTypeBigQuery = new StringDataTypeBigQuery();
        DatabaseDataType databaseDataType = stringDataTypeBigQuery.toDatabaseDataType(new BigQueryDatabase());
        assertNotNull(databaseDataType);
        assertEquals("STRING", databaseDataType.getType());
    }

    @Test
    void objectToSql() {
        StringDataTypeBigQuery stringDataTypeBigQuery = new StringDataTypeBigQuery();
        String sql = stringDataTypeBigQuery.objectToSql("TEST", new BigQueryDatabase());
        assertEquals("'TEST'", sql);
    }

    @Test
    void objectToSqlNewLineCharacter() {
        StringDataTypeBigQuery stringDataTypeBigQuery = new StringDataTypeBigQuery();
        String sql = stringDataTypeBigQuery.objectToSql("TEST\n NEW LINE", new BigQueryDatabase());
        assertEquals("'''TEST\n NEW LINE'''", sql);
    }
}