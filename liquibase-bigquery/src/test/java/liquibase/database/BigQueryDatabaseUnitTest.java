package liquibase.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BigQueryDatabaseUnitTest {

    private BigQueryDatabase database;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
    }

    @Test
    void getShortName() {
        assertEquals("bigquery", database.getShortName());
    }

    @Test
    void getDefaultDatabaseProductName() {
        assertEquals("Google BigQuery", database.getDefaultDatabaseProductName());
    }

    @Test
    void supportsDatabaseChangeLogHistory() {
        assertTrue(database.supportsDatabaseChangeLogHistory());
    }

    @Test
    void getCurrentDateTimeFunction() {
        assertEquals("CURRENT_DATETIME()", database.getCurrentDateTimeFunction());
    }

    @Test
    void getQuotingStartCharacter() {
        assertEquals("`", database.getQuotingStartCharacter());
    }

    @Test
    void getQuotingEndCharacter() {
        assertEquals("`", database.getQuotingEndCharacter());
    }

}