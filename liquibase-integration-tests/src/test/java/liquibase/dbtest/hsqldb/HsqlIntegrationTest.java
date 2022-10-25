package liquibase.dbtest.hsqldb;

import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.util.SystemUtil;
import org.junit.Assume;

import java.sql.SQLSyntaxErrorException;

public class HsqlIntegrationTest extends AbstractIntegrationTest {

    public static final String OBJECT_ALREADY_EXISTS = "42504";

    public HsqlIntegrationTest() throws Exception {
        super("hsqldb", DatabaseFactory.getInstance().getDatabase("hsqldb"));
    }


    @Override
    public void setUp() throws Exception {
        Assume.assumeTrue(SystemUtil.getJavaMajorVersion() >= 11) ; // Since HSQLDB 2.7.1 it requires java 11
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
