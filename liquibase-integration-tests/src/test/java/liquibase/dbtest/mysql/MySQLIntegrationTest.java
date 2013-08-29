package liquibase.dbtest.mysql;

import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.snapshot.*;
import org.junit.Test;

public class MySQLIntegrationTest extends AbstractIntegrationTest {

    public MySQLIntegrationTest() throws Exception {
        super("mysql", "jdbc:mysql://"+ getDatabaseServerHostname("MySQL") +"/liquibase");
    }

    @Test
    @Override
    public void testRunChangeLog() throws Exception {
        super.testRunChangeLog();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Test
    public void snapshot() throws Exception {
        if (getDatabase() == null) {
            return;
        }


        runCompleteChangeLog();
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(getDatabase().getDefaultSchema(), getDatabase(), new SnapshotControl(getDatabase()));
        System.out.println(snapshot);
    }

}
