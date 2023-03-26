package liquibase.database.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import liquibase.exception.DatabaseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public abstract class AbstractDb2DatabaseTest<D extends AbstractDb2Database> {

    protected D database;

    protected AbstractDb2DatabaseTest(D database) throws Exception {
        this.database = database;
    }

    public D getDatabase() {
        return database;
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        " 2018-12-31          | DATE('2018-12-31')               ",
        " 23:58:59            | TIME('23:58:59')                 ",
        " 2018-12-31 23:58:59 | TIMESTAMP('2018-12-31 23:58:59') ",
        " foo                 | UNSUPPORTED:foo                  ",
    })
    public void testGetDateLiteral(String isoDate, String expected) throws DatabaseException {
        try (D database = getDatabase()) {
            assertEquals(expected, database.getDateLiteral(isoDate));
        }
    }
}
