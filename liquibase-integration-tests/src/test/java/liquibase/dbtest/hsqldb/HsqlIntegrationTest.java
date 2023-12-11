package liquibase.dbtest.hsqldb;

import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import liquibase.util.SystemUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.sql.SQLSyntaxErrorException;

public class HsqlIntegrationTest extends AbstractIntegrationTest {

    public static final String OBJECT_ALREADY_EXISTS = "42504";

    public HsqlIntegrationTest() throws Exception {
        super("hsqldb", DatabaseFactory.getInstance().getDatabase(SystemUtil.isAtLeastJava11() ? "hsqldb": "none"));
    }


    @Override
    public void setUp() throws Exception {
        Assume.assumeTrue(SystemUtil.isAtLeastJava11()) ; // Since HSQLDB 2.7.1 it requires java 11
        super.setUp();
        try {
            // Create schemas for tests testRerunDiffChangeLogAltSchema
            ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                    "CREATE SCHEMA LBCAT2"
            );
        } catch (SQLSyntaxErrorException e) {
            if (e.getSQLState().equals(OBJECT_ALREADY_EXISTS)) {
                // do nothing
            }
        }
        getDatabase().commit();
    }
}
