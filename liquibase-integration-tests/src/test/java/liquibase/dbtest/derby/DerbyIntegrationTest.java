package liquibase.dbtest.derby;

import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;

import java.sql.SQLException;

public class DerbyIntegrationTest extends AbstractIntegrationTest {

    public static final String DERBY_SQLSTATE_OBJECT_ALREADY_EXISTS = "X0Y68";

    public DerbyIntegrationTest() throws Exception {
        super("derby", DatabaseFactory.getInstance().getDatabase("derby"));
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        // Derby is an in-process database
        return true;
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            // Create schemas for tests testRerunDiffChangeLogAltSchema
            ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                    "CREATE SCHEMA LIQUIBASE"
            );
        } catch (SQLException e) {
            if (e.getSQLState().equals(DERBY_SQLSTATE_OBJECT_ALREADY_EXISTS)) {
                // do nothing
            } else {
                throw e;
            }
        }
        ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                "SET SCHEMA LIQUIBASE"
        );
        getDatabase().commit();
    }
}
