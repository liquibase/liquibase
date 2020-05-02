package liquibase.database.core;

import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.derby.jdbc.BasicEmbeddedDataSource40;

import junit.framework.TestCase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;

public class DerbyDatabaseTest extends TestCase {
    public void testGetDefaultDriver() {
        Database database = new DerbyDatabase();

        assertEquals("org.apache.derby.jdbc.EmbeddedDriver", database.getDefaultDriver("java:derby:liquibase;create=true"));

        assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
    }

    public void testGetDateLiteral() {
        assertEquals("TIMESTAMP('2008-01-25 13:57:41')", new DerbyDatabase().getDateLiteral("2008-01-25 13:57:41"));
        assertEquals("TIMESTAMP('2008-01-25 13:57:41.300000')", new DerbyDatabase().getDateLiteral("2008-01-25 13:57:41.3"));
        assertEquals("TIMESTAMP('2008-01-25 13:57:41.340000')", new DerbyDatabase().getDateLiteral("2008-01-25 13:57:41.34"));
        assertEquals("TIMESTAMP('2008-01-25 13:57:41.347000')", new DerbyDatabase().getDateLiteral("2008-01-25 13:57:41.347"));
    }

    public void testCloseShutsEmbeddedDerbyDown() throws Exception {
        DataSource ds = newDataSource();
        try (Connection pooled = ds.getConnection()) {

            Database database = new DerbyDatabase();
            database.setConnection(new JdbcConnection(ds.getConnection()));
            database.close();

            assertEquals("connection.closed", true, pooled.isClosed());
        }
    }

    public void testCloseDoesNotShutEmbeddedDerbyDown() throws Exception {
        DataSource ds = newDataSource();
        try (Connection connection = ds.getConnection()) {

            DerbyDatabase database = new DerbyDatabase();
            database.setShutdownEmbeddedDerby(false);
            database.setConnection(new JdbcConnection(ds.getConnection()));
            database.close();

            assertEquals("connection.closed", false, connection.isClosed());
        }
    }

    private BasicEmbeddedDataSource40 newDataSource() {
        BasicEmbeddedDataSource40 ds = new BasicEmbeddedDataSource40();
        ds.setDatabaseName("memory:Foo");
        ds.setCreateDatabase("create");
        return ds;
    }

}
