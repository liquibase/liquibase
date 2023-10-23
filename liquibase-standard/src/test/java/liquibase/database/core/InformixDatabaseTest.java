package liquibase.database.core;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InformixDatabaseTest {

    private InformixDatabase database;

    @BeforeEach
    protected void setUp() throws Exception {
        database = new InformixDatabase();
    }

    public void testGetDateLiteral() {
        String d;

        d = database.getDateLiteral("2010-11-12 13:14:15");
        assertEquals("DATETIME (2010-11-12 13:14:15) YEAR TO FRACTION(5)", d);

        d = database.getDateLiteral("2010-11-12");
        assertEquals("'2010-11-12'", d);

        d = database.getDateLiteral("13:14:15");
        assertEquals("DATETIME (13:14:15) HOUR TO FRACTION(5)", d);
    }

    public void testGetDefaultDriver() {
        assertEquals("com.informix.jdbc.IfxDriver",
                database.getDefaultDriver("jdbc:informix-sqli://localhost:9088/liquibase:informixserver=ol_ids_1150_1"));
    }
}
