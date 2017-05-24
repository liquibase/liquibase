package liquibase.dbtest.hsqldb;

import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;

import java.sql.SQLSyntaxErrorException;

public class HsqlIntegrationTest extends AbstractIntegrationTest {

    public static final String OBJECT_ALREADY_EXISTS = "42504";

    public HsqlIntegrationTest() throws Exception {
        super("hsqldb", DatabaseFactory.getInstance().getDatabase("hsqldb"));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            // Create schemas for tests testRerunDiffChangeLogAltSchema
            ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                    "CREATE SCHEMA LBCAT2"
            );
        } catch (SQLSyntaxErrorException e) {
            if (e.getSQLState().equals(OBJECT_ALREADY_EXISTS)) {
                ; // do nothing
            }
        }
        getDatabase().commit();
    }
}
