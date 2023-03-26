package liquibase.database.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DerbyDatabaseTest {

    @Test
    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new DerbyDatabase()) {
            assertEquals("org.apache.derby.jdbc.EmbeddedDriver", database.getDefaultDriver("java:derby:liquibase;create=true"));

            assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
        }
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        " 2008-01-25 13:57:41     | TIMESTAMP('2008-01-25 13:57:41')        ",
        " 2008-01-25 13:57:41.3   | TIMESTAMP('2008-01-25 13:57:41.300000') ",
        " 2008-01-25 13:57:41.34  | TIMESTAMP('2008-01-25 13:57:41.340000') ",
        " 2008-01-25 13:57:41.347 | TIMESTAMP('2008-01-25 13:57:41.347000') ",
    })
    public void testGetDateLiteral(String isoDate, String expected) {
        assertEquals(expected, new DerbyDatabase().getDateLiteral(isoDate));
    }

    @Test
    public void testCloseShutsEmbeddedDerbyDown() throws Exception {
        Connection con = mockConnection();
        DerbyDatabase database = spyDatabase(con);

        database.close();

        verify(database).shutdownDerby(anyString(), anyString());
        verify(con).close();
    }

    @Test
    public void testCloseDoesNotShutEmbeddedDerbyDown() throws Exception {
        Connection con = mockConnection();
        DerbyDatabase database = spyDatabase(con);
        database.setShutdownEmbeddedDerby(false);

        database.close();

        verify(database, never()).shutdownDerby(anyString(), anyString());
        verify(con).close();
    }

    private static DerbyDatabase spyDatabase(Connection con) throws DatabaseException {
        DerbyDatabase database = spy(new DerbyDatabase());
        doNothing().when(database).shutdownDerby(anyString(), anyString());
        database.setConnection(new JdbcConnection(con));
        return database;
    }

    private static Connection mockConnection() throws SQLException {
        Connection con = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class, RETURNS_SMART_NULLS);
        when(metaData.getURL()).thenReturn("jdbc:derby:memory:foo");
        when(metaData.getDriverName()).thenReturn("org.apache.derby.jdbc.EmbeddedDriver");
        when(con.getMetaData()).thenReturn(metaData);
        return con;
    }
}
