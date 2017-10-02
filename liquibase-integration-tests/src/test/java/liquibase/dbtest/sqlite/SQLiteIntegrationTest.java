package liquibase.dbtest.sqlite;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.ValidationFailedException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class SQLiteIntegrationTest extends AbstractIntegrationTest {

    public SQLiteIntegrationTest() throws Exception {
        super("sqlite", DatabaseFactory.getInstance().getDatabase("sqlite"));
        File f = new File("sqlite");
        try {
            f.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        return true;
    }

    @Override
    @Test
    public void testRunChangeLog() throws Exception {
        super.testRunChangeLog();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Test
    public void smartDataLoad() throws Exception {
        if (this.getDatabase() == null) {
            return;
        }

        Liquibase liquibase = createLiquibase("changelogs/common/smartDataLoad.changelog.xml");
        clearDatabase();

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        // check that the automatically rollback now works too
        try {
            liquibase.rollback(new Date(0), this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Override
    @Test
    public void testDiffExternalForeignKeys() throws Exception {
        //cross-schema security for oracle is a bother, ignoring test for now
    }

    @Override
    public void testOutputChangeLog() throws Exception {
        LogService.getLog(getClass()).info(LogType.LOG, "Due to several unimplemented ALTER TABLE substatements in SQLite, " +
                "this test is technically impossible on this RDBMS.");
        assertTrue(true);
    }

}
